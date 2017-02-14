/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.ota;

import javax.ws.rs.FormParam;
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
public interface IOTAService {

    @POST
    @Path("/settings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSettings(OTARequest otar);
}
