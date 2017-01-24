package com.sf.biocapture.ws.settings;

import com.sf.biocapture.ws.access.SettingsResponse;

/**
 * 
 * @author Nnanna
 * @since 23/01/2017
 * 
 */
public class ClientSettings extends SettingsResponse{
	//NEW REGISTRATION MSISDN
	private boolean validateMsisdnNM = Boolean.TRUE;
	private boolean pukMandatoryNM = Boolean.FALSE;
	
	//NEW REGISTRATION SIM SERIAL
	private boolean validateSerialNS = Boolean.TRUE;
	private boolean pukMandatoryNS = Boolean.FALSE;
	
	//NEW REGISTRATION MSISDN+SERIAL
	private boolean validateMsisdnNMS = Boolean.TRUE;
	private boolean validateSerialNMS = Boolean.TRUE;
	private boolean pukMandatoryNMS = Boolean.FALSE;

	public boolean isValidateMsisdnNM() {
		return validateMsisdnNM;
	}

	public void setValidateMsisdnNM(boolean validateMsisdnNM) {
		this.validateMsisdnNM = validateMsisdnNM;
	}

	public boolean isPukMandatoryNM() {
		return pukMandatoryNM;
	}

	public void setPukMandatoryNM(boolean pukMandatoryNM) {
		this.pukMandatoryNM = pukMandatoryNM;
	}

	public boolean isValidateSerialNS() {
		return validateSerialNS;
	}

	public void setValidateSerialNS(boolean validateSerialNS) {
		this.validateSerialNS = validateSerialNS;
	}

	public boolean isPukMandatoryNS() {
		return pukMandatoryNS;
	}

	public void setPukMandatoryNS(boolean pukMandatoryNS) {
		this.pukMandatoryNS = pukMandatoryNS;
	}

	public boolean isValidateMsisdnNMS() {
		return validateMsisdnNMS;
	}

	public void setValidateMsisdnNMS(boolean validateMsisdnNMS) {
		this.validateMsisdnNMS = validateMsisdnNMS;
	}

	public boolean isValidateSerialNMS() {
		return validateSerialNMS;
	}

	public void setValidateSerialNMS(boolean validateSerialNMS) {
		this.validateSerialNMS = validateSerialNMS;
	}

	public boolean isPukMandatoryNMS() {
		return pukMandatoryNMS;
	}

	public void setPukMandatoryNMS(boolean pukMandatoryNMS) {
		this.pukMandatoryNMS = pukMandatoryNMS;
	}
	
	public void to(SettingsResponse sr){
		//client fields
		setClientFieldSettings(sr.getClientFieldSettings());
		setRegexOne(sr.getRegexOne());
		setRegexTwo(sr.getRegexTwo());
		setSpoofData(sr.isSpoofData());
		setMinimumAcceptableCharacter(sr.getMinimumAcceptableCharacter());
		
		//sim swap
		setModeOfValidation(sr.getModeOfValidation());
		setAllowableFpFailures(sr.getAllowableFpFailures());
		setMatchedMsisdns(sr.getMatchedMsisdns());
		setQuestionnaireValidation(sr.getQuestionnaireValidation());
		
		//VAS
		setAirtimeSalesMandatory(sr.getAirtimeSalesMandatory());
		setAirtimeSalesURL(sr.getAirtimeSalesURL());
		setEnableVasModule(sr.isEnableVasModule());
		
		//core
		setClientlockoutPeriod(sr.getClientlockoutPeriod());
		setClientAuditSyncInterval(sr.getClientAuditSyncInterval());
		setClientActivityLogBatchSize(sr.getClientActivityLogBatchSize());
		setLoginMode(sr.getLoginMode());
		setLoginOffline(sr.isLoginOffline());
		setOfflineValidationType(sr.getOfflineValidationType());
		setOtpRequired(sr.isOtpRequired());
		setHeartbeatRate(sr.getHeartbeatRate());
		setAvailableUseCases(sr.getAvailableUseCases());
		setMaxMsisdn(sr.getMaxMsisdn());
		setMaximumMsisdnAllowedPerRegistration(sr.getMaximumMsisdnAllowedPerRegistration());
		
		//Registrations		
		setSignRegistration(sr.isSignRegistration());
	}
}