package com.sf.biocapture.analyzer;

import java.util.Arrays;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

import org.hibernate.criterion.Restrictions;
import org.keyczar.Crypter;

import com.sf.biocapture.CryptController;
import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.ds.BioDS;
import com.sf.biocapture.entity.EnrollmentRef;
import com.sf.biocapture.entity.security.BannedSource;
import com.sf.biocapture.entity.security.WatchList;

import org.keyczar.exceptions.KeyczarException;

@Stateless
public class IntrusionAnalyzer extends BsClazz{

	@Inject
	private BioCache cache;

	@Inject
	private BioDS bioDs;
	
	@Inject
	private AccessDS accessDs;

	@Inject
	private CryptController cryptControl;

	protected final String AUTH_HEADER_KEY = "#D8CK>HIGH<LOW>#";
        private static final String BANNED_ = "BANNED_";
        private static final String VERSION_ = "VERSION_";

	protected String scUuid;
	private String agent;
	private String body;
	private String auth;
	private String clientId;

	private String sourceIp;
	private String method;
	
	/**
	 * Flags whether a request should proceed or fail
	 * @param req
	 * @param request
	 * @return true for clean request false for otherwise
	 */
	public boolean analyze(HttpServletRequest req, ContainerRequestContext request){
		sourceIp = getRemoteAddress(req);
		method = request.getUriInfo().getPath();
		auth = request.getHeaderString("sc-auth-key");
		body = req.getQueryString();
		scUuid = request.getHeaderString("User-UUID");
		agent = request.getHeaderString("User-Agent");
		clientId = request.getHeaderString("Client-ID");
		logger.debug("User-Agent: " + agent + "; Client-ID: " + clientId + "; scUuid: " + scUuid);

		if(isBanned(sourceIp) || null == auth || !checkAuthenticationHeader(sourceIp, request.getUriInfo().getPath())){
			logger.debug("Banned Request from: " + sourceIp);
			return false;
		}
		return true;
	}
	
	protected boolean isAppSignValid(String[] codes){
		if(!checkAppSignature()){
			//we're not checking signature for this app version
			return true;
		}
		String appSign = getClientSignature(codes); //base64 encoding of client signature
		if(appSign == null){
			return false;
		}
		String serverSign = getAppSignature();
		logger.debug("App sign from client: " + appSign + "; from server: " + serverSign); 
		return appSign.equalsIgnoreCase(serverSign);
	}
	
	protected String getClientSignature(String[] codes){
		String appSign = null;
		try{
			appSign = codes[3];
		}catch(ArrayIndexOutOfBoundsException ex){ //throws for app versions without signature
			logger.error("Request does not contain an app signature " + agent);
		}
		return appSign;
	}
	
	/**
	 * gets a hash of the app signature from the settings table
	 * @return
	 */
	protected String getAppSignature(){
		return accessDs.getSettingValue(agent + "-APP-SIGN", "").trim();
	}
	
	/**
	 * checks app signature based on the kit version
	 * @return
	 */
	protected boolean checkAppSignature(){
		//check cache to determine if to check for app signature
		Boolean checkVersion = cache.getItem(VERSION_ + agent.replaceAll(".", "_"), Boolean.class);
		if(checkVersion == null){
			//get check status for app version
			String appVersions = appProps.getProperty("allowed-app-sign-versions", "");
			logger.debug("Versions to check app version for: " + appVersions);
			boolean check = Arrays.asList(appVersions.split(" ")).contains(agent);
			logger.debug("Check app version for " + agent + "? " + check);
			
			//add status to cache
			cache.setItem(VERSION_ + agent.replaceAll(".", "_"), check, 5 * 60); //cache for 5 minutes
			
			return check;
		}
		
		return false;
	}
	
	protected boolean isClientMacValid(EnrollmentRef ref, String clientMac){
		if(clientId != null && clientId.toLowerCase().contains("droid")){
			//mac address from droid client
			logger.debug("Before formatting: " + clientMac);
			clientMac = clientMac.replaceAll("-", ":");
			logger.debug("After formatting: " + clientMac);
		}
		logger.debug("MAC Address to compare against server: " + clientMac);
		return ref != null && ref.getMacAddress() != null && ref.getMacAddress().equalsIgnoreCase(clientMac);
	}

	protected String getRemoteAddress(HttpServletRequest request) {
		String ip = request.getHeader("X-FORWARDED-FOR");
		if (ip == null) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * Determines if an address has been banned
	 * @param ip
	 * @return true if banned false if otherwise
	 */
	protected boolean isBanned(String ip){
		Integer item = (Integer) cache.getItem(BANNED_ + ip.replaceAll(".", "_")); // cached banned status
		if(item == null){// check db
			BannedSource ban = bioDs.getDbService().getByCriteria(BannedSource.class, Restrictions.eq("remoteAddress", ip), Restrictions.gt("expiryDate", new Date()));
			if(ban == null){
				cache.setItem(BANNED_+ ip.replaceAll(".", "_"), 0, 60);// not banned cache briefly
				return false;
			}else{
				// is definitely banned
				cache.setItem(BANNED_+ ip.replaceAll(".", "_"), 1, Integer.valueOf((ban.getExpiryDate().getTime() - new Date().getTime())/1000 + ""));
				return true;
			}

		} else if (item == 0) {
			// not banned
			return false;
		} else {
			// banned
			return true;

		}
	}

	protected boolean logRequest(){
		WatchList wl = new WatchList();
		wl.setRemoteAddress(sourceIp);
		wl.setTargetUrl(method);
		wl.setRequestBody(body);
		bioDs.getDbService().create(wl);

		Integer item = (Integer) cache.getItem("LA_"+ sourceIp.replaceAll(".", "_"));
		if(item == null){
			cache.setItem("LA_"+ sourceIp.replaceAll(".", "_"), 1, 10 * 60);
		}else if(item >= 5){ // ban candidate
			BannedSource ban = new BannedSource();
			ban.setRemoteAddress(sourceIp);
			ban.setExpiryDate(new Date(new Date().getTime() + 1 * 60 * 60 * 1000)); // 12 hour ban
			bioDs.save(ban);
			cache.setItem(BANNED_+ sourceIp.replaceAll(".", "_"), 1, Integer.valueOf((ban.getExpiryDate().getTime() - new Date().getTime())/1000 + ""));
		}else{
			cache.setItem("LA_"+ sourceIp.replaceAll(".", "_"), item + 1, 10 * 60);
		}

		return false;
	}

	/**
	 * This method takes into consideration tagging requests
	 * @param auth
	 * @param agent
	 * @param method
	 * @return
	 */
	protected boolean checkAuthenticationHeader(String sourceIp, String method){
		if(AUTH_HEADER_KEY.equals(auth)){
			return checkV1Header(auth, agent);
		}else{
			Crypter crypter = cryptControl.getCrypter();
			try {
				String message = crypter.decrypt(auth); //TAG:MAC:UUID:APP-SIGN
				String[] codes = message.split(":");
				String tag = codes[0];

				if("0".equals(tag)){ //TODO consider limiting it to just url for tagging
					return scUuid != null && scUuid.equals(codes[2]); // un-tagged requests should only be checked for HMAC
				}
				
				logger.debug("Authenticating headers for " + tag + "; mac: " + codes[1] + "; uuid: " + codes[2]);

				String item = (String) cache.getItem("TAG_"+ tag);
				if(item == null){
					EnrollmentRef ref = bioDs.getEnrollmentRef(tag);
					logger.debug("codes[1] BEFORE: " + codes[1]);
					if(isClientMacValid(ref, codes[1])){
						logger.debug("codes[1] AFTER: " + codes[1]);
						cache.setItem("TAG_"+ tag, codes[1], 12 * 60 * 60);
						return scUuid != null && scUuid.equals(codes[2]) && isAppSignValid(codes);
					}
				}else if(item.equalsIgnoreCase(codes[1])){
					return scUuid != null && scUuid.equals(codes[2]);
				}

			} catch (KeyczarException e) {
				logger.error("", e);
			}
			return false;
		}
	}
	
	protected boolean checkV1Header(String auth, String userAgent){
		return null != userAgent;
	}
	
}