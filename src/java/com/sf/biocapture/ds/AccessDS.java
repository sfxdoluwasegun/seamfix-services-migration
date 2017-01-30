package com.sf.biocapture.ds;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import nw.orm.core.query.QueryModifier;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.jboss.resteasy.util.Base64;

import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.app.KannelSMS;
import com.sf.biocapture.entity.KitMarker;
import com.sf.biocapture.entity.KycBlacklist;
import com.sf.biocapture.entity.KycBroadcast;
import com.sf.biocapture.entity.Node;
import com.sf.biocapture.entity.onboarding.OnboardingStatus;
import com.sf.biocapture.entity.Setting;
import com.sf.biocapture.entity.audit.VersionLog;
import com.sf.biocapture.entity.enums.KycPrivilege;
import com.sf.biocapture.entity.enums.OtpStatusRecordTypeEnum;
import com.sf.biocapture.entity.enums.SettingsEnum;
import com.sf.biocapture.entity.enums.SimSwapModes;
import com.sf.biocapture.entity.enums.VersionType;
import com.sf.biocapture.entity.onboarding.AgentFingerprint;
import com.sf.biocapture.entity.security.KMRole;
import com.sf.biocapture.entity.security.KMUser;
import com.sf.biocapture.entity.security.PasswordPolicy;
import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.access.AccessResponse;
import com.sf.biocapture.ws.access.AccessResponseData;
import com.sf.biocapture.ws.access.AgentOnboardingResponseData;
import com.sf.biocapture.ws.access.FingerLoginResponse;
import com.sf.biocapture.ws.access.FetchPrivilegesResponse;
import com.sf.biocapture.ws.access.NodeData;
import com.sf.biocapture.ws.access.PasswordResetResponse;
import com.sf.biocapture.ws.access.SettingsResponse;
import com.sf.biocapture.ws.access.pojo.InputComponents;
import com.sf.biocapture.ws.onboarding.AgentFingerprintPojo;
import com.sf.biocapture.ws.otp.OtpDS;
import com.sf.biocapture.ws.tags.ClientRefRequest;
import com.sf.biocapture.ws.threshold.ThresholdService;

import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import nw.orm.core.exception.NwormQueryException;
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AccessDS extends DataService {

	private final String BLACKLIST_KEY = "XLIST-";
	private final String AUTHENTICATION_KEY = "#D8CK>HIGH<LOW>#";
	private final String DEFAULT_KM_SERVLET_URL = "http://kycmanager.mtnnigeria.net:8070/KycManagerMTN-portlet/PasswordUpdate";
	private final String DEFAULT_KM_SERVLET_URL_PILOT = "http://10.1.242.34:8090/KycManagerMTN-portlet/PasswordUpdate";
        private final static String EMAIL_ADDRESS = "emailAddress";
        private static final String ACTIVE = "active";
        private static final String OTP_ERROR_MESSAGE = "An error occurred while generating OTP. Please try again later.";
	@Inject
	private BioCache cache;
	@Inject
	private OtpDS otpDS;
	@Inject 
	private KannelSMS kSms;
        
        private ThresholdUtil thresholdUtil = new ThresholdUtil();
	
	public static void main(String[] args) {
		System.out.println(new Date().getTime());
	}


	public AccessResponse doPasswordReset(String email, String password){
		AccessResponse ar = new AccessResponse();

		//check if any fields are empty
		if(isEmpty(email) || isEmpty(password)){
			ar.setStatus(-1);
			ar.setMessage("Required fields are missing");
		}else{
			KMUser user = getUser(email);

			if(user != null){
				//do password reset in kyc manager
				PasswordResetResponse resp = doKMPassordReset(user.getOrbitaId(), password);	
				if(resp != null){
					if( resp.getStatusCode() == 0 ){
						//update KMUser                                        
						user.setPassword(password);
						user.setLastPasswordChange(new Timestamp(new Date().getTime()));
						user.setClientFirstLogin(new Timestamp(new Date().getTime()));
						dbService.update(user);

						ar.setStatus(resp.getStatusCode());
						ar.setMessage(resp.getStatusMessage());

						logger.info("Password reset successful!!!!! for email: " + email);
					}else{
						logger.info("Password reset failed for: " + email);
						ar.setStatus(resp.getStatusCode());
						ar.setMessage(resp.getStatusMessage());
					}
				}else{
					logger.info("Password reset failed for: " + email);
					ar.setStatus(-1);
					ar.setMessage("Password reset could not be performed at this time. Try again later.");
				}
			}else{
				logger.info("User with email " + email + " was not found!!!");
				ar.setStatus(-1);
				ar.setMessage("User not found");
			}
		}
		return ar;
	}

	private HttpResponse makeHttpRequest(String serverUrl, Long orbitaId, String password){
		HttpResponse response = null;
		try {
			HttpClient hc = new DefaultHttpClient();
			logger.info("***** the kyc manager server url is : " + serverUrl);
			HttpPost hp = new HttpPost( serverUrl );//URLEncoder.encode(serverUrl,"UTF-8"));
			hp.setHeader("sc-auth-key", AUTHENTICATION_KEY);

			List<NameValuePair> nvps = new ArrayList<>();
			nvps.add(new BasicNameValuePair("orbitaId", orbitaId.toString()));
			nvps.add(new BasicNameValuePair("password", password));

			hp.setEntity(new UrlEncodedFormEntity(nvps));

			response = hc.execute(hp);

			return response;
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception ", e);
		}catch (IOException e) {
			logger.error("Exception ", e);
		}
		return response;
	}

	@SuppressWarnings("deprecation")
	private PasswordResetResponse doKMPassordReset(Long orbitaId, String password){
		String serverUrl = getSettingValue("KM_SERVLET_URL", DEFAULT_KM_SERVLET_URL);

		//Execute HTTP Post Request
		HttpResponse response = makeHttpRequest(serverUrl, orbitaId, password);
		try {
			if( response != null ){
				int responseCode = response.getStatusLine().getStatusCode();
				if(responseCode == 200){
					String resp = convertStreamToString(response.getEntity().getContent());
					return new Gson().fromJson(resp, PasswordResetResponse.class);
				}else{
					logger.debug("***** connecting to kyc manager returned a status code of : " + responseCode);
				}                        
			}else{
				logger.debug("*** response from kyc manager is null...");
			}

		} catch (IOException | UnsupportedOperationException e) {
			logger.error("An exception was thrown attempting to call km for password reset: ", e);
		}

		return null;
	}

	private String convertStreamToString(InputStream is) {

		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public String checkBlacklistStatus(String tag, String mac){
		String bItem = cache.getItem(BLACKLIST_KEY + tag, String.class);
		String blacklisted = "N";
		if(bItem == null){
			Disjunction or = Restrictions.disjunction();
			or.add(Restrictions.eq("blacklistedItem", tag).ignoreCase());
			if(mac != null){
				logger.debug("MAC address to check blacklist status: " + mac);
				or.add(Restrictions.eq("macAddress", mac).ignoreCase());
			}
			KycBlacklist kb = dbService.getByCriteria(KycBlacklist.class, or, Restrictions.eq(ACTIVE, true));

			if(kb == null){
				cache.setItem(BLACKLIST_KEY + tag, "N", 10 * 60);//10mins
			}else{
				//				cache.setItem(tag + BLACKLIST_KEY + tag, kb, 60 * 60);
				cache.setItem(BLACKLIST_KEY + tag, "Y", 10 * 60);
				blacklisted = "Y";
			}
		}else{
			blacklisted = bItem;
		}
		return blacklisted;
	}

	public String getAgentStatus(String email){
		String deactivated = "N";
		if(email != null){
			KMUser user = getUser(email);
			if(user != null){
				if(user.isActive()){
					deactivated = "N";
				}else{
					deactivated = "Y";
				}
			}
		}
		return deactivated;
	}

	public void preloadBlacklist(){
		Long count = countOf(KycBlacklist.class, Restrictions.eq(ACTIVE, true));
		for(int z = 0; z <= count; z = z + 200){
			List<KycBlacklist> bis = getBlacklistedItems(z);
			for (KycBlacklist bi : bis) {
				cache.setItem(bi.getBlacklistedItem() + BLACKLIST_KEY + bi.getBlacklistedItem(), bi, 60 * 60);
				cache.setItem(BLACKLIST_KEY + bi.getBlacklistedItem(), "Y", 10 * 60);
			}
		}

	}

	public List<KycBlacklist> getBlacklistedItems(int index){
		QueryModifier qm = new QueryModifier(KycBlacklist.class);
		qm.setPaginated(index, 200);
		return dbService.getListByCriteria(KycBlacklist.class, qm, Restrictions.eq(ACTIVE, true));
	}

	private List<String> getUserRoles(Set<KMRole> roles){
		List<String> userRoles = new ArrayList<String>();
		if(roles != null && !roles.isEmpty()){
			for(KMRole kmr : roles){
				userRoles.add(kmr.getRole());
			}
		}
		return userRoles;
	}
	
	public AccessResponse performLogin(String email, String password, String tag){
            logger.info("User wants to login. Tag: " + tag + ", Email: " + email);
            AccessResponse ar = new AccessResponse();
            try {
                KMUser user = getUser(email);

                if (user == null) {
                    ar.setStatus(-1);
                    ar.setMessage("Invalid username or password entered.");

                    return ar;
                }

                PasswordPolicy pp = dbService.getByCriteria(PasswordPolicy.class, Restrictions.eq("policyActive", true));
                boolean isPasswordValid = user.isPasswordValid(password);

		// The line below sets the appropriate response message to the passed AccessResponse object.
                // If no message is returned, we consider this to have passed password policy, else, we return
                // For password expiration cases, we set the password syntax regex and guide and return it to client.
                checkPasswordPolicy(user, isPasswordValid, pp, ar);
                if (ar.getMessage() != null && !ar.getMessage().isEmpty()) {
                    return ar;
                }

                if (!isPasswordValid) {
                    // The line below updates the failed logins status of the user.
                    return updateUserPasswordPolicy(user, ar);
                }

                ar.setName(getUsername(user));

                //check if role-based login is enabled
                String roleBasedEnabled = appProps.getProperty("enable-role-based", "1"); //0 or 1
                if (roleBasedEnabled != null && roleBasedEnabled.equalsIgnoreCase("1")) {
                    if (tag == null || (tag != null && tag.trim().equalsIgnoreCase("TAG_NOT_SET"))) {
				//it means the kit has not been tagged
                        //so check if user has support role
//				if(!user.hasRole(KycManagerRole.SUPPORT.name())){
                        if (user.getPrivileges() != null && !user.hasPrivilege(KycPrivilege.TAGGING)) {
                            logger.debug("User, " + email + ", does not have TAGGING privilege");
                            ar.setStatus(-1);
                            ar.setMessage("Please contact support to tag this device");
                            return ar;
                        }
                    } else {
                        //check if kit is blacklisted
                        String bs = checkBlacklistStatus(tag, null);
                        if (bs != null && bs.equalsIgnoreCase("Y")) {
                            //kit is blacklisted
                            ar.setStatus(-3);
                            ar.setMessage("This kit is blacklisted. Please contact support");
                            return ar;
                        }
                    }

                    //check if agent is deactivated
                    if (!user.isActive()) {
                        ar.setStatus(-4);
                        ar.setMessage("Your account was deactivated. Please contact support");
                        return ar;
                    }

                    ar.setRoles(getUserRoles(user.getRoles())); //retain this for legacy machines

                    //if user has at least one privilege, then he can login
                    if (user.getPrivileges() != null && !user.getPrivileges().isEmpty()) {
                        updateSuccessfulLogin(user);
                        ar = getSuccessResponseObj(ar, pp, null);
                    } else {
                        ar.setStatus(-1);
                        ar.setMessage("User does not have the privileges required for login");
                        return ar;
                    }

			//android kits
//			if(tag != null && tag.trim().toUpperCase().startsWith("DROID")){
//				if(user.hasRole(KycManagerRole.SUPERVISOR.name()) || user.hasRole(KycManagerRole.CORPORATE_ROLE.name())
//						|| user.hasRole(KycManagerRole.SUPPORT.name()) || user.hasRole(KycManagerRole.AGENT.name())
//						|| user.hasRole(KycManagerRole.ADDITIONAL_SIM.name()) || user.hasRole(KycManagerRole.REGISTRATION_UPDATE.name())){
//
//					updateSuccessfulLogin( user );
//					ar = getSuccessResponseObj(ar, pp, null);
//				}else{
//					ar.setStatus(-1);
//					ar.setMessage("User does not have the required roles to login into this device");
//					return ar;
//				}
//			}else{
//				//other kits
//				if(user.hasRole(KycManagerRole.FOREIGN_NATIONAL_ROLE.name()) || user.hasRole(KycManagerRole.ADDITIONAL_SIM.name())
//						|| user.hasRole(KycManagerRole.SUPERVISOR.name()) || user.hasRole(KycManagerRole.CORPORATE_ROLE.name())
//						|| user.hasRole(KycManagerRole.SUPPORT.name()) || user.hasRole(KycManagerRole.AGENT.name())
//						|| user.hasRole(KycManagerRole.REGISTRATION_UPDATE.name())){
//
//					updateSuccessfulLogin(user);
//					ar = getSuccessResponseObj(ar, pp, null);
//				}else{
//					ar.setStatus(-1);
//					ar.setMessage("User does not have the required roles to login into this device");
//					return ar;
//				}
//			}
                    if (user.getClientFirstLogin() != null) {
                        ar.setFirstLogin(false);
                    } else {
                        updateSuccessfulLogin(user);
                        ar = getSuccessResponseObj(ar, pp, true);
                        return ar;
                    }
                } else {
                    updateSuccessfulLogin(user);
                    ar.setStatus(0);
                    ar.setMessage("Login successful");
                }

            } catch (NwormQueryException e) {
                logger.error("", e);
            }

		return ar;
	}

	private AccessResponse updateUserPasswordPolicy(KMUser user, AccessResponse ar){
		user.setFailedLoginAttempts( user.getFailedLoginAttempts() + 1 );
		user.setLastFailedLogin( new Timestamp( new Date().getTime() ) );                    
		getDbService().update(user);

		ar.setStatus(-1);
		ar.setMessage("Invalid username or password entered.");

		return ar;
	}

	private AccessResponse getSuccessResponseObj(AccessResponse ar, PasswordPolicy pp, Boolean firstLogin){
		ar.setStatus(0);
		ar.setMessage("Login successful");
		if(pp != null){
			ar.setPasswordSyntaxRegex( pp.getRegex() );
			ar.setPasswordSyntaxGuide( pp.getUserMessage() );
		}
		if(firstLogin != null){
			ar.setFirstLogin(firstLogin);
		}
		return ar;
	}

	private void updateSuccessfulLogin(KMUser user){
		// The line below ensures that failed login attempts count is reset to 0 each time a user has a successful login.
		// This is to ensure that failed logins are only considered in consecutive order.
		user.setFailedLoginAttempts( 0 );

		user.setLastSuccessfulLogin( new Timestamp( new Date().getTime() ) );
		getDbService().update(user);
	}

	/**
	 * Determines how many days records will stay on 
	 * the client before biosmart triggers a deletion
	 * @return
	 */
	private int getClientRecordsLifespan(){
		int duration = 0;
		String ls = cache.getItem("CLIENT-RECORDS-LIFESPAN", String.class);
		if(ls != null && !ls.isEmpty()){
			duration = Integer.valueOf(ls.trim());
		}else{
			//check db settings
			Setting setting = getDbService().getByCriteria(Setting.class, Restrictions.eq("name", "CLIENT-RECORDS-LIFESPAN").ignoreCase());
			if(setting != null && setting.getValue() != null){
				duration = Integer.valueOf(setting.getValue().trim());
				cache.setItem("CLIENT-RECORDS-LIFESPAN", duration + "", 60 * 60); //1hr
			}
		}

		logger.debug("Client records lifespan: " + duration);

		return duration;
	}

	@SuppressWarnings("unchecked")
	public SettingsResponse getGlobalSettings(){
                String defaultFalseValue = "false";
		SettingsResponse sr = new SettingsResponse();

		sr = getClientFieldSettings(sr);
		sr = getSimSwapSettings(sr);
                
		String r2 = "(abcde|bcdef|cdefg|defgh|efghi|fghij|ghijk|hijkl|ijklm|jklmn|klmno|lmnop|mnopq|nopqr|opqrs|pqrst|qrstu|rstuv|stuvw|tuvwx|uvwxy|vwxyz)";
		String heartBeatRate = getSettingValue("SC-HEARTBEAT-RATE","10");
		String maxMsisdn = getSettingValue("SC-MAX-MSISDN","5");
		String maxChildMsisdn = getSettingValue("MAX-CHILD-MSISDN","5");
		String regexOne = getSettingValue("REGEX-ONE","(\\\\w)\\\\1{3,}");
		String regexTwo = getSettingValue("REGEX-TWO",r2);
		String spoofData = getSettingValue("SC-SPOOF-DATA","1");
		String clientlockoutPeriod = getSettingValue("CLIENT-LOCKOUT-PERIOD", "30"); //in mins
		String clientAuditSyncInterval = getSettingValue("CLIENT-AUDIT-SYNC-INTERVAL", "30"); //in mins
		
		
		sr.setHeartbeatRate( (heartBeatRate == null) ? 10L : Long.valueOf(heartBeatRate) );//     appProps.getLong("sc-heartbeat-rate", 10L));
		sr.setMaxMsisdn( (maxMsisdn == null) ? 5 : Integer.valueOf(maxMsisdn) ); // appProps.getInt("sc-max-msisdn", 5));
		sr.setRegexOne( ((regexOne == null) || regexOne.isEmpty()) ? "(\\\\w)\\\\1{3,}" : regexOne);  //appProps.getProperty("regex-one", "(\\w)\\1{3,}"));
		sr.setRegexTwo( ((regexTwo == null) || regexTwo.isEmpty()) ? r2 : regexTwo ); //appProps.getProperty("regex-two", r2));
		sr.setSpoofData( ( (spoofData == null) ? 1 : Integer.valueOf(spoofData) ) == 1 );  //appProps.getInt("sc-spoof-data", 1) == 1);
		sr.setMaxChildMsisdn( (maxChildMsisdn == null) ? 5 : Integer.valueOf(maxChildMsisdn) ); // appProps.getInt("max-child-msisdn", 5));

		String defaultLocalIdTypes = "International Passport,Drivers License,National ID Card,Other";
		String defaultForeignIdTypes = "Residence Permit,Travel Document,National ID,National Drivers License";
		sr.setLocalIdTypes(getIdTypes("LOCAL_ID_TYPES", defaultLocalIdTypes));
		sr.setForeignIdTypes(getIdTypes("FOREIGN_ID_TYPES", defaultForeignIdTypes));
		sr.setClientRecordsLifespan(getClientRecordsLifespan());
		sr.setClientlockoutPeriod(Integer.valueOf(clientlockoutPeriod));
		sr.setClientAuditSyncInterval(Integer.valueOf(clientAuditSyncInterval));
                
                String signRegistration = getSettingValue("SIGN-REGISTRATION", defaultFalseValue);
                sr.setSignRegistration(Boolean.valueOf(signRegistration));
                String loginMode = getSettingValue("CLIENT-LOGIN-MODE", "FINGERPRINT, USERNAME");
                String loginModes [] = loginMode.split(",");
                if (loginModes != null) {
                    List<String> modes = new ArrayList<>();
                    for (String mode : loginModes) {
                        modes.add(mode.trim());
                    }
                    sr.setLoginMode(modes);
                }
                String otpRequired = getSettingValue(SettingsEnum.OTP_REQUIRED);
                sr.setOtpRequired(Boolean.valueOf(otpRequired));
                
                String offlineMode = getSettingValue(SettingsEnum.LOGIN_OFFLINE);
                sr.setLoginOffline(Boolean.valueOf(offlineMode));

                String offlineValidationType = getSettingValue(SettingsEnum.LOGIN_OFFLINE_VALIDATION_TYPE);
                sr.setOfflineValidationType(offlineValidationType);
                
                String airtimeSalesMandatory = getSettingValue(SettingsEnum.AIRTIME_SALES_MANDATORY);
                sr.setAirtimeSalesMandatory(Boolean.valueOf(airtimeSalesMandatory));                
                              
                String airtimeSalesURL = getSettingValue(SettingsEnum.AIRTIME_SALES_URL);
                sr.setAirtimeSalesURL(airtimeSalesURL);
                
                String minAcceptableCharacter = getSettingValue(SettingsEnum.MINIMUM_ACCEPTABLE_CHARACTER);
                sr.setMinimumAcceptableCharacter(Integer.valueOf(minAcceptableCharacter));
                
                String airtimeSalesEnabled = getSettingValue(SettingsEnum.ENABLE_VAS_MODULE);
                sr.setEnableVasModule(Boolean.valueOf(airtimeSalesEnabled));
                
                String availableUseCase = getSettingValue(SettingsEnum.AVAILABLE_USE_CASE);
                String availableUseCases[] = availableUseCase.split(",");
                if (availableUseCases != null) {
                    List<String> useCases = new ArrayList<>();
                    for (String uCase : availableUseCases) {
                        useCases.add(uCase);
                    }
                    sr.setAvailableUseCases(useCases);
                }
                
                String clientLogBatchSize = getSettingValue(SettingsEnum.CLIENT_ACTIVITY_LOG_BATCH_SIZE);
                try {
                    sr.setClientActivityLogBatchSize(Integer.valueOf(clientLogBatchSize));
                    sr.setMaximumMsisdnAllowedPerRegistration(Integer.valueOf(getSettingValue(SettingsEnum.MAXIMUM_MSISDN_ALLOWED_PER_REGISTRATION)));
                    
                    JAXBContext jc = JAXBContext.newInstance(InputComponents.class);
                    Marshaller marshaller = jc.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
                    marshaller.setProperty("com.sun.xml.bind.xmlDeclaration", Boolean.FALSE);
                    StringWriter sw = new StringWriter();
                    marshaller.marshal(thresholdUtil.unmarshal(InputComponents.class, ThresholdService.DYNAMIC_INPUT_NAME), sw);
                    String dynamicInputs = sw.toString();
                    sr.setDynamicInputs(dynamicInputs);
                } catch (NumberFormatException | JAXBException e) {
                    logger.error("", e);
                }
		return sr;
	}
	
	public SettingsResponse getSimSwapSettings(SettingsResponse sr){
		//sim swap settings
		String modeOfValidation = getSettingValue("SIM_SWAP_VALIDATION_MODE", "FINGERPRINT_AND_QUESTIONNAIRE");
		String allowableFpFailures = getSettingValue("SIM_SWAP_ALLOWABLE_FP_FAILURES", "3");
		String noOfCheckedMsisdns = getSettingValue("SIM_SWAP_NO_CHECKED_MSISDNS", "3");//for frequently dialed numbers
		String qValidation = getSettingValue("SS_QUESTIONNAIRE_VALIDATION", "FIRST_NAME:MANDATORY "
				+ "LAST_NAME:MANDATORY DATE_OF_BIRTH:MANDATORY LGA_OF_ORIGIN:MANDATORY LRC_AMOUNT:MANDATORY "
				+ "ACCOUNT_ID:MANDATORY INVOICE_AMOUNT:MANDATORY");//for validating questionnaire
		
//		logger.debug("Mapping for questionnaire validation: " + qValidation);

		//validate mode of validation
		SimSwapModes mode = SimSwapModes.valueOf(modeOfValidation);
                if(mode == null){
                        logger.error("Invalid setting config for sim swap mode of validation: " + modeOfValidation);
                        sr.setModeOfValidation("FINGERPRINT_AND_QUESTIONNAIRE"); //use default
                }else{
                        sr.setModeOfValidation(modeOfValidation);
                }

		//validate no. of allowable fp failures
		try{
			Integer.valueOf(allowableFpFailures);
			sr.setAllowableFpFailures(allowableFpFailures);
		}catch(NumberFormatException ex){
			logger.error("Invalid setting config for sim swap number of allowable failed fingerprint validations: " + allowableFpFailures);
			sr.setAllowableFpFailures("3");
		}
		
		//validate no. of checked msisdns
		try{
			sr.setMatchedMsisdns(Integer.valueOf(noOfCheckedMsisdns));
		}catch(NumberFormatException ex){
			logger.error("Invalid setting config for number of checked msisdns for frequently-dialed numbers: " + noOfCheckedMsisdns);
			sr.setMatchedMsisdns(3);
		}
		
		//construct mapping for validating questionnaire
		HashMap<String, String> questionnaireValidation = null;
		if(qValidation != null){
			questionnaireValidation = new HashMap<String, String>();
			for(String keyVal : qValidation.split(" ")){
				try{
					String[] configSplit = keyVal.split(":");
					//validate the key
					questionnaireValidation.put(configSplit[0], configSplit[1]);
				}catch(ArrayIndexOutOfBoundsException ex){
					logger.error("Error in constructing questionnaire validation config: ", ex);
				}
			}
		}
		
		sr.setQuestionnaireValidation(questionnaireValidation);
		
		return sr;
	}
	
	/**
	 * Retrieves config which determines if a 
	 * client field is optional or mandatory
	 * @param sr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private SettingsResponse getClientFieldSettings(SettingsResponse sr){
		HashMap<String, String> fieldValidation = null;
		String fvKey = "FIELD_VALIDATION_SETTINGS";

		//check if field validation map exists in cache
		Object val = cache.getItem(fvKey, Object.class);
		if(val != null){
			fieldValidation = (HashMap<String, String>) val;
		}else{
			String fValidation = getSettingValue("CLIENT_FIELD_SETTINGS", "KYC_FORM:MANDATORY MODE_OF_IDENTIFICATION:OPTIONAL");//to determine if fields are optional or mandatory
			if(fValidation != null && !fValidation.isEmpty()){
				fieldValidation = new HashMap<String, String>();
				for(String keyVal : fValidation.split(" ")){
					try{
						String[] configSplit = keyVal.split(":");
						fieldValidation.put(configSplit[0], configSplit[1]);
					}catch(ArrayIndexOutOfBoundsException ex){
						logger.error("Error in constructing field validation config: ", ex);
					}
				}
				//add field validation map to cache
				cache.setItem(fvKey, fieldValidation, 60 * 60); //1 hr
			}
		}

		sr.setClientFieldSettings(fieldValidation);

		return sr;
	}
	
	/**
	 * Retrieves the modes of identification to be sent
	 * to the client
	 * @param settingName, local or foreign settings
	 * @return
	 */
	private String getIdTypes(String settingName, String defaultValue){
		return getSettingValue(settingName,defaultValue);
	}

	public String getSettingValue(String settingName, String defaultValue){
		String val = cache.getItem(settingName, String.class);
		if(val == null){
			//check db settings
			Setting setting = getDbService().getByCriteria(Setting.class, Restrictions.eq("name", settingName).ignoreCase());
			if(setting != null && setting.getValue() != null){
				val = setting.getValue().trim();
			}else{
				val = defaultValue;
				setting = new Setting();
				setting.setName(settingName);
				setting.setValue(defaultValue);
				setting.setDescription(settingName);

				getDbService().create(setting);
			}
			cache.setItem(settingName, val, 60 * 60); //1 hr
		}

		return val;
	}
        
        public String getSettingValue(SettingsEnum settingsEnum){
		String val = cache.getItem(settingsEnum.getName(), String.class);
		if(val == null){
			//check db settings
                    try {
			Setting setting = getDbService().getByCriteria(Setting.class, Restrictions.eq("name", settingsEnum.getName()).ignoreCase());
			if(setting != null && setting.getValue() != null){
				val = setting.getValue().trim();
			}else{
				val = settingsEnum.getValue();
				setting = new Setting();
				setting.setName(settingsEnum.getName());
				setting.setValue(settingsEnum.getValue());
				setting.setDescription(settingsEnum.getDescription());

				getDbService().create(setting);
			}
                    } catch (NwormQueryException e) {
                        logger.error("", e);
                    }
			cache.setItem(settingsEnum.getName(), val, 60 * 60); //1 hr
		}

		return val;
	}

	/**
	 *
	 * @param ref
	 * @param type
	 * @param req
	 * @return
	 */
	public SettingsResponse getSettings(String ref, String type, ClientRefRequest req){

		SettingsResponse sr = getGlobalSettings();

		NodeData nd = cache.getItem("NODEDATA-" + req.getMac(), NodeData.class); // mac must not be null

		if(nd == null){
			// Retrieve node data from database
			nd = new NodeData();
			Map<String, Object> nodeData = getNodeData(req.getMac());
			if(nodeData.get("BROADCAST") != null){
				KycBroadcast kb = (KycBroadcast) nodeData.get("BROADCAST");
				nd.setServerMessage(kb.getMessage());
				sr.setServerMessage(kb.getMessage());
			}

			if(nodeData.get("NODE") != null){
				Node node = (Node) nodeData.get("NODE");

				if(node.getCorperateKit() != Boolean.TRUE){
					node.setCorperateKit(false); // just to ensure the value is false at the db layer
				}

				if(node.getLastInstalledUpdate() == null || (node.getLastInstalledUpdate() != null && !req.getPatchVersion().equals(node.getLastInstalledUpdate()))){
					node.setLastInstalledUpdate(req.getPatchVersion());
					node.setLastUpdated(new Date(req.getInstallDate().getTime()));

					node.setMachineManufacturer(req.getMachineManufacturer());
					node.setMachineModel(req.getMachineModel());
					node.setMachineOS(req.getMachineOS());
					node.setMachineSerialNumber(req.getMachineSerial());
					boolean success = dbService.update(node);
					logger.debug("Node update successful? " + success);

					VersionLog versionLog = new VersionLog();
					versionLog.setType(VersionType.KIT_VERSION);
					versionLog.setVersion(String.valueOf(req.getPatchVersion()));
					versionLog.setNode(node);
					dbService.create(versionLog);
				}

				nd.setCorporateKit(node.getCorperateKit());
				nd.setMacId(node.getMacAddress());
				Float version = node.getLastInstalledUpdate();
				nd.setPatchVersion(version);
				sr.setCorporateKit(nd.isCorporateKit());

				confirmUpdateAvailable(sr,type,version,req);

				// cache node data
				cache.setItem("NODEDATA-" + req.getMac(), nd, 30 * 60);
			}
		}else{
			sr.setServerMessage(nd.getServerMessage());
			sr.setCorporateKit(nd.isCorporateKit());

			confirmUpdateAvailable(sr,type,nd.getPatchVersion(),req);
		}
                
                sr.setCode(ResponseCodeEnum.SUCCESS);
                sr.setDescription("Success");
                
		return sr;
	}

	private void confirmUpdateAvailable(SettingsResponse sr,String type,Float patchVersion,ClientRefRequest req){
		Float cv = 0f;
		boolean useList = false;
		try{
			cv = appProps.getFloat("SC_"+ type, 1.23f); // type DROID, WIN, LITE
			useList = appProps.getBool("use-update-kit-list", false);
		}catch(NumberFormatException ex){
			ex.printStackTrace();
		}

//		logger.debug("current version: " + cv);
//		logger.debug("kit version: " + patchVersion );

		if( useList ){
			KitMarker kMarker = getDbService().getByCriteria( KitMarker.class, Restrictions.eq("macAddress", req.getMac().trim()) );
			if( (kMarker == null) ){
				sr.setUpdateAvailable(false);
				return;
			}

			setUpdateAvailable(sr,type,patchVersion,cv);

		}else{
			setUpdateAvailable(sr,type,patchVersion,cv);
		}
	}

	private void setUpdateAvailable(SettingsResponse sr,String type,Float patchVersion,float cv){
		if( (patchVersion != null) && (cv > patchVersion) ){
			sr.setUpdateAvailable(true);
			sr.setUpdateVersion(appProps.getFloat("SC_"+ type, 1.23f)); // type DROID, WIN, LITE
		}else{
			sr.setUpdateAvailable(false);
		}
	}

	private Map<String, Object> getNodeData(String macId){

		Map<String, Object> nodeData = new HashMap<String, Object>();

		Session sxn = dbService.getSessionService().getManagedSession();

		Criteria nc = sxn.createCriteria(Node.class);
		nc.add(Restrictions.eq("macAddress", macId));
		Object node = nc.uniqueResult();

		nodeData.put("NODE", node);
		nodeData.put("BROADCAST", null);
//		KycBroadcast brd = cache.getItem("GLOBALBROADCAST", KycBroadcast.class);
//		if(brd == null){
//			Criteria bc = sxn.createCriteria(KycBroadcast.class);
//			bc.add(Restrictions.eq("global", true));
//			bc.add(Restrictions.eq(ACTIVE, true));
//			Object broadcast = bc.uniqueResult();
//			if(broadcast != null){
//				nodeData.put("BROADCAST", broadcast);
//				cache.setItem("GLOBALBROADCAST", broadcast, 48 * 60 * 60);
//			}
//		}
		dbService.getSessionService().closeSession(sxn);
		return nodeData;
	}

	public void handleLicenseRequest(ClientRefRequest crr){

	}

	public AccessResponse authenticate(String auth) {
		AccessResponse ar = new AccessResponse();
		try {
			byte[] decodedParams = Base64.decode(auth);
			String params = new String(decodedParams);
			String uname = params.split(",")[0];
			String pw = params.split(",")[1];

			KMUser user = getUser(uname);
			if(user != null && user.isPasswordValid(pw) && user.isActive()){
				ar.setStatus(0);
				ar.setMessage(user.getPk() + "");
			}else{
				ar.setStatus(-1);
				ar.setMessage("Unknown credentials specified");
			}

		} catch (IOException | ArrayIndexOutOfBoundsException e) {
			ar.setMessage("Unknown credentials specified");
			logger.error("Exception ", e);
		}
		return ar;
	}

	private AccessResponse checkPasswordPolicy(KMUser kUser,boolean isPasswordValid, PasswordPolicy pp,AccessResponse ar) {

		if( (kUser == null) || (pp == null) || (ar == null) ){
			return ar;
		}

		if( pp.getLockoutEnabled() ){

			if( (kUser.isLockedOut() != null) && kUser.isLockedOut() ){
				Timestamp lastFailedLogin = kUser.getLastFailedLogin();
				Timestamp lastSuccessfulLogin = kUser.getLastSuccessfulLogin();
				if( (lastFailedLogin != null) && (lastSuccessfulLogin != null) && lastFailedLogin.after(lastSuccessfulLogin) ){ // This ensures that a person with a successful login after a failed login does not get logged out.

					Timestamp currentTime = new Timestamp(new Date().getTime());
					int difference = getTimeDifference(lastFailedLogin, currentTime, DatePart.MINUTE);
					if( difference < pp.getLockoutDuration() ){    
						ar.setStatus(-1);
						ar.setMessage("Your lockout wait period has not expired. Please try again later.");
						return ar;
					}else{
                        String allowAdminUnlockOnly = getSettingValue("ALLOW-ADMIN-UNLOCK-ONLY", "true");
						if( (allowAdminUnlockOnly != null ) && allowAdminUnlockOnly.equalsIgnoreCase("false") ){
							kUser.setFailedLoginAttempts(0);// resets the failed login attempts.
							kUser.setLockedOut(Boolean.FALSE);
							getDbService().update(kUser);
						}
					}

				}
			}

			int failedLoginCount = kUser.getFailedLoginAttempts() == null ? 0 : kUser.getFailedLoginAttempts();
			if( failedLoginCount >= pp.getMaximumFailure() ){ 
				// The line below marks the KMUser locked out status.
				ar.setStatus(-1);
				ar.setMessage("You have " + pp.getMaximumFailure() + " failed logins. Your account is now locked.");
				kUser.setLockedOut(Boolean.TRUE);
				getDbService().update(kUser);
				return ar;
			}            
		}

		if( pp.getPasswordExpirationEnabled() ){

			if( !isPasswordValid ){
				return updateUserPasswordPolicy(kUser, ar);
			}

			// We are not checking for null here as there should always be a last password change date. Remember user first login?
			Timestamp lastLogin = kUser.getLastPasswordChange();
			Timestamp currentTime = new Timestamp(new Date().getTime());
			int difference = getTimeDifference(lastLogin, currentTime, DatePart.DAY);
			if( difference > pp.getMaximumAge() ){   
				ar.setStatus(-7);
				ar.setName( getUsername(kUser) );
				ar.setRoles(getUserRoles(kUser.getRoles()));
				ar.setMessage("Your password has expired. Please reset your password");
				ar.setPasswordSyntaxRegex( pp.getRegex() );
				ar.setPasswordSyntaxGuide( pp.getUserMessage() );
				return ar;
			}            
		}

		return ar;
	}

	private String getUsername(KMUser kUser){
		String firstname = kUser.getFirstName() == null ? "" : kUser.getFirstName();
		String surname = kUser.getSurname() == null ? "" : kUser.getSurname();

		return firstname + " " + surname;
	}

	enum DatePart{DAY, HOUR, MINUTE,SECONDS, MILLISECONDS}

	private int getTimeDifference(Timestamp start, Timestamp end,DatePart datePart){

		if( (start == null) || (end == null) || (datePart == null) ) {
			return 0;
                }

		Long diffMs = end.getTime() - start.getTime();
		Long diffSec = diffMs / 1000;
		Long diffMin = diffSec / 60;
		Long diffHr = diffMin / 60;
		Long diffDays = diffHr / 24;

		switch( datePart ){
		case DAY :
			return diffDays.intValue();
		case HOUR :
			return diffHr.intValue();
		case MINUTE :
			return diffMin.intValue();
		case SECONDS :
			return diffSec.intValue();
		case MILLISECONDS :
			return diffMs.intValue();
		}

		return 0;
	}

        @SuppressWarnings("PMD") //needed to catch exception because a third party api
        public AccessResponseData requestLoginOtp(String email, AccessResponse accessResponse) {
            AccessResponseData responseData = new AccessResponseData();
            responseData.setCode(ResponseCodeEnum.ERROR);
            try {
                KMUser user = getUser(email);
                if (user == null || user.getMobile() == null || user.getMobile().isEmpty()) {
                    responseData.setCode(ResponseCodeEnum.FAILED_AUTHENTICATION);
                    return responseData;
                }

                try {   //s + ":" + otpToken + ":" + otpExpiration;
                    String resp = otpDS.generateOTP(OtpStatusRecordTypeEnum.LOGIN, user.getMobile());

                    logger.debug("otp response=" + resp);
                    String[] respSplit = resp.split(":");
                    if (respSplit[0] != null) {

                        //                boolean b = kSms.sendSMS(msisdn, "Your one-time password is " + respSplit[1] + ". It expires in " + respSplit[2] + " minutes." );
                        String message = "Your one-time password is " + respSplit[1] + ". It expires in " + respSplit[2] + " minutes.";
                        boolean sendSMS = kSms.sendSMS(user.getMobile(), message);
                        if (sendSMS) {
                            responseData.setCode(ResponseCodeEnum.SUCCESS);
                            responseData.setDescription("OTP was generated successfully.");
                            if (accessResponse != null) {
                                responseData.setFirstLogin(accessResponse.isFirstLogin());
                                responseData.setMsisdn(user.getMobile());
                                responseData.setName(accessResponse.getName());
                                responseData.setPasswordSyntaxGuide(accessResponse.getPasswordSyntaxGuide());
                                responseData.setPasswordSyntaxRegex(accessResponse.getPasswordSyntaxRegex());
                                responseData.setOtp(respSplit[1]);
                                responseData.setOtpExpirationTime(respSplit[2]);
                                    responseData.setPrivileges(user.getPrivileges());
                            }
                        } else {
                            responseData.setCode(ResponseCodeEnum.ERROR);
                            responseData.setDescription("Failed to generate/send OTP. Try again later.");
                        }
                    } else {
                        responseData.setCode(ResponseCodeEnum.ERROR);
                        responseData.setDescription("An error occurred while generating OTP. Please try again later.");
                    }

                    return responseData;

                } catch (Exception e) {
                    responseData.setCode(ResponseCodeEnum.ERROR);
                    responseData.setDescription(OTP_ERROR_MESSAGE);
                    logger.error("", e);
                }

                return responseData;

            } catch (ArrayIndexOutOfBoundsException e) {
                responseData.setCode(ResponseCodeEnum.ERROR);
                responseData.setDescription(OTP_ERROR_MESSAGE);
                logger.error("", e);
                return responseData;
            }

        }
	   
	public AccessResponse handleVerifyOtp(OtpStatusRecordTypeEnum otpStatusRecordTypeEnum, String mobile, String otp) { 
		AccessResponse accessResponse = new AccessResponse();

		if (otp == null || otp.isEmpty() || mobile == null || mobile.isEmpty()) {
			accessResponse.setStatus(-1);
			accessResponse.setMessage("Invalid username or phone number found.");
			return accessResponse; 
		}

		int code = 0;
                code = otpDS.verifyOtpStatus(otpStatusRecordTypeEnum, otp, mobile);
		String msg = "";
		switch (code) {
		case 1:
			msg = "The specified otp is valid.";
			break;
		case 2:
			msg = "This otp has expired.";
			break;
		case 3:
			msg = "There is no record associated the provided otp.";
			break;
		case 4:
			msg = "Both the msisdn and the OTP are required inputs. Note that msisdn must be 11 digits.";
			break;
		}
		accessResponse.setMessage(msg);
		accessResponse.setStatus(code);
		return accessResponse;
	}

    public AgentOnboardingResponseData handleValidateAgentEmailAddress(String emailAddress) {
        AgentOnboardingResponseData ar = new AgentOnboardingResponseData(ResponseCodeEnum.ERROR);
        try {
            KMUser user = getUser(emailAddress);

            if (user == null) {
                ar.setCode(ResponseCodeEnum.FAILED_AUTHENTICATION);
                ar.setDescription("Invalid email address was entered.");
                return ar;
            }
            if (!user.isActive()) {
                ar.setCode(ResponseCodeEnum.INACTIVE_ACCOUNT);
                ar.setDescription("Your account was deactivated. Please contact support");
                return ar;
            }
            
            OnboardingStatus os = dbService.getByCriteria(OnboardingStatus.class, Restrictions.eq("user", user));
//            Session session = null;
//            try {
//                session = dbService.getSessionService().getFactory().openSession();
//                Criteria criteria = session.createCriteria(OnboardingStatus.class);
//                criteria.add(Restrictions.eq("user", user));
//                logger.debug("query to retrieve onboarding details: " + criteria.toString());
//                os = (OnboardingStatus) criteria.uniqueResult();
//            } catch (HibernateException e) {
//                logger.error("", e);
//            } finally {
//                if (session != null) {
//                    session.close();
//                }
//            }
    		if(os != null && os.isOnboarded()){
    			ar.setCode(ResponseCodeEnum.ALREADY_ONBOARDED);
    			ar.setDescription("Agent already onboarded");
    			return ar;
    		}

            String resp = otpDS.generateOTP(OtpStatusRecordTypeEnum.EMAIL_VALIDATION, user.getMobile());
            String[] respSplit = resp.split(":");
            if (respSplit[0] != null) {
                String message = "Your one-time password is " + respSplit[1] + ". It expires in " + respSplit[2] + " minutes.";
                boolean sendSMS = kSms.sendSMS(user.getMobile(), message);
                if (sendSMS) {
                    ar.setCode(ResponseCodeEnum.SUCCESS);
                    ar.setEmail(emailAddress);
                    ar.setMsisdn(user.getMobile());
                    ar.setFirstName(user.getFirstName());
                    ar.setLastName(user.getSurname());
                    ar.setDescription("OTP was successfully generated.");
                } else {
                    ar.setCode(ResponseCodeEnum.ERROR);
                    ar.setDescription("Failed to generate/send OTP. Try again later.");
                }
            } else {
                ar.setCode(ResponseCodeEnum.ERROR);
                ar.setDescription(OTP_ERROR_MESSAGE);
            }

        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            logger.error("", e);
        }
        return ar;
    }
    
    /**
     * Fetches agent fingerprints for matching
     * on the client
     * @param email
     * @return
     */
    public FingerLoginResponse fetchAgentFingerprints(String email){
    	if(email == null || email.isEmpty()){
			return new FingerLoginResponse(ResponseCodeEnum.INVALID_INPUT, "No email address supplied");
		}
    	
		KMUser user = getUser(email.trim());
		if(user == null){
			return new FingerLoginResponse(ResponseCodeEnum.FAILED_AUTHENTICATION, "No agent was found with entered email address");
		}
		
		if(!user.isActive()){
			return new FingerLoginResponse(ResponseCodeEnum.INACTIVE_ACCOUNT, "Your Account has been deactivated");
		}
		
//		check if agent has been onboarded
		OnboardingStatus os = dbService.getByCriteria(OnboardingStatus.class, Restrictions.eq("user.pk", user.getPk()));
		if(os == null || !os.isOnboarded()){
			return new FingerLoginResponse(ResponseCodeEnum.ONBOARDING_PENDING, "Agent has not been onboarded yet");
		}
		
//		retrieve the agent's fingerprints from db
		List<AgentFingerprintPojo> fingers = new ArrayList<AgentFingerprintPojo>();
		List<AgentFingerprint> afs = dbService.getListByCriteria(AgentFingerprint.class, Restrictions.eq("user.pk", user.getPk()));
		if(afs != null && !afs.isEmpty()){
			for(AgentFingerprint af : afs){
				logger.debug("Adding finger: " + af.getFingerType().name());
				fingers.add(new AgentFingerprintPojo(af.getFingerType().name(), Base64.encodeBytes(af.getFingerprint())));
			}
		}
		
		FingerLoginResponse flr = new FingerLoginResponse(ResponseCodeEnum.SUCCESS, "Agent's fingerprints successfully retrieved");
		flr.setFingerprints(fingers);
		flr.setFirstName(user.getFirstName());
		flr.setSurname(user.getSurname());
		flr.setCached(isUserCacheable(user));
		flr.setEmail(email);
		
		flr.setPrivileges(user.getPrivileges());
		
		return flr;
    }
    
    public FetchPrivilegesResponse getPrivileges(String email){
		logger.info("Retrieving privileges for: " + email);
		FetchPrivilegesResponse resp = new FetchPrivilegesResponse();
		KMUser user = getUser(email);

		if(user == null){
			resp.setCode(ResponseCodeEnum.INVALID_INPUT);
			resp.setDescription("User with entered email address not found"); 
			return resp;
		}
		
		if(!user.isActive()){
			resp.setCode(ResponseCodeEnum.INACTIVE_ACCOUNT);
			resp.setDescription("User is blacklisted"); 
			return resp;
		}

		resp.setFirstName(user.getFirstName());
		resp.setSurname(user.getSurname());
		resp.setEmail(email);
		resp.setCached(isUserCacheable(user));
		resp.setPrivileges(user.getPrivileges());
		resp.setCode(ResponseCodeEnum.SUCCESS);
		resp.setDescription("Successfully retrieved user's privileges");
		
		return resp;
	}
	
	public boolean isUserCacheable(KMUser user){
		boolean cacheable = true;
		if(user != null){
			for(KMRole role : user.getRoles()){
				if(role.getAdmin() != null && !role.getAdmin()){
					cacheable = false;
					break;
				}
			}
		}
		return cacheable;
	}
}