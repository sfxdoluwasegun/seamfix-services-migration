package com.sf.biocapture.ws.settings;

import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.ResponseData;
import com.sf.biocapture.ws.access.SettingsResponse;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Nnanna
 * @author Marcel
 * @since 23/01/2017
 *
 */
public class ClientSettings extends ResponseData {

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

    private int maxMsisdn;
    private long heartbeatRate;

    private int clientRecordsLifespan;
    private int clientAuditSyncInterval;
    private String localIdTypes;
    private String foreignIdTypes;

    //sim swap settings
    /**
     * fingerprint and/or questionnaire validation
     */
    private String modeOfValidation;

    /**
     * no of failed fingerprint validation allowed on the client
     */
    private String allowableFpFailures;

    /**
     * no of frequently dialed numbers that must be matched for sim swap to
     * continue
     */
    private int matchedMsisdns = 3;

    private HashMap<String, String> questionnaireValidation = null;

    private int maxChildMsisdn;

    /**
     * no of minutes a client is allowed to stay idle before user is forced to
     * login again
     */
    private int clientlockoutPeriod;

    private HashMap<String, String> clientFieldSettings = null;

    private boolean signRegistration;
    private boolean otpRequired;
    private List<String> loginMode;
    private boolean loginOffline;
    private String offlineValidationType;
    private boolean airtimeSalesMandatory;
    private String airtimeSalesURL;
    private List<String> availableUseCases;
    private int clientActivityLogBatchSize = 20;
    private int maximumMsisdnAllowedPerRegistration = 5;
    private boolean enableVasModule = true;
    private int minimumAcceptableCharacter;

    /**
     * *SERVICE INTERVALS**
     */
    private int notificationsChecker;
    private int agentBioSynchronizer;
    private int auditXmlSynchronizer;
    private int thresholdUpdater;
    private int auditSynchronizer;
    private int activationChecker;
    private int synchronizer;
    private int harmonizer;
    private int settingsService;
    private int blackLister;

    public ClientSettings() {
        setCode(ResponseCodeEnum.ERROR);
        setDescription(ResponseCodeEnum.ERROR.getDescription());
    }

    public void to(SettingsResponse sr) {
        if (sr == null) {
            return;
        }
        //client fields
        clientFieldSettings = sr.getClientFieldSettings();
        minimumAcceptableCharacter = sr.getMinimumAcceptableCharacter();
        dynamicInputs = sr.getDynamicInputs();

        //sim swap
        modeOfValidation = sr.getModeOfValidation();
        allowableFpFailures = sr.getAllowableFpFailures();
        matchedMsisdns = sr.getMatchedMsisdns();
        questionnaireValidation = sr.getQuestionnaireValidation();

        //VAS
        airtimeSalesMandatory = sr.getAirtimeSalesMandatory();
        airtimeSalesURL = sr.getAirtimeSalesURL();
        enableVasModule = sr.isEnableVasModule();

        //core
        clientlockoutPeriod = sr.getClientlockoutPeriod();
        clientActivityLogBatchSize = sr.getClientActivityLogBatchSize();
        loginMode = sr.getLoginMode();
        loginOffline = sr.isLoginOffline();
        offlineValidationType = sr.getOfflineValidationType();
        otpRequired = sr.isOtpRequired();
        availableUseCases = sr.getAvailableUseCases();
        maxMsisdn = sr.getMaxMsisdn();
        maximumMsisdnAllowedPerRegistration = sr.getMaximumMsisdnAllowedPerRegistration();

        //Registrations		
        signRegistration = sr.isSignRegistration();

        //intervals
        notificationsChecker = sr.getNotificationsChecker();
        agentBioSynchronizer = sr.getAgentBioSynchronizer();
        auditXmlSynchronizer = sr.getAuditXmlSynchronizer();
        auditSynchronizer = sr.getAuditSynchronizer();
        thresholdUpdater = sr.getThresholdUpdater();
        activationChecker = sr.getActivationChecker();
        synchronizer = sr.getSynchronizer();
        harmonizer = sr.getHarmonizer();
        settingsService = sr.getSettingsService();
        blackLister = sr.getBlackLister();
        heartbeatRate = sr.getHeartbeatRate();
        clientAuditSyncInterval = sr.getClientAuditSyncInterval();
        clientRecordsLifespan = sr.getClientRecordsLifespan();
        clientlockoutPeriod = sr.getClientlockoutPeriod();

        //
        localIdTypes = sr.getLocalIdTypes();
        foreignIdTypes = sr.getForeignIdTypes();
        maxChildMsisdn = sr.getMaxChildMsisdn();

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

    public int getMaxMsisdn() {
        return maxMsisdn;
    }

    public void setMaxMsisdn(int maxMsisdn) {
        this.maxMsisdn = maxMsisdn;
    }

    public long getHeartbeatRate() {
        return heartbeatRate;
    }

    public void setHeartbeatRate(long heartbeatRate) {
        this.heartbeatRate = heartbeatRate;
    }

    public int getClientRecordsLifespan() {
        return clientRecordsLifespan;
    }

    public void setClientRecordsLifespan(int clientRecordsLifespan) {
        this.clientRecordsLifespan = clientRecordsLifespan;
    }

    public int getClientAuditSyncInterval() {
        return clientAuditSyncInterval;
    }

    public void setClientAuditSyncInterval(int clientAuditSyncInterval) {
        this.clientAuditSyncInterval = clientAuditSyncInterval;
    }

    public String getLocalIdTypes() {
        return localIdTypes;
    }

    public void setLocalIdTypes(String localIdTypes) {
        this.localIdTypes = localIdTypes;
    }

    public String getForeignIdTypes() {
        return foreignIdTypes;
    }

    public void setForeignIdTypes(String foreignIdTypes) {
        this.foreignIdTypes = foreignIdTypes;
    }

    public String getModeOfValidation() {
        return modeOfValidation;
    }

    public void setModeOfValidation(String modeOfValidation) {
        this.modeOfValidation = modeOfValidation;
    }

    public String getAllowableFpFailures() {
        return allowableFpFailures;
    }

    public void setAllowableFpFailures(String allowableFpFailures) {
        this.allowableFpFailures = allowableFpFailures;
    }

    public int getMatchedMsisdns() {
        return matchedMsisdns;
    }

    public void setMatchedMsisdns(int matchedMsisdns) {
        this.matchedMsisdns = matchedMsisdns;
    }

    public HashMap<String, String> getQuestionnaireValidation() {
        return questionnaireValidation;
    }

    public void setQuestionnaireValidation(HashMap<String, String> questionnaireValidation) {
        this.questionnaireValidation = questionnaireValidation;
    }

    public int getMaxChildMsisdn() {
        return maxChildMsisdn;
    }

    public void setMaxChildMsisdn(int maxChildMsisdn) {
        this.maxChildMsisdn = maxChildMsisdn;
    }

    public int getClientlockoutPeriod() {
        return clientlockoutPeriod;
    }

    public void setClientlockoutPeriod(int clientlockoutPeriod) {
        this.clientlockoutPeriod = clientlockoutPeriod;
    }

    public HashMap<String, String> getClientFieldSettings() {
        return clientFieldSettings;
    }

    public void setClientFieldSettings(HashMap<String, String> clientFieldSettings) {
        this.clientFieldSettings = clientFieldSettings;
    }

    public boolean isSignRegistration() {
        return signRegistration;
    }

    public void setSignRegistration(boolean signRegistration) {
        this.signRegistration = signRegistration;
    }

    public boolean isOtpRequired() {
        return otpRequired;
    }

    public void setOtpRequired(boolean otpRequired) {
        this.otpRequired = otpRequired;
    }

    public List<String> getLoginMode() {
        return loginMode;
    }

    public void setLoginMode(List<String> loginMode) {
        this.loginMode = loginMode;
    }

    public boolean isLoginOffline() {
        return loginOffline;
    }

    public void setLoginOffline(boolean loginOffline) {
        this.loginOffline = loginOffline;
    }

    public String getOfflineValidationType() {
        return offlineValidationType;
    }

    public void setOfflineValidationType(String offlineValidationType) {
        this.offlineValidationType = offlineValidationType;
    }

    public boolean isAirtimeSalesMandatory() {
        return airtimeSalesMandatory;
    }

    public void setAirtimeSalesMandatory(boolean airtimeSalesMandatory) {
        this.airtimeSalesMandatory = airtimeSalesMandatory;
    }

    public String getAirtimeSalesURL() {
        return airtimeSalesURL;
    }

    public void setAirtimeSalesURL(String airtimeSalesURL) {
        this.airtimeSalesURL = airtimeSalesURL;
    }

    public List<String> getAvailableUseCases() {
        return availableUseCases;
    }

    public void setAvailableUseCases(List<String> availableUseCases) {
        this.availableUseCases = availableUseCases;
    }

    public int getClientActivityLogBatchSize() {
        return clientActivityLogBatchSize;
    }

    public void setClientActivityLogBatchSize(int clientActivityLogBatchSize) {
        this.clientActivityLogBatchSize = clientActivityLogBatchSize;
    }

    public int getMaximumMsisdnAllowedPerRegistration() {
        return maximumMsisdnAllowedPerRegistration;
    }

    public void setMaximumMsisdnAllowedPerRegistration(int maximumMsisdnAllowedPerRegistration) {
        this.maximumMsisdnAllowedPerRegistration = maximumMsisdnAllowedPerRegistration;
    }

    public boolean isEnableVasModule() {
        return enableVasModule;
    }

    public void setEnableVasModule(boolean enableVasModule) {
        this.enableVasModule = enableVasModule;
    }

    public int getMinimumAcceptableCharacter() {
        return minimumAcceptableCharacter;
    }

    public void setMinimumAcceptableCharacter(int minimumAcceptableCharacter) {
        this.minimumAcceptableCharacter = minimumAcceptableCharacter;
    }

    public int getNotificationsChecker() {
        return notificationsChecker;
    }

    public void setNotificationsChecker(int notificationsChecker) {
        this.notificationsChecker = notificationsChecker;
    }

    public int getAgentBioSynchronizer() {
        return agentBioSynchronizer;
    }

    public void setAgentBioSynchronizer(int agentBioSynchronizer) {
        this.agentBioSynchronizer = agentBioSynchronizer;
    }

    public int getAuditXmlSynchronizer() {
        return auditXmlSynchronizer;
    }

    public void setAuditXmlSynchronizer(int auditXmlSynchronizer) {
        this.auditXmlSynchronizer = auditXmlSynchronizer;
    }

    public int getThresholdUpdater() {
        return thresholdUpdater;
    }

    public void setThresholdUpdater(int thresholdUpdater) {
        this.thresholdUpdater = thresholdUpdater;
    }

    public int getAuditSynchronizer() {
        return auditSynchronizer;
    }

    public void setAuditSynchronizer(int auditSynchronizer) {
        this.auditSynchronizer = auditSynchronizer;
    }

    public int getActivationChecker() {
        return activationChecker;
    }

    public void setActivationChecker(int activationChecker) {
        this.activationChecker = activationChecker;
    }

    public int getSynchronizer() {
        return synchronizer;
    }

    public void setSynchronizer(int synchronizer) {
        this.synchronizer = synchronizer;
    }

    public int getHarmonizer() {
        return harmonizer;
    }

    public void setHarmonizer(int harmonizer) {
        this.harmonizer = harmonizer;
    }

    public int getSettingsService() {
        return settingsService;
    }

    public void setSettingsService(int settingsService) {
        this.settingsService = settingsService;
    }

    public int getBlackLister() {
        return blackLister;
    }

    public void setBlackLister(int blackLister) {
        this.blackLister = blackLister;
    }

}
