package com.sf.biocapture.ws.sms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.sf.biocapture.app.BsClazz;

/**
 * Get all sms without biometrics, confirm if they already have a unique_id on user_id table
 * 
 * if they do update the sms activation date to the part key on the user_id table
 * @author nuke
 *
 */

@Stateless
public class SmsBiometricService extends BsClazz{
	
	@Inject
	private SmsDS sds;
	
	private Integer threadPool = 5;
	
	@PostConstruct
	private void init() {
		threadPool = appProps.getInt("bio-checker-count", 5);

	}
	
	public void checkBiometrics() {
		// get size of records without biometrics
		Long noBiometrics = sds.getSizeOfSmsWithoutBiometric();
		if(noBiometrics == null) {
			noBiometrics = 0L;
		}
		logger.info("Bulk collection size for unsent sms: " + noBiometrics);
		ExecutorService service = Executors.newFixedThreadPool(threadPool);
		for(Long z = 0L; z <= noBiometrics; z = z + appProps.getInt("nobiometrics.page", 500)){
			BiometricChecker bc = new BiometricChecker(z.intValue(), sds);
			service.submit(bc);
		}
		
	}

}
