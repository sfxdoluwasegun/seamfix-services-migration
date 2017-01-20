package com.sf.biocapture.ws.access;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import nw.commons.StopWatch;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.entity.enums.OtpStatusRecordTypeEnum;
import com.sf.biocapture.entity.enums.SettingsEnum;
import com.sf.biocapture.entity.security.KMUser;
import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.ResponseData;
import com.sf.biocapture.ws.tags.ClientRefRequest;
import com.sf.biocapture.ws.tags.TagsDS;
import nw.orm.core.exception.NwormQueryException;

@Path("/access")
public class AccessService extends BsClazz implements IAccessService{
    
        private static final String LITE = "lite";
        private static final String SMART_CLIENT = "smartclient";
        private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a");

	@Inject
	private AccessDS accessDS;
	
	@Inject
	private TagsDS tagsDS;        

	@Override
	@RolesAllowed({SMART_CLIENT, LITE})
	public Response blacklist(String tag) {
		StopWatch sw = new StopWatch(true);
		String response = accessDS.checkBlacklistStatus(tag, null);
		logger.debug("BLACKLIST STATUS for: " + tag + " " + response + " proc: " + sw.elapsedTime() + "[ms] ");

		return Response.status(Status.OK).entity(response).build();
	}

	@Override
	@RolesAllowed({SMART_CLIENT, LITE})
	public AccessResponse login(String email, String password, String tag) {
		AccessResponse response = accessDS.performLogin(email, password, tag);
		return response;
	}
	
	@Override
	@RolesAllowed({SMART_CLIENT, LITE})
	public AccessResponse login(String email, String password, String msisdn,
			String tag) {
		return login(email, password, tag); //msisdn is not in use anymore
	}

	@Override
	@RolesAllowed({SMART_CLIENT, LITE})
	public AccessResponse changePassword(String email, String password) {
		return accessDS.doPasswordReset(email, password);
	}

	@Override
	@RolesAllowed({SMART_CLIENT, LITE})
	public Response settings(String ref, String type, ClientRefRequest req) {

		SettingsResponse sr = new SettingsResponse();
		if(req.getMac() != null){
			sr = accessDS.getSettings(ref, type, req);
		}
		return Response.status(Status.OK).entity(sr).build();
	}

	@Override
	@RolesAllowed({"kycdroid", "kyclive"})
	public AccessResponse login(HttpServletRequest req) {
		String auth = req.getHeader("KYC-Authentication");
		return accessDS.authenticate(auth);
	}

	@Override
	public Response blacklist(String tag, String email) {
		return blacklist(tag, email, null);
	}

	@Override
	public Response blacklist(String tag, String email, String mac) {
		AgentResponse resp = new AgentResponse();
		String kStatus = accessDS.checkBlacklistStatus(tag, mac);
		resp.setTag(tag);
		resp.setKitStatus(kStatus);
		
		logger.debug("BLACKLIST - " + tag + ": " + kStatus + "; mac: " + mac);
		
		if(email != null){
			String aStatus = accessDS.getAgentStatus(email);
			resp.setAgentStatus(aStatus);
			
			logger.debug("AGENT - " + email + ": " + aStatus);
		}
		
		return Response.status(Status.OK).entity(resp).build();
	}

	@Override
        @RolesAllowed({SMART_CLIENT, LITE})
	public AccessResponse loginClientTime(String email, String password, String tag, String clientTime) {
		logger.debug("**** Inside the login client time. Client time is : " + clientTime);
		if(!tagsDS.clientTimeIsCorrect(clientTime)){
			AccessResponse ar = new AccessResponse();
			ar.setStatus(-1);
                        ar.setMessage("Please correct your system time. Server time is " + sdf.format(new Date()));
			return ar;
		}
		logger.debug("time difference passed....");

		return login(email, password, tag);
	}
        
       
	@Override
        @RolesAllowed({SMART_CLIENT, LITE})
	public ResponseData otpLogin(String email, String password,
			String tag, String clientTime) {		
                AccessResponseData responseData = new AccessResponseData();
                responseData.setCode(ResponseCodeEnum.ERROR);
		AccessResponse accessResponse = loginClientTime(email, password, tag, clientTime);
                boolean otpRequired = false;
                try {
                    otpRequired = Boolean.valueOf(accessDS.getSettingValue(SettingsEnum.OTP_REQUIRED));
                } catch (NumberFormatException e) {
                    logger.error("", e);
                }catch (NwormQueryException e) {
                    logger.error("", e);
                }
                if (accessResponse.getStatus() == 0) {
                    if (otpRequired) {
                        responseData = accessDS.requestLoginOtp(email, accessResponse);
                    } else {                        
                        responseData.setCode(ResponseCodeEnum.SUCCESS);
                        responseData.from(accessResponse);
                    } 
                    KMUser user = accessDS.getKmUserByEmail(email);
                    if(user != null){
                        responseData.setPrivileges(user.getPrivileges());
                        responseData.setCached(accessDS.isUserCacheable(user));
                    }
                }   else {
                	if(accessResponse.getStatus() == -4){
                		responseData.setCode(ResponseCodeEnum.INACTIVE_ACCOUNT);
                	}else{
                		responseData.setCode(ResponseCodeEnum.ERROR);
                	}
                    responseData.from(accessResponse);
                }            
                responseData.setOtpRequired(otpRequired);
                return responseData;
	}

        @SuppressWarnings("CPD-START")
        @Override
        @RolesAllowed({SMART_CLIENT, LITE})
        public ResponseData verifyOtpLogin(String mobile, String otp) {
            AccessResponse accessResponse = accessDS.handleVerifyOtp(OtpStatusRecordTypeEnum.LOGIN, mobile, otp);
            AccessResponseData responseData = new AccessResponseData();
            responseData.setCode(ResponseCodeEnum.ERROR);
            if (accessResponse != null) {
                switch (accessResponse.getStatus()) {
                    case 1:
                        responseData.setCode(ResponseCodeEnum.SUCCESS);
                        responseData.setDescription("The specified OTP is valid.");
                        break;
                    case 2:
                        responseData.setCode(ResponseCodeEnum.ERROR);
                        responseData.setDescription("This otp has expired.");
                        break;
                    case 3:
                        responseData.setCode(ResponseCodeEnum.ERROR);
                        responseData.setDescription("No match was found for the specified OTP.");
                        break;
                    case 4:
                        responseData.setCode(ResponseCodeEnum.INVALID_INPUT);
                        responseData.setDescription("Both the msisdn and the OTP are required inputs. Note that msisdn must be 11 digits.");
                        break;
                }
                responseData.setFirstLogin(accessResponse.isFirstLogin());
                responseData.setMsisdn(mobile);
                responseData.setName(accessResponse.getName());
                responseData.setPasswordSyntaxGuide(accessResponse.getPasswordSyntaxGuide());
                responseData.setPasswordSyntaxRegex(accessResponse.getPasswordSyntaxRegex());                
            }
            return responseData;
        }

        @Override
        @RolesAllowed({SMART_CLIENT, LITE})
        public AgentOnboardingResponseData validateAgentEmail(String emailAddress) {
            return accessDS.handleValidateAgentEmailAddress(emailAddress);
        }

        @SuppressWarnings("CPD-START")
        @Override
        @RolesAllowed({SMART_CLIENT, LITE})
        public AgentOnboardingResponseData validateAgentOtp(String mobile, String otp) {
            AccessResponse accessResponse = accessDS.handleVerifyOtp(OtpStatusRecordTypeEnum.EMAIL_VALIDATION, mobile, otp);
            AgentOnboardingResponseData responseData = new AgentOnboardingResponseData();
                responseData.setCode(ResponseCodeEnum.ERROR);
                if (accessResponse != null) {
                    switch (accessResponse.getStatus()) {
                        case 1:
                            responseData.setCode(ResponseCodeEnum.SUCCESS);
                            responseData.setDescription("The specified OTP is valid.");
                            break;
                        case 2:
                            responseData.setCode(ResponseCodeEnum.ERROR);
                            responseData.setDescription("This otp has expired.");
                            break;
                        case 3:
                            responseData.setCode(ResponseCodeEnum.ERROR);
                            responseData.setDescription("No match was found for the specified OTP.");
                            break;
                        case 4:
                            responseData.setCode(ResponseCodeEnum.INVALID_INPUT);
                            responseData.setDescription("Both the msisdn and the OTP are required inputs. Note that msisdn must be 11 digits.");
                            break;
                    }
                    responseData.setMsisdn(mobile);
                }
            return responseData;
        }

		@Override
		public FingerLoginResponse fetchAgentFingerprints(String agentEmail) {
			return accessDS.fetchAgentFingerprints(agentEmail);
		}

		@Override
		public FetchPrivilegesResponse getUserPrivileges(String email) {
			return accessDS.getPrivileges(email);
		}
}