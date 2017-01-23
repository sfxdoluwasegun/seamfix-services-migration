package com.sf.biocapture.ws.usecase;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.sf.biocapture.ws.biodata.BioDataResponse;

/**
 * 
 * @author Nnanna
 * @since 23/01/2017
 * 
 */
public interface IUseCaseValidation {
	public static final String MSISDN = "msisdn";
	
    @POST
    @Path("/general")
    @Produces({MediaType.APPLICATION_JSON})
    public BioDataResponse validateAll(@FormParam("msisdn") String msisdn, @FormParam("serial") String serial, @FormParam("puk") String puk);
    
    @POST
    @Path("/msisdn")
    @Produces({MediaType.APPLICATION_JSON})
    public BioDataResponse validateMsisdn(@FormParam("msisdn") String msisdn, @FormParam("puk") String puk);
    
    @POST
    @Path("/serial")
    @Produces({MediaType.APPLICATION_JSON})
    public BioDataResponse validateSerial(@FormParam("serial") String serial, @FormParam("puk") String puk);
    
}
