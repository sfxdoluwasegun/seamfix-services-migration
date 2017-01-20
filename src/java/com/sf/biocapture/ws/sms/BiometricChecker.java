package com.sf.biocapture.ws.sms;

import java.util.ArrayList;
import java.util.List;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.postactivation.sms.SmsReq;

public class BiometricChecker extends BsClazz implements Runnable{
	
	private Integer index = 0;
	
	private SmsDS sds;
	
	public BiometricChecker(Integer index, SmsDS sds) {
		this.index = index;
		this.sds = sds;
		
		logger.info("Initializing sms without biometric checker : " + index);
	}

	@Override
	public void run() {
		check();
		
	}
	
	private void check() {
		// pull records within index
		List<SmsReq> sms = sds.getSmsOnlyRecords(index);
		List<String> uidList = getUniqueIdList(sms);
		
		if(!uidList.isEmpty()){
			int updated = sds.getDbService().executeHQLUpdate("Update PhoneNumberStatus p set p.status = 'ACTIVATED' WHERE p.id in"
					+ uidList.toString().replace("[", "(").replace("]", ")"));
			logger.info("Total records updated: " + updated + " Total expected: " + uidList.size());
		}
	}
	
	private List<String> getUniqueIdList(List<SmsReq> req){
		List<String> list = new ArrayList<String>();
		
		return list;
	}

}
