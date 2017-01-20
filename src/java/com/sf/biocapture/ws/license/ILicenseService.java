
package com.sf.biocapture.ws.license;

import com.sf.biocapture.StatusResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author @wizzyclems
 */
public interface ILicenseService
{    
    @POST
    @Path("/status")
    @Produces({MediaType.APPLICATION_JSON})
    public LicenseStatusResponse checkLicenseStatus(@FormParam("macAddress") String macAddress,@FormParam("tagId") String tagId );
    
    @POST
    @Path("/request")
    @Produces({MediaType.APPLICATION_JSON})
    public StatusResponse requestForLicensing(@FormParam("macAddress") String macAddress, 
            @FormParam("tagId") String tagId, @FormParam("agentName") String name, @FormParam("agentEmail") String agentEmail  );
}


