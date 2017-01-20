
package com.sf.biocapture.ws.license;

import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.FMLicenseRequest;
import com.sf.biocapture.entity.enums.KycManagerRole;
import com.sf.biocapture.entity.security.KMUser;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;

import nw.orm.core.query.QueryAlias;
import nw.orm.core.query.QueryFetchMode;
import nw.orm.core.query.QueryModifier;

import org.hibernate.FetchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author @wizzyclems
 */
@Stateless
public class LicenseDS extends DataService {
    
    public boolean createLicenseRequest(String macAddress, String tagId, String agentName, String agentEmail){
    	KMUser user = getUser(agentEmail);
    	if(user == null){
    		logger.debug("Unknown user requesting for license...");
    		return false;
    	}
    	
        FMLicenseRequest fmr = new FMLicenseRequest();
        fmr.setAgentName(user.getSurname() + " " + user.getFirstName());
        fmr.setEmailAddress(agentEmail);
        fmr.setKitTag(tagId);
        fmr.setMacAddress(macAddress);
        fmr.setRequestDate(new Timestamp(new Date().getTime()));
        fmr.setRequestedBy(user);
        
        Serializable s = getDbService().create(fmr);
        
        return (s != null);
    }
    
    public String[] getAllAdminEmails(){
        QueryModifier m = new QueryModifier(KMUser.class);
        m.addAlias(new QueryAlias("roles","r"));
        QueryFetchMode qf = new QueryFetchMode();
        qf.setAlias("r");
        qf.setFetchMode(FetchMode.JOIN);
        m.addFetchMode( qf );
        m.addProjection(Projections.property("emailAddress"));
        
        String[] roles = new String[]{KycManagerRole.ADMIN.name()} ;
        List<String> emails = getDbService().getListByCriteria(String.class, m, Restrictions.in("r.role", roles));
        if( (emails != null) && !emails.isEmpty() ){
            logger.debug("*** The admin email count is : " + emails.size());
            logger.debug("The list of admin emails returned is : " + emails.toArray( new String[emails.size()] ) );
            return emails.toArray( new String[emails.size()] );
        }
        
        return null;
    }
    
    public String getKitLicenseStatus(String macAddress){
	logger.debug("Checking for the validity status of the specified mac address.");
        // this is a combination of the approvalstatus and the license code
        String response = "";
        
        if( macAddress == null){
            return response;
        }
        
        QueryModifier qm = new QueryModifier(FMLicenseRequest.class);
        qm.addOrderBy( Order.desc("requestDate"));
        qm.setPaginated(0, 1);
        
        //retrieve the kit status from here
        List<FMLicenseRequest> licenses = dbService.getListByCriteria(FMLicenseRequest.class, qm,
        		Restrictions.eq("macAddress", macAddress).ignoreCase() );
 
        if (licenses == null || licenses.isEmpty()) {
            return "";
        }

        FMLicenseRequest license = licenses.get(0);
        if( (license.getApproved() == null) ){
                return null;
        }
        
        
       response = license.getApproved() + ":" + license.getLicenseHash();
        
       return response;
    }

    public boolean allowCreateLicense(String macAddress) {
        String msg = getKitLicenseStatus(macAddress);
        if( msg == null ){
//          "The license approval for this kit is pending."
            return false;
        }
        
        if( msg.isEmpty() ){
//          "There is no license for this kit. Please request for license."
            return true;
        }
        
        String[] resp = msg.split(":");
        return resp[0].equalsIgnoreCase("false");
    }
}
