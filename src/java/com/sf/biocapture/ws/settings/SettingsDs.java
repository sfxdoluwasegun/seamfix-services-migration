package com.sf.biocapture.ws.settings;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.ds.DataService;
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
		
		return cs;
	}
}
