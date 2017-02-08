package com.sf.biocapture.ws.settings;

import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.access.SettingsResponse;
import java.util.List;

/**
 *
 * @author Nnanna
 * @author Marcel
 * @since 23/01/2017
 *
 */
public class ClientSettings extends SettingsResponse {

    //NEW REGISTRATION MSISDN

    private boolean validateMsisdnNM = Boolean.TRUE;
    private boolean pukMandatoryNM = Boolean.FALSE;

    //NEW REGISTRATION SIM SERIAL
    private boolean validateSerialNS = Boolean.TRUE;
    private boolean pukMandatoryNS = Boolean.FALSE;

    //NEW REGISTRATION MSISDN+SERIAL
    private boolean validateNMS = Boolean.TRUE;
    private boolean pukMandatoryNMS = Boolean.FALSE;

    //DYNAMIC DEMOGRAPHICS
    private String dynamicInputs;

    //FIELD LENGTHS
    private int msisdnMinLength;
    private int msisdnMaxLength;
    private int serialMinLength;
    private int serialMaxLength;
    private int pukMinLength;
    private int pukMaxLength;

    //Biometric Validation
    private boolean portraitCaptureMandatory = Boolean.TRUE;
    private boolean portraitValidationMandatory = Boolean.TRUE;
    private boolean fingerprintCaptureMandatory = Boolean.TRUE;
    private boolean fingerprintValidationMandatory = Boolean.TRUE;

    private List<SettingFingerprintTypesEnum> fingerprintTypes;

    public ClientSettings() {
        setCode(ResponseCodeEnum.ERROR);
        setDescription(ResponseCodeEnum.ERROR.getDescription());
    }

    
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

    public boolean isValidateNMS() {
        return validateNMS;
    }

    public void setValidateNMS(boolean validateNMS) {
        this.validateNMS = validateNMS;
    }

    public boolean isPukMandatoryNMS() {
        return pukMandatoryNMS;
    }

    public void setPukMandatoryNMS(boolean pukMandatoryNMS) {
        this.pukMandatoryNMS = pukMandatoryNMS;
    }

    public String getDynamicInputs() {
        return dynamicInputs;
    }

    public void setDynamicInputs(String dynamicInputs) {
        this.dynamicInputs = dynamicInputs;
    }

    public void to(SettingsResponse sr) {
        //client fields
        setClientFieldSettings(sr.getClientFieldSettings());
        setRegexOne(sr.getRegexOne());
        setRegexTwo(sr.getRegexTwo());
        setSpoofData(sr.isSpoofData());
        setMinimumAcceptableCharacter(sr.getMinimumAcceptableCharacter());
        setDynamicInputs(sr.getDynamicInputs());

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
        setClientActivityLogBatchSize(sr.getClientActivityLogBatchSize());
        setLoginMode(sr.getLoginMode());
        setLoginOffline(sr.isLoginOffline());
        setOfflineValidationType(sr.getOfflineValidationType());
        setOtpRequired(sr.isOtpRequired());
        setAvailableUseCases(sr.getAvailableUseCases());
        setMaxMsisdn(sr.getMaxMsisdn());
        setMaximumMsisdnAllowedPerRegistration(sr.getMaximumMsisdnAllowedPerRegistration());

        //Registrations		
        setSignRegistration(sr.isSignRegistration());

        //intervals
        setNotificationsChecker(sr.getNotificationsChecker());
        setAgentBioSynchronizer(sr.getAgentBioSynchronizer());
        setAuditXmlSynchronizer(sr.getAuditXmlSynchronizer());
        setAuditSynchronizer(sr.getAuditSynchronizer());
        setThresholdUpdater(sr.getThresholdUpdater());
        setActivationChecker(sr.getActivationChecker());
        setSynchronizer(sr.getSynchronizer());
        setHarmonizer(sr.getHarmonizer());
        setSettingsService(sr.getSettingsService());
        setBlackLister(sr.getBlackLister());
        setHeartbeatRate(sr.getHeartbeatRate());
        setClientAuditSyncInterval(sr.getClientAuditSyncInterval());
    }

    public int getMsisdnMinLength() {
        return msisdnMinLength;
    }

    public void setMsisdnMinLength(int msisdnMinLength) {
        this.msisdnMinLength = msisdnMinLength;
    }

    public int getMsisdnMaxLength() {
        return msisdnMaxLength;
    }

    public void setMsisdnMaxLength(int msisdnMaxLength) {
        this.msisdnMaxLength = msisdnMaxLength;
    }

    public int getSerialMinLength() {
        return serialMinLength;
    }

    public void setSerialMinLength(int serialMinLength) {
        this.serialMinLength = serialMinLength;
    }

    public int getSerialMaxLength() {
        return serialMaxLength;
    }

    public void setSerialMaxLength(int serialMaxLength) {
        this.serialMaxLength = serialMaxLength;
    }

    public int getPukMinLength() {
        return pukMinLength;
    }

    public void setPukMinLength(int pukMinLength) {
        this.pukMinLength = pukMinLength;
    }

    public int getPukMaxLength() {
        return pukMaxLength;
    }

    public void setPukMaxLength(int pukMaxLength) {
        this.pukMaxLength = pukMaxLength;
    }

    public boolean isPortraitCaptureMandatory() {
        return portraitCaptureMandatory;
    }

    public void setPortraitCaptureMandatory(boolean portraitCaptureMandatory) {
        this.portraitCaptureMandatory = portraitCaptureMandatory;
    }

    public boolean isPortraitValidationMandatory() {
        return portraitValidationMandatory;
    }

    public void setPortraitValidationMandatory(boolean portraitValidationMandatory) {
        this.portraitValidationMandatory = portraitValidationMandatory;
    }

    public boolean isFingerprintValidationMandatory() {
        return fingerprintValidationMandatory;
    }

    public void setFingerprintValidationMandatory(boolean fingerprintValidationMandatory) {
        this.fingerprintValidationMandatory = fingerprintValidationMandatory;
    }
    public boolean isFingerprintCaptureMandatory() {
        return fingerprintCaptureMandatory;
    }

    public void setFingerprintCaptureMandatory(boolean fingerprintCaptureMandatory) {
        this.fingerprintCaptureMandatory = fingerprintCaptureMandatory;
    }

    public List<SettingFingerprintTypesEnum> getFingerprintTypes() {
        return fingerprintTypes;
    }

    public void setFingerprintTypes(List<SettingFingerprintTypesEnum> fingerprintTypes) {
        this.fingerprintTypes = fingerprintTypes;
    }

}
