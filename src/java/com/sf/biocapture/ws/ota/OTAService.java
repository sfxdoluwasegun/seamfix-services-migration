/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.ota;

import com.sf.biocapture.app.BsClazz;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Marcel
 * @since 13-Feb-2017
 */
@Path("/ota")
public class OTAService extends BsClazz {

    @Inject
    private OTADS otads;

    @POST
    @Path("/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings(OTARequest otar) {
        return Response.status(Response.Status.OK).entity(otads.handleOTASettings(otar)).build();
    }

}
