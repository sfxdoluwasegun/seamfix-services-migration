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
import com.sf.biocapture.entity.Setting;
import com.sf.biocapture.entity.enums.SettingsEnum;
/**
 * 
 * @author Nnanna
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

	public ClientSettings getClientSettings(){
		ClientSettings cs = new ClientSettings();
		cs.to(access.getGlobalSettings());

		String validateMsisdnNM = access.getSettingValue(SettingsEnum.VALIDATE_MSISDN_NM);
		cs.setValidateMsisdnNM(Boolean.valueOf(validateMsisdnNM));

		String pukMandatoryNM = access.getSettingValue(SettingsEnum.PUK_MANDATORY_NM);
		cs.setPukMandatoryNM(Boolean.valueOf(pukMandatoryNM));

		String validateSerialNS = access.getSettingValue(SettingsEnum.VALIDATE_SERIAL_NS);
		cs.setValidateSerialNS(Boolean.valueOf(validateSerialNS));

		String pukMandatoryNS = access.getSettingValue(SettingsEnum.PUK_MANDATORY_NS);
		cs.setPukMandatoryNS(Boolean.valueOf(pukMandatoryNS));

		String validateMsisdnNMS = access.getSettingValue(SettingsEnum.NMS_VALIDATE_MSISDN);
		cs.setValidateMsisdnNMS(Boolean.valueOf(validateMsisdnNMS));

		String validateSerialNMS = access.getSettingValue(SettingsEnum.NMS_VALIDATE_SERIAL);
		cs.setValidateSerialNMS(Boolean.valueOf(validateSerialNMS));

		String pukMandatoryNMS = access.getSettingValue(SettingsEnum.NMS_PUK_MANDATORY);
		cs.setPukMandatoryNMS(Boolean.valueOf(pukMandatoryNMS));
		
		cs.setMsisdnMinLength(parseSettingInteger(SettingsEnum.MSISDN_MIN_LENGTH));
		cs.setMsisdnMaxLength(parseSettingInteger(SettingsEnum.MSISDN_MAX_LENGTH));
		cs.setSerialMinLength(parseSettingInteger(SettingsEnum.SERIAL_MIN_LENGTH));
		cs.setSerialMaxLength(parseSettingInteger(SettingsEnum.SERIAL_MAX_LENGTH));
		cs.setPukMinLength(parseSettingInteger(SettingsEnum.PUK_MIN_LENGTH));
		cs.setPukMaxLength(parseSettingInteger(SettingsEnum.PUK_MAX_LENGTH));

		return cs;
	}

	private int parseSettingInteger(SettingsEnum setting){
		int finalVal = 0;
		try{
			finalVal = Integer.valueOf(getSettingValue(setting));
		}catch(NumberFormatException e){
			logger.error("NumberFormatException on retrieving setting: " + setting.getName());
			finalVal = Integer.valueOf(setting.getValue()); //use default instead
		}
		return finalVal;
	}

	private String getSettingValue(SettingsEnum settingsEnum){
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
}
