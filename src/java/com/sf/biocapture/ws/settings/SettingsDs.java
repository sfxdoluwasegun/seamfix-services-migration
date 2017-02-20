package com.sf.biocapture.ws.settings;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import nw.orm.core.exception.NwormQueryException;

import org.hibernate.criterion.Restrictions;

import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.ClientSettingValue;
import com.sf.biocapture.entity.ClientSettingVersion;
import com.sf.biocapture.entity.ClientSetting;
import com.sf.biocapture.entity.enums.ClientSettingEnum;
import com.sf.biocapture.entity.enums.ClientSettingStatusEnum;
import com.sf.biocapture.entity.enums.SettingsEnum;
import com.sf.biocapture.ws.ResponseCodeEnum;
import java.util.ArrayList;
import java.util.List;
import nw.orm.core.query.QueryAlias;
import nw.orm.core.query.QueryModifier;
import org.hibernate.criterion.Conjunction;
import org.hibernate.criterion.Order;

/**
 *
 * @author Nnanna
 * @author Marcel
 * @since 24/01/2017
 *
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SettingsDs extends DataService {

    @Inject
    AccessDS access;

    @Inject
    BioCache cache;

    private final String cachedKey = "SFX_CS";
    private final String activeVersionCachedKey = "SFX_CS_ACTIVE_VERSION";
    private final int cacheAge = 3600; //in seconds

    public GlobalSettingResponse getGlobalSettings() {
        GlobalSettingResponse cs = new GlobalSettingResponse();
        cs.to(access.getGlobalSettings());

        cs.setClientSettingInterval(parseSettingInteger(SettingsEnum.CLIENT_SETTING_INTERVAL));

        cs.setCode(ResponseCodeEnum.SUCCESS);
        cs.setDescription(ResponseCodeEnum.SUCCESS.getDescription());
        return cs;
    }

    private int parseSettingInteger(SettingsEnum setting) {
        int finalVal = 0;
        try {
            finalVal = Integer.valueOf(access.getSettingValue(setting));
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException on retrieving setting: " + setting.getName());
            finalVal = Integer.valueOf(setting.getValue()); //use default instead
        }
        return finalVal;
    }

    public ClientSettingResponse getClientSettings() {
        ClientSettingResponse cs = new ClientSettingResponse();


        /**
         * use this version to ensure that all the settings retrieved below are of the same version
         */
        ClientSettingVersion clientSettingVersion = getActiveClientSettingVersion(); 
        cs.setSettingVersion(clientSettingVersion.getVersionNumber());

        String validateMsisdnNM = getClientSettingValue(ClientSettingEnum.VALIDATE_MSISDN_NM, clientSettingVersion);
        cs.setValidateMsisdnNM(Boolean.valueOf(validateMsisdnNM));

        String pukMandatoryNM = getClientSettingValue(ClientSettingEnum.PUK_MANDATORY_NM, clientSettingVersion);
        cs.setPukMandatoryNM(Boolean.valueOf(pukMandatoryNM));

        String validateSerialNS = getClientSettingValue(ClientSettingEnum.VALIDATE_SERIAL_NS, clientSettingVersion);
        cs.setValidateSerialNS(Boolean.valueOf(validateSerialNS));

        String pukMandatoryNS = getClientSettingValue(ClientSettingEnum.PUK_MANDATORY_NS, clientSettingVersion);
        cs.setPukMandatoryNS(Boolean.valueOf(pukMandatoryNS));

        String validateNMS = getClientSettingValue(ClientSettingEnum.VALIDATE_NMS, clientSettingVersion);
        cs.setValidateNMS(Boolean.valueOf(validateNMS));

        String pukMandatoryNMS = getClientSettingValue(ClientSettingEnum.NMS_PUK_MANDATORY, clientSettingVersion);
        cs.setPukMandatoryNMS(Boolean.valueOf(pukMandatoryNMS));

        cs.setMsisdnMinLength(parseSettingInteger(ClientSettingEnum.MSISDN_MIN_LENGTH, clientSettingVersion));
        cs.setMsisdnMaxLength(parseSettingInteger(ClientSettingEnum.MSISDN_MAX_LENGTH, clientSettingVersion));
        cs.setSerialMinLength(parseSettingInteger(ClientSettingEnum.SERIAL_MIN_LENGTH, clientSettingVersion));
        cs.setSerialMaxLength(parseSettingInteger(ClientSettingEnum.SERIAL_MAX_LENGTH, clientSettingVersion));
        cs.setPukMinLength(parseSettingInteger(ClientSettingEnum.PUK_MIN_LENGTH, clientSettingVersion));
        cs.setPukMaxLength(parseSettingInteger(ClientSettingEnum.PUK_MAX_LENGTH, clientSettingVersion));

        cs.setPortraitCaptureMandatory(Boolean.valueOf(getClientSettingValue(ClientSettingEnum.PORTRAIT_MANDATORY, clientSettingVersion)));
        cs.setPortraitValidationMandatory(Boolean.valueOf(getClientSettingValue(ClientSettingEnum.VALIDATE_PORTRAIT, clientSettingVersion)));
        cs.setFingerprintCaptureMandatory(Boolean.valueOf(getClientSettingValue(ClientSettingEnum.FINGERPRINT_MANDATORY, clientSettingVersion)));
        cs.setFingerprintValidationMandatory(Boolean.valueOf(getClientSettingValue(ClientSettingEnum.VALIDATE_FINGERPRINT, clientSettingVersion)));

        String requiredFingerprintTypes[] = getClientSettingValue(ClientSettingEnum.REQUIRED_FINGERPRINT_TYPES, clientSettingVersion).split(",");
        List<SettingFingerprintTypesEnum> fingerprintTypesEnum = new ArrayList<>();
        for (String type : requiredFingerprintTypes) {
            fingerprintTypesEnum.add(SettingFingerprintTypesEnum.valueOf(type.trim()));
        }
        cs.setFingerprintTypes(fingerprintTypesEnum);

        cs.setCode(ResponseCodeEnum.SUCCESS);
        cs.setDescription(ResponseCodeEnum.SUCCESS.getDescription());
        return cs;
    }

    private int parseSettingInteger(ClientSettingEnum setting, ClientSettingVersion clientSettingVersion) {
        int finalVal = 0;
        try {
            finalVal = Integer.valueOf(getClientSettingValue(setting, clientSettingVersion));
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException on retrieving setting: " + setting.getName());
            finalVal = Integer.valueOf(setting.getValue()); //use default instead
        }
        return finalVal;
    }

    public String getClientSettingValue(ClientSettingEnum clientSettingEnum, ClientSettingVersion clientSettingVersion) {
        String settingName = clientSettingEnum.getName().trim();
        if (clientSettingVersion == null) {
            //retrieve active version
            clientSettingVersion = getActiveClientSettingVersion();
            if (clientSettingVersion == null) {
                //no need to continue if this failed at this point
                return null;
            }
            cache.setItem(activeVersionCachedKey, clientSettingVersion, cacheAge);
        }

        try {
            ClientSetting clientSetting = (ClientSetting) cache.getItem(cachedKey + settingName);
            if (clientSetting == null) {
                //just to ensure this setting name is created in db
                clientSetting = getClientSetting(clientSettingEnum);
                if (clientSetting == null) {
                    //no need to continue if this failed
                    return null;
                }
                cache.setItem(cachedKey + settingName, clientSetting, cacheAge);
            }
            String settingValue = cache.getItem(cachedKey + clientSettingVersion.getVersionNumber() + settingName, String.class);
            if (settingValue == null) {
                Conjunction conjunction = Restrictions.conjunction();
                conjunction.add(Restrictions.eq("clientSetting", clientSetting));
                conjunction.add(Restrictions.eq("clientSettingVersion", clientSettingVersion));
                ClientSettingValue clientSettingValue = getDbService().getByCriteria(ClientSettingValue.class, conjunction);
                if (clientSettingValue == null) {
                    clientSettingValue = new ClientSettingValue();
                    clientSettingValue.setClientSetting(clientSetting);
                    clientSettingValue.setClientSettingVersion(clientSettingVersion);
                    clientSettingValue.setValue(clientSettingEnum.getValue());
                    getDbService().create(clientSettingValue);
                }
                settingValue = clientSettingValue.getValue().trim();
                cache.setItem(cachedKey + clientSettingVersion.getVersionNumber() + settingName, settingValue, cacheAge);
            }
            return settingValue;
        } catch (NwormQueryException e) {
            logger.error("", e);
        }
        return null;
    }

    private ClientSetting getClientSetting(ClientSettingEnum clientSettingEnum) {
        try {
            ClientSetting clientSetting = getDbService().getByCriteria(ClientSetting.class, Restrictions.eq("name", clientSettingEnum.getName().trim()));
            if (clientSetting == null) {
                clientSetting = new ClientSetting();
                clientSetting.setName(clientSettingEnum.getName().trim());
                clientSetting.setDescription(clientSettingEnum.getDescription().trim());
                getDbService().create(clientSetting);
            }
            return clientSetting;
        } catch (NwormQueryException e) {
            logger.error("", e);
        }

        return null;
    }

    public ClientSettingVersion getActiveClientSettingVersion() {
        try {
            ClientSettingVersion clientSettingVersion = (ClientSettingVersion) cache.getItem(activeVersionCachedKey);
            if (clientSettingVersion != null) {
                return clientSettingVersion;
            }
            //we expect only one entry to be active at a time.
            clientSettingVersion = getDbService().getByCriteria(ClientSettingVersion.class, Restrictions.eq("clientSettingStatusEnum", ClientSettingStatusEnum.ACTIVE));
            if (clientSettingVersion == null) {
                clientSettingVersion = new ClientSettingVersion();
                clientSettingVersion.setClientSettingStatusEnum(ClientSettingStatusEnum.ACTIVE);
                clientSettingVersion.setDescription("Default values");
                Integer version = getMaxClientSettingVersionNumber();
                if (version == null) {
                    version = 0;
                }
                clientSettingVersion.setVersionNumber(version + 1);
                getDbService().create(clientSettingVersion);
            }
            return clientSettingVersion;
        } catch (NwormQueryException e) {
            logger.error("", e);
        }
        return null;
    }

    public Integer getMaxClientSettingVersionNumber() {
        Integer version = null;
        try {
            QueryModifier modifier = new QueryModifier(ClientSettingVersion.class);
            modifier.addOrderBy(Order.desc("versionNumber"));
            modifier.setPaginated(0, 1);
            ClientSettingVersion clientSettingVersion = getDbService().getByCriteria(ClientSettingVersion.class, modifier);
            if (clientSettingVersion != null) {
                version = clientSettingVersion.getVersionNumber();
            }
            return version;
        } catch (NwormQueryException e) {
            logger.error("", e);
        }
        return version;
    }
}
