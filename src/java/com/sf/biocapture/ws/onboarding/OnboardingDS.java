package com.sf.biocapture.ws.onboarding;

import java.util.Base64;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.criterion.Restrictions;

import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.onboarding.OnboardingStatus;
import com.sf.biocapture.entity.enums.FingersEnum;
import com.sf.biocapture.entity.onboarding.AgentFingerprint;
import com.sf.biocapture.entity.onboarding.AgentPassport;
import com.sf.biocapture.entity.security.KMUser;
import com.sf.biocapture.ws.ResponseCodeEnum;
import java.util.Date;
import nw.orm.core.exception.NwormQueryException;
/**
 * 
 * @author Nnanna
 *
 */
@Stateless
public class OnboardingDS extends DataService {
	public static final String ONBOARDING_KEY_PREFIX = "ONBOARDING_STATUS_";

	@Inject
	private BioCache cache;

	public OnboardingResponse saveAgentData(AgentData data){
		OnboardingResponse or = new OnboardingResponse();

                try {
                    //validate initiator email
                    KMUser adminUser = getUser(data.getOnboardedByEmailAddress());
                    if (adminUser == null) {
                            logger.info("Initiator user with email " + data.getOnboardedByEmailAddress() + " was not found!!!");
                            or = new OnboardingResponse(ResponseCodeEnum.FAILED_AUTHENTICATION, "Initiator user with email, " + data.getOnboardedByEmailAddress() + " was not found");
                            return or;
                    }                

                    //get user
                    KMUser user = getUser(data.getEmailAddress());
                    if(user != null){
                            if(!user.isActive()){
                                    logger.info("User with email " + data.getEmailAddress() + " is blacklisted!!");
                                    return new OnboardingResponse(ResponseCodeEnum.INACTIVE_ACCOUNT, "Agent is blacklisted");
                            }
                            OnboardingStatus os = dbService.getByCriteria(OnboardingStatus.class, Restrictions.eq("user.pk", user.getPk()));
                            if(os != null && os.isOnboarded()){
                                    logger.info("User with email " + data.getEmailAddress() + " has already been onboarded!!");
                                    return new OnboardingResponse(ResponseCodeEnum.ALREADY_ONBOARDED, "Agent already onboarded");
                            }

                            if(data.getFingerprints() != null && data.getFingerprints().size() < appProps.getInt("allowed-finger-count-onboarding", 10)){
                                    logger.error("Agent has incomplete fingerprints!!");
                                    return new OnboardingResponse(ResponseCodeEnum.INCOMPLETE_BIOMETRICS, "Agent must enrol at least " + appProps.getInt("allowed-finger-count-onboarding", 10) + " fingerprints");
                            }

                            try{
                                    //check if user already has biometrics
                                    AgentPassport portrait = new AgentPassport();
                                    portrait.setActive(true);
                                    portrait.setDeleted(false);
                                    portrait.setPassportData(Base64.getDecoder().decode(data.getPortrait().replaceAll("\\s*", "")));
                                    portrait.setUser(user);
                                    dbService.create(portrait);
                                    logger.debug("Done saving agent portrait " + data.getEmailAddress());

                                    //persist agent's fingerprints to db
                                    for(AgentFingerprintPojo fp : data.getFingerprints()){
                                            AgentFingerprint fingerprint = new AgentFingerprint();
                                            fingerprint.setActive(true);
                                            fingerprint.setDeleted(false);
                                            try{
                                                    fingerprint.setFingerprint(Base64.getDecoder().decode(fp.getFingerData().replaceAll("\\s*", "")));
                                                    fingerprint.setFingerType(FingersEnum.valueOf(fp.getFingerType()));
                                            }catch(IllegalArgumentException e){
                                                    logger.error("Error thrown in saving agent's fingerprint. Finger type is: " + fp.getFingerType());
                                                    continue;
                                            }
                                            fingerprint.setUser(user);
                                            dbService.create(fingerprint);
                                            logger.debug("Done saving agent fingerprint " + fingerprint.getFingerType().name());
                                    }

                                    //save onboarding status
                                    os = new OnboardingStatus();
                                    os.setUser(user);
                                    os.setOnboardedByUser(adminUser);
                                    os.setOnboardTimestamp(new Date(data.getOnboardDateValue()));
                                    os.setOnboarded(true);
                                    os.setActive(true);
                                    os.setDeleted(false);
                                    dbService.create(os);

                                    or = new OnboardingResponse(ResponseCodeEnum.SUCCESS, "Agent's biometrics saved successfully");
                                    //cache reference
                                    cache.setItem(ONBOARDING_KEY_PREFIX + data.getEmailAddress().replaceAll(" ", ""), "ONBOARDED", 60 * 10); //10mins
                            }catch(NwormQueryException ex){
                                    logger.error("Error in saving agent biometrics:", ex);
                                    return new OnboardingResponse(ResponseCodeEnum.ERROR, "A server error occurred while saving agent's biometrics");
                            }
                    }else{
                            logger.info("User with email " + data.getEmailAddress() + " was not found!!!");
                            or = new OnboardingResponse(ResponseCodeEnum.FAILED_AUTHENTICATION, "User with email, " + data.getEmailAddress() + ", not found");
                    }
                    
                } catch (NwormQueryException  e) {
                    logger.error("", e);
                }

		return or;
	}

	public OnboardingResponse getAgentOnboardingStatus(String agentEmail){
		//get user
		KMUser user = getUser(agentEmail);
		if(user == null){
			return new OnboardingResponse(ResponseCodeEnum.INVALID_INPUT, "User with email, " + agentEmail + ", not found");
		}
		if(!user.isActive()){
			return new OnboardingResponse(ResponseCodeEnum.INACTIVE_ACCOUNT, "User with email, " + agentEmail + ", is blacklisted");
		}
		OnboardingStatus os = dbService.getByCriteria(OnboardingStatus.class, Restrictions.eq("user.pk", user.getPk()));
		if(os != null && os.isOnboarded()){
			cache.setItem(ONBOARDING_KEY_PREFIX + agentEmail.replaceAll(" ", ""), "ONBOARDED", 60 * 10); //10mins
			return new OnboardingResponse(ResponseCodeEnum.SUCCESS, "Agent has been onboarded");
		}else{
			return new OnboardingResponse(ResponseCodeEnum.ONBOARDING_PENDING, "Agent has not been onboarded");
		}
	}
}
