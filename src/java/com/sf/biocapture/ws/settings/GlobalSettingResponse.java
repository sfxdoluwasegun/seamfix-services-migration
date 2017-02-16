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
public class GlobalSettingResponse extends ResponseData {

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
     * no of frequently dialled numbers that must be matched for SIM swap to
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
    private int notificationsCheckerInterval;
    private int agentBioSynchronizerInterval;
    private int auditXmlSynchronizerInterval;
    private int thresholdUpdaterInterval;
    private int auditSynchronizerInterval;
    private int activationCheckerInterval;
    private int synchronizerInterval;
    private int harmonizerInterval;
    private int settingsServiceInterval;
    private int blackListerInterval;
    private int otaInterval;
    private int clientSettingInterval;

    public GlobalSettingResponse() {
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
        notificationsCheckerInterval = sr.getNotificationsCheckerInterval();
        agentBioSynchronizerInterval = sr.getAgentBioSynchronizerInterval();
        auditXmlSynchronizerInterval = sr.getAuditXmlSynchronizerInterval();
        auditSynchronizerInterval = sr.getAuditSynchronizerInterval();
        thresholdUpdaterInterval = sr.getThresholdUpdaterInterval();
        activationCheckerInterval = sr.getActivationCheckerInterval();
        synchronizerInterval = sr.getSynchronizerInterval();
        harmonizerInterval = sr.getHarmonizerInterval();
        settingsServiceInterval = sr.getSettingsServiceInterval();
        blackListerInterval = sr.getBlackListerInterval();
        otaInterval = sr.getOtaInterval();
        heartbeatRate = sr.getHeartbeatRate();
        clientAuditSyncInterval = sr.getClientAuditSyncInterval();
        clientRecordsLifespan = sr.getClientRecordsLifespan();
        clientlockoutPeriod = sr.getClientlockoutPeriod();
        //
        localIdTypes = sr.getLocalIdTypes();
        foreignIdTypes = sr.getForeignIdTypes();
        maxChildMsisdn = sr.getMaxChildMsisdn();

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

    public int getNotificationsCheckerInterval() {
        return notificationsCheckerInterval;
    }

    public void setNotificationsCheckerInterval(int notificationsCheckerInterval) {
        this.notificationsCheckerInterval = notificationsCheckerInterval;
    }

    public int getAgentBioSynchronizerInterval() {
        return agentBioSynchronizerInterval;
    }

    public void setAgentBioSynchronizerInterval(int agentBioSynchronizerInterval) {
        this.agentBioSynchronizerInterval = agentBioSynchronizerInterval;
    }

    public int getAuditXmlSynchronizerInterval() {
        return auditXmlSynchronizerInterval;
    }

    public void setAuditXmlSynchronizerInterval(int auditXmlSynchronizerInterval) {
        this.auditXmlSynchronizerInterval = auditXmlSynchronizerInterval;
    }

    public int getThresholdUpdaterInterval() {
        return thresholdUpdaterInterval;
    }

    public void setThresholdUpdaterInterval(int thresholdUpdaterInterval) {
        this.thresholdUpdaterInterval = thresholdUpdaterInterval;
    }

    public int getAuditSynchronizerInterval() {
        return auditSynchronizerInterval;
    }

    public void setAuditSynchronizerInterval(int auditSynchronizerInterval) {
        this.auditSynchronizerInterval = auditSynchronizerInterval;
    }

    public int getActivationCheckerInterval() {
        return activationCheckerInterval;
    }

    public void setActivationCheckerInterval(int activationCheckerInterval) {
        this.activationCheckerInterval = activationCheckerInterval;
    }

    public int getSynchronizerInterval() {
        return synchronizerInterval;
    }

    public void setSynchronizerInterval(int synchronizerInterval) {
        this.synchronizerInterval = synchronizerInterval;
    }

    public int getHarmonizerInterval() {
        return harmonizerInterval;
    }

    public void setHarmonizerInterval(int harmonizerInterval) {
        this.harmonizerInterval = harmonizerInterval;
    }

    public int getSettingsServiceInterval() {
        return settingsServiceInterval;
    }

    public void setSettingsServiceInterval(int settingsServiceInterval) {
        this.settingsServiceInterval = settingsServiceInterval;
    }

    public int getBlackListerInterval() {
        return blackListerInterval;
    }

    public void setBlackListerInterval(int blackListerInterval) {
        this.blackListerInterval = blackListerInterval;
    }

    public int getOtaInterval() {
        return otaInterval;
    }

    public void setOtaInterval(int otaInterval) {
        this.otaInterval = otaInterval;
    }

    public int getClientSettingInterval() {
        return clientSettingInterval;
    }

    public void setClientSettingInterval(int clientSettingInterval) {
        this.clientSettingInterval = clientSettingInterval;
    }

}
