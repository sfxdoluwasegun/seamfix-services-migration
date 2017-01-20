package com.sf.biocapture.ws.sms;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import nw.orm.core.query.QueryAlias;
import nw.orm.core.query.QueryModifier;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.sf.biocapture.app.JmsSender;
import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.common.Base64Coder;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.BlacklistedData;
import com.sf.biocapture.entity.KycBlacklist;
import com.sf.biocapture.entity.SmsActivationRequest;
import com.sf.biocapture.postactivation.sms.SmsReq;
import java.security.GeneralSecurityException;
import java.util.regex.PatternSyntaxException;

@Stateless
public class SmsDS extends DataService{
	
	@Inject
	private JmsSender appSession;
	
	@Inject
	private BioCache cache;
	
	public boolean isHelpMessage(String body){
		try {
			String[] reqItems = body.split(getDelimiter());
			String source = reqItems[0];// TAG::MAC_ADDRESS
			String[] t = source.split("::");
			if(t.length == 2){
				logger.debug("Help Message received via sms from " + t[0] + " message " + body);
				appSession.queueHelpMessage(body);
				return true;
			}
		} catch (PatternSyntaxException e) {
			logger.error("Error checking sms type::: Not fatal", e);
		}
		return false;
	}
	
	public String receive(String sender, String body){
		if(!isHelpMessage(body) && isBlocked(body)){
			return processSmsRegistration(sender, body);
		}
		return "false";
	}
	
	protected boolean isBlocked(String body){
		Object item = null;
		String ref = null;
		try {
			ref = body.split(">")[2];
			item = cache.getItem("BLOCKED" + ref.trim());
		} catch (PatternSyntaxException | ArrayIndexOutOfBoundsException e) {
			logger.error("", e);
		}
		logger.debug("Blocked sms gotten? from " + ref + " val: " + item);
		return item == null;
	}

	private String processSmsRegistration(String sender, String body) {
		if(validateSMS(body)){
			SmsMessage sm = new SmsMessage();
			sm.setSender(sender);
			sm.setBody(body);
			return appSession.queueSms(sm);
		}else{
			logger.info("Invalid sms received: sender [" + sender + "] " + body );
		}
		return "false";
	}
	
        @SuppressWarnings("PMD")
	private boolean validateSMS(String sms) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update("SeamfixBioCapture".getBytes(), 0, "SeamfixBioCapture".getBytes().length);

			// Separate message from Hashvalue
			int separator = sms.lastIndexOf(">");
			String message = sms.substring(0, separator);
			String hashValue = sms.substring(separator + 1);

			byte[] buffer = message.getBytes();
			digest.update(buffer, 0, buffer.length);
			byte[] digestArray = digest.digest();
			String encodedDigest = new String(Base64Coder.encode(digestArray));
			return hashValue.equals(encodedDigest);
		} catch (PatternSyntaxException | ArrayIndexOutOfBoundsException | GeneralSecurityException e) {
			logger.error("", e);
		}
		return false;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	private KycBlacklist getBlacklistedNode(String tag){
		KycBlacklist item = cache.getItem("BLKLIST-"+ tag, KycBlacklist.class);
		if(item == null){
			item = dbService.getByCriteria(KycBlacklist.class, Restrictions.eq("blacklistedItem", tag));
			if(item == null){
				item = new KycBlacklist(); // dummy
			}
			cache.setItem("BLKLIST-"+ tag, item, 60 * 60 * 24 * 7);
		}
		if(item.getPk() == null){
			return null;
		}
		return item;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean isBlacklisted(String tag, String method, String record, String sender) throws UnknownHostException{
		boolean result = false;
		String ip = Inet4Address.getLocalHost().getHostAddress();
		KycBlacklist bl = getBlacklistedNode(tag);
		if(bl != null){
			// Record is blacklisted
			BlacklistedData data = dbService.getByCriteria(BlacklistedData.class, Restrictions.eq("targetRecord", record), Restrictions.eq("processMethod", method)) ;//new BlacklistedData();
			if(data == null){
				data = new BlacklistedData();
				data.setProcessMethod(method);
				data.setTargetBlacklist(bl);
				data.setTargetRecord(record);
				data.setSourceAddress(ip);
				data.setSender(sender);
				dbService.create(data);
			}else{
				dbService.update(data);//updates modified date
			}
			result = true;
		}
		return result;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public boolean isSynchronized(String msisdn, String uniqueId){
		QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
		qm.addProjection(Projections.rowCount());
		return dbService.getByCriteria(Long.class, qm, Restrictions.eq("phoneNumber", msisdn), Restrictions.eq("uniqueId", uniqueId)) > 0;
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<SmsActivationRequest> getInitialRequest(String msisdn){
		QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
		qm.addAlias(new QueryAlias("phoneNumberStatus", "pns"));
		qm.addOrderBy(Order.desc("receiptTimestamp"));
		
		return dbService.getListByCriteria(SmsActivationRequest.class, qm, Restrictions.isNull("pns.churnTimestamp"), Restrictions.eq("isInitiator", true),
				Restrictions.eq("phoneNumber", msisdn));
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Long getSizeOfSmsWithoutBiometric() {
		QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
		qm.addProjection(Projections.rowCount());
		return dbService.getByCriteria(Long.class, qm, Restrictions.isNull("activationTimestamp"));
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public List<SmsReq> getSmsOnlyRecords(int pageIndex){
		QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
		qm.addProjection(Projections.property("uniqueId").as("uniqueId"));
		qm.setPaginated(pageIndex, appProps.getInt("nobiometrics.page", 500));
		qm.transformResult(true);
		return dbService.getListByCriteria(SmsReq.class, qm, Restrictions.isNull("activationTimestamp"));
	}

}
