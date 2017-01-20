package com.sf.biocapture.ws.sms;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/receivesms")
public class SmsService implements ISmsService{
	
	@Inject
	private SmsDS ssm;

	@Override
	@PermitAll
	public Response getSms(String sender, String body) {
		String respnse = ssm.receive(sender, body);
		return Response.status(200).entity(respnse).build();
	}

	@Override
	public Response postSms(String sender, String body) {
		return getSms(sender, body);
	}
	
}
