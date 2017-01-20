package com.sf.biocapture.ws.sms;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface ISmsService {

	@GET
	@Path("/")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getSms(@QueryParam("sender") String sender, @QueryParam("body") String body);

	@POST
	@Path("/")
	@Produces({MediaType.TEXT_PLAIN})
	public Response postSms(@FormParam("sender") String sender, @FormParam("body") String body);

}
