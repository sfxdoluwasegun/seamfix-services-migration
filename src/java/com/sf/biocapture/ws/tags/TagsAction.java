package com.sf.biocapture.ws.tags;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.entity.EnrollmentRef;
import com.sf.biocapture.entity.enums.KycPrivilege;
import com.sf.biocapture.entity.security.KMUser;

import java.text.SimpleDateFormat;
import java.util.Date;

@Stateless
public class TagsAction extends BsClazz{
	
	@Inject
	protected TagsDS ds;
	
	@Inject
	private AccessDS accessDS;
        
	private boolean kitIsBlacklisted(String tag, String mac){
		return accessDS.checkBlacklistStatus(tag, mac).equalsIgnoreCase("Y");
	}
	
	public ClientRefResponse retag(ClientRefRequest cr){
		ClientRefResponse cresp = new ClientRefResponse();
		logger.debug("MAC ADDRESS: " + cr.getMac() + "; Tag: " + cr.getRef() + " ; admin email : [" + cr.getAdminEmail() + "]");
                KMUser kUser = ds.getKmUserByEmail(cr.getAdminEmail());
                if( (cr.getAdminEmail() == null) || (cr.getAdminEmail().isEmpty()) || (kUser == null) || !kUser.isActive() || !kUser.hasPrivilege(KycPrivilege.TAGGING)){                
                    cresp.setStatus(-2);
                    cresp.setMessage("A valid user email is required for re-tagging a kit. Please provide one and try again.");
                    return cresp;
                }
                
                if(isEmpty(cr.getMac()) || isEmpty(cr.getRef())){
                        // invalid request received
                        cresp.setStatus(-2);
                        cresp.setMessage("Unspecified Machine ID. MAC Address not valid");
                }else{
                        //check if kit is blacklisted
                        if(kitIsBlacklisted(cr.getRef(), cr.getMac())){
                                cresp.setStatus(-2);
                                cresp.setMessage("Kit is blacklisted. Please contact support");
                                return cresp;
                        }

                        //check if client time is incorrect
                        if(cr.getClientTime() != null){
                                if(!ds.clientTimeIsCorrect(String.valueOf(cr.getClientTime().getTime()))){
                                        cresp.setStatus(-2);
                                        cresp.setMessage("Please correct your system time. Server time is " + new SimpleDateFormat("dd MMM yyyy hh:mm:ss a").format(new Date()));
                                        return cresp;
                                }
                        }

                        List<EnrollmentRef> refs = ds.getKitByTagOrMac(cr.getMac(), cr.getRef()); // check for ref existence
                        logger.debug("No of records found: " + refs.size());
                        if(refs.isEmpty()){
                                // ref does not exist
                                cresp.setStatus(-2);
                                cresp.setMessage("This Kit has not been tagged. Retag request has been rejected");
                        }else if(refs.size() == 1){
                                // ref exists
                                logger.debug("-------->>>>> ABOUT TO RETAG KIT REF: " + cr.getRef());
                                EnrollmentRef ref = refs.get(0);
                                cresp = ds.retagKit(cr, ref, false);

                        }else if(refs.size() == 2){
                                logger.debug("Double kit entry detected...");
                                cresp = ds.handleDoubleKitEntry(cr, refs);
                        }else{
                                cresp.setStatus(-2);
                                cresp.setMessage("Multiple Entries found, please contact support");
                        }
                }
		return cresp;
	}
	
	public ClientRefResponse testKit(String refz){
		ClientRefResponse cr = new ClientRefResponse();
		logger.debug("Test request received " + refz);
                EnrollmentRef ref = ds.getEnrollmentRef(refz);
                if(ref != null){
                        ref.setCustom1("TESTED");
                        if(ds.update(ref)){
                                cr.setStatus(0);
                                cr.setMessage("Test result updated successfully");
                                return cr;
                        }
                }
		cr.setStatus(-2);
		cr.setMessage("Failed to update test result, please retry");
		
		return cr;
	}
	
	/**
	 * Returns true if text is empty
	 * @param text
	 * @return
	 */
	private boolean isEmpty(String text){
		return text == null || text.length() == 0;
	}

}
