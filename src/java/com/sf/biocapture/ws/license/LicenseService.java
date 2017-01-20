
package com.sf.biocapture.ws.license;

import com.sf.biocapture.StatusResponse;
import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.email.EmailMethods;
import com.sf.biocapture.email.EmailMethods.EmailResponse;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.Path;
import nw.orm.core.exception.NwormQueryException;

@Path("/license")
public class LicenseService  extends BsClazz implements ILicenseService
{
    @Inject
    LicenseDS licenseDS;

    /**
     * This service endpoint allows you to check the license status of a kit using it's mac address
     * 
     * Status  0 - dissapprove, 1 - approve, 2 - pending, 3 - no license,4 - invalid input parameters, non-positive-value - error
     * 
     * @param macAddress
     * @param tagId
     * @return 
     */ 
    @Override
    public LicenseStatusResponse checkLicenseStatus(String macAddress, String tagId) {
        logger.debug("Mac Address.: " + macAddress + " tag Id : " + tagId);       
        
        
        if( (macAddress == null) || macAddress.isEmpty() || (tagId == null) || tagId.isEmpty() ){  
            return getResponse("Both mac address and tag Id are required for checking license status.", 4, null);
        }    
                
        String msg = licenseDS.getKitLicenseStatus(macAddress);
        if( msg == null ){
            return getResponse("The license approval for this kit is pending.", 2, null);
        }
        
        if( msg.isEmpty() ){
            return getResponse("There is no license for this kit. Please request for license.", 3, null);
        }
        
        String[] s = msg.split(":");
        if( s[0].equalsIgnoreCase("true")){
            return getResponse("This kit has been licensed.", 1, s[1]);
        }else{
            return getResponse("This kit's license request has been rejected.", 0, s[1]);
        }
       
    }

    public LicenseStatusResponse getResponse(String msg, int status, String licenseCode){
        LicenseStatusResponse response = new LicenseStatusResponse();
        StatusResponse sr = new StatusResponse();
        sr.setMessage(msg);
        sr.setStatus(status);
        response.setStatusResponse(sr);
        response.setLicenseCode( licenseCode );
        
        return response;
    }
    
    @Override
    public StatusResponse requestForLicensing(String macAddress, String tagId, String agentName, String agentEmail) {
        logger.debug("Mac Address.: " + macAddress + " tag Id : " + tagId + " agent name : " + agentName + " agent email : " + agentEmail); 
        
        //Status  0 - false, 1 - true, non-positive-value - error
        if(macAddress == null || macAddress.isEmpty() || tagId == null || tagId.isEmpty() || agentEmail == null ||
                agentEmail.isEmpty() || !validEmail(agentEmail)){
            return getResponse("All fields are required and email has to be a valid email Id. Correct and try again.",0,null).getStatusResponse();
        }        
         
        boolean createAllowed = licenseDS.allowCreateLicense(macAddress);
        if( createAllowed ){
            try{
                if( licenseDS.createLicenseRequest(macAddress, tagId, agentName, agentEmail) ){
                    String[] s = licenseDS.getAllAdminEmails();
                    if( (s != null) && (s.length > 0) ){
                        logger.debug("*** actually attempting to send an email to admin users...");
                        LicenseRequestParams licenseParams = new LicenseRequestParams(macAddress,tagId,agentName,agentEmail,new Timestamp(new Date().getTime()), s);
                        EmailResponse eRes = EmailMethods.sendLicenseRequest( licenseParams );
                        logger.debug("The email response is : " + eRes.name());
                    }else{
                        logger.debug("*** list of admin emails returned a null or empty array.");
                    }
                        

                    return getResponse("Your license request has been sent for approval.",1,null).getStatusResponse();
                }

                return getResponse("Failed to request license. Please try again later.",0,null).getStatusResponse();
            }catch(NwormQueryException e){
                return getResponse("An error occurred while trying to create license. Please try again later.",-1,null).getStatusResponse();
            }
        }
        
        return getResponse("You already have a license. You cannot request for another now.",0,null).getStatusResponse();        
    }
    
    private boolean validEmail( String agentEmail ){
//        Pattern pattern = Pattern.compile("^.+@.+\\..+$");
        Pattern pattern = Pattern.compile("^.+@.+(\\.[^\\.]+)+$");
        Matcher matcher = pattern.matcher(agentEmail);
        
        return matcher.matches();
    }
    
}
