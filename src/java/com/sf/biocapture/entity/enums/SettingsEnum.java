/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.entity.enums;

/**
 *
 * @author Marcel
 * @since 09-Nov-2016, 09:15:07
 */
public enum SettingsEnum {

    OTP_REQUIRED("OTP-REQUIRED", "false", "Used to specify whether otp is required during client login"),
    LOGIN_OFFLINE("LOGIN-OFFLINE", "false", "Determines whether an agent can login offline"),
    LOGIN_OFFLINE_VALIDATION_TYPE("LOGIN-OFFLINE-VALIDATION-TYPE", "CACHED", "Can either be CACHED OR SHORTCODE"),
    AIRTIME_SALES_MANDATORY("AIRTIME-SALES-MANDATORY", "false", "Informs client on the availability of airtime sales"),
    AIRTIME_SALES_URL("AIRTIME-SALES-URL", "http://seamfix.com/", "URL to airtime sales"),
    AVAILABLE_USE_CASE("AVAILABLE-USE-CASE", "NS,NM,SS,RR,AR,BU", "This is used to specify available use case exposed to client"),
    CLIENT_ACTIVITY_LOG_BATCH_SIZE("CLIENT-ACTIVITY-LOG-BATCH-SIZE", "20", "Determines the batch size of client activity log sent to the server from the client"),
    MAXIMUM_MSISDN_ALLOWED_PER_REGISTRATION("MAXIMUM-MSISDN-ALLOWED-PER-REGISTRATION", "5", "Specifies the maximum number of msisdn that can be registered per registration process"),
    AGILITY_SIM_SERIAL_VALID_STATUS_KEYS("AGILITY-SIM-SERIAL-VALID-STATUS-KEYS", "F,E", "Comma separated keys used to specify valid sim serial keys retrieved from agility"),
    AGL_BIO_UPDATE_PLATINUM_CODES("AGL-BIO-UPDATE-PLATINUM-CODES", "PLATM", "Comma separated codes used to specify the category codes valid for registering platinum customers"),
    AGL_BIO_UPDATE_MNP_CODES("AGL-BIO-UPDATE-MNP-CODES", "Port-In", "Comma separated codes used to specify the category codes valid for registering MNP customers"),
    AGL_BIO_UPDATE_POSTPAID_CODES("AGL-BIO-UPDATE-POSTPAID-CODES", "Y", "Comma separated codes used to specify the category codes valid for registering Postpaid customers"),
    SIMSWAP_DATE_VALIDATION("SIMSWAP-DATE-VALIDATION", "true", "Indicates if sim swap date will be compared against activation date"),
    SIMSWAP_TIME_FRAME("SIMSWAP-TIME-FRAME", "7", "Minimum no of days allowed between sim activation and sim swap"),
    ENABLE_VAS_MODULE("ENABLE-VAS-MODULE", "true", "Determines whether or not the VAS module will be enabled on the clients"),
    MINIMUM_ACCEPTABLE_CHARACTER("MINIMUM-ACCEPTABLE-CHARACTER", "2", "This is the minimum number of allowed characters for name field during foreigner's registration"),

    //DYNAMIC BIOSMART SETTINGS
    VALIDATE_MSISDN_NM("VALIDATE-MSISDN-NM", "true", "Determines whether client will do backend validation for msisdn for New Reg (MSISDN) use case"),
    PUK_MANDATORY_NM("PUK-MANDATORY-NM", "false", "Determines whether puk is mandatory for New Reg (MSISDN) use case"),
    VALIDATE_SERIAL_NS("VALIDATE-SERIAL-NS", "true", "Determines whether client will do backend validation for sim serial for New Reg (SIM SERIAL) use case"),
    PUK_MANDATORY_NS("PUK-MANDATORY-NS", "false", "Determines whether puk is mandatory for New Reg (SIM SERIAL) use case"),
    VALIDATE_NMS("VALIDATE-NMS", "true", "Determines whether client will do backend validation for msisdn and serial for New Reg (MSISDN+SERIAL) use case"),
    NMS_PUK_MANDATORY("NMS-PUK-MANDATORY", "false", "Determines whether puk is mandatory for New Reg (MSISSN+SERIAL) use case"),
    
    MSISDN_MIN_LENGTH("MSISDN-MIN-LENGTH", "11", "MSISDN minimum length"),
    MSISDN_MAX_LENGTH("MSISDN-MAX-LENGTH", "11", "MSISDN maximum length"),
    SERIAL_MIN_LENGTH("SERIAL-MIN-LENGTH", "20", "SERIAL minimum length"),
    SERIAL_MAX_LENGTH("SERIAL-MAX-LENGTH", "20", "SERIAL maximum length"),
    PUK_MIN_LENGTH("PUK-MIN-LENGTH", "8", "PUK minimum length"),
    PUK_MAX_LENGTH("PUK-MAX-LENGTH", "8", "PUK maximum length"),
    
    //Biometric Validation
    PORTRAIT_MANDATORY("PORTRAIT-MANDATORY", "true", "Determines whether portrait capture is mandatory"),
    VALIDATE_PORTRAIT("VALIDATE-PORTRAIT", "true", "Determines whether portrait validation is required"),
    FINGERPRINT_MANDATORY("FINGERPRINT-MANDATORY", "true", "Determines whether fingerprint capture is mandatory"),
    VALIDATE_FINGERPRINT("VALIDATE-FINGERPRINT", "true", "Determines whether fingerprint validation is required"),
    REQUIRED_FINGERPRINT_TYPES("REQUIRED-FINGERPRINT-TYPES", "RT, RI, RM, RR, RP, LT, LI, LM, LR, LP", "This is a comma separated values of available or required fingerprint types to be captured on the client (R and L stands for Right and Left respectively. T, I, M, R and P stands for Thumb, Index, Middle, Ring and Pinky fingers respectively)"),
    
    NOTIFICATION_CHECKER_INTERVAL("NOTIFICATION-CHECKER-INTERVAL", "60", "Time interval, in seconds, between which client checks for notifications from server"),
    AGENT_BIOSYNC_INTERVAL("AGENT-BIOSYNC-INTERVAL", "60", "Time interval, in seconds, between which client sends available agents' biometrics to server"),
    AUDIT_SYNC_INTERVAL("AUDIT-SYNC-INTERVAL", "1800", "Time interval, in seconds, between which client sends available client activity logs to HTTP server"),
    AUDIT_XML_SYNC_INTERVAL("AUDIT-XML-SYNC-INTERVAL", "1800", "Time interval, in seconds, between which client sends available audit xml files to SFTP server"),
    THRESHOLD_CHECKER_INTERVAL("THRESHOLD-CHECKER-INTERVAL", "3600", "Time interval, in seconds, between which client checks server for threshold updates"),
    ACTIVATION_CHECKER_INTERVAL("ACTIVATION-CHECKER-INTERVAL", "1200", "Time interval, in seconds, between which client checks server for activation status of msisdns"),
    SYNCHRONIZER_INTERVAL("SYNCHRONIZER-INTERVAL", "2", "Time interval, in seconds, between which client sends available sync files to SFTP server for processing"),
    HARMONIZER_INTERVAL("HARMONIZER-INTERVAL", "120", "Time interval, in seconds, between which client checks for status of each registration using unique ID"),
    SETTINGS_INTERVAL("SETTINGS-INTERVAL", "300", "Time interval, in seconds, between which client checks server for settings"),
    OTA_INTERVAL("OTA-INTERVAL", "300", "Time interval, in seconds, between which client checks for new application update"),
    
    BLACKLIST_CHECKER_INTERVAL("BLACKLIST-CHECKER-INTERVAL", "600", "Time interval, in seconds, between which client checks server for kit and agent blacklist status");

    
    private SettingsEnum(String name, String value, String description) {
        this.name = name;
        this.value = value;
        this.description = description;
    }

    private String name;
    private String value;
    private String description;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

}
