package com.sf.biocapture.ws.sms;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.entity.PhoneNumberStatus;
import com.sf.biocapture.entity.SmsActivationRequest;
import java.net.UnknownHostException;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/bio/queue/SmsRegistrations")})
public class SmsMessageListener  extends BsClazz implements MessageListener{
	
	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy h:mm a");
	private SimpleDateFormat sdfs = new SimpleDateFormat("dd-MM-yyyy h:mm:ss a");
	
	@Inject
	private SmsDS ssm;

	@Override
	public void onMessage(Message msg) {
		
		try {
			ObjectMessage omsg = (ObjectMessage) msg;
			SmsMessage sms = (SmsMessage) omsg.getObject();
			
			try {
				processSms(sms);
			} catch (ParseException ex) {
                            logger.error("", ex);
                        } catch (UnknownHostException ex) {
                                logger.error("", ex);
                        }
			
		} catch (JMSException e) {
			logger.error("", e);
		}
		
	}
	
	private void processSms(SmsMessage sms) throws ParseException, UnknownHostException{
		String[] parts = sms.getBody().split(">");
		Date t;
		try {
			t = sdf.parse(parts[0] + " " + parts[1]);
		} catch (ParseException e) {
			t = sdfs.parse(parts[0] + " " + parts[1]);
			logger.error("", e.getMessage());
		}
		
		Timestamp regTimestamp = new Timestamp(t.getTime());
		String enrollmentRef = parts[2];
		String customerName = parts[3].toUpperCase();
		String numberArray = parts[4];
		String[] numberparts = numberArray.split("\\(");
		String number = numberparts[0];
		String number_serial = numberparts[1].split("\\)")[0];
		Timestamp receiptTimestamp = new Timestamp(new Date().getTime());
		
		if(ssm.isBlacklisted(enrollmentRef, parts[5], number, sms.getSender())){
			logger.info("Blacklisted SMS received from : " + enrollmentRef  + " SENDER: " + sms.getSender() + " RECORD: " + number);
			return;
		}
		if(ssm.isSynchronized(number, parts[5])){
			logger.debug("Phone already processed via biometrics " + number);
			return;
		}
		SmsActivationRequest smsActivationRequest = new SmsActivationRequest();
		smsActivationRequest.setCustomerName(customerName);
		smsActivationRequest.setEnrollmentRef(enrollmentRef);
		smsActivationRequest.setPhoneNumber(number);
		smsActivationRequest.setReceiptTimestamp(receiptTimestamp);
		smsActivationRequest.setSenderNumber(sms.getSender());
		smsActivationRequest.setStatus("UNACTIVATED");
		smsActivationRequest.setSerialNumber(number_serial);
//		smsActivationRequest.setActivationTimestamp(null);
		smsActivationRequest.setRegistrationTimestamp(regTimestamp);
		smsActivationRequest.setUniqueId(parts[5]);
		smsActivationRequest.setStateId(Long.valueOf(parts[6]));
		smsActivationRequest.setIsInitiator(true);
		
		PhoneNumberStatus phoneNumStatus = new PhoneNumberStatus();
		phoneNumStatus.setInitTimestamp(receiptTimestamp);
		phoneNumStatus.setStatus("ACTIVATION_PENDING");
		smsActivationRequest.setPhoneNumberStatus(phoneNumStatus);
		
		if(ssm.getDbService().create(smsActivationRequest) != null){
			logger.info("Sms Registration successfully processed: " + sms.getBody());
		}
	}
	
	public static void main(String[] args) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy h:mm:ss a");
		System.out.println(sdf.parse("25-09-2014 6:28:01 PM"));
	}

}
