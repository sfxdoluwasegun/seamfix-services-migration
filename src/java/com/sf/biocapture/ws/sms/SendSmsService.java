package com.sf.biocapture.ws.sms;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sf.biocapture.app.KannelSMS;
import java.io.IOException;

@Path("/sendsms")
public class SendSmsService {
	
	@Inject
	private KannelSMS ksms;
	
	@GET
	@Path("/")
	public Response send(@Context HttpHeaders headers, @DefaultValue("MTN KYC") @QueryParam("from") String from, @QueryParam("to") String to, @QueryParam("message") String message){
		
            try {
                if(ksms.sendSMS(to, message)){
                    return Response.status(Status.OK).entity("SMS Sent to: " + to).build();
                }else{
                    return Response.status(Status.OK).entity("SMS sending failed: " + to).build();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            return Response.status(Status.OK).entity("SMS sending failed: " + to).build();
	}

}
