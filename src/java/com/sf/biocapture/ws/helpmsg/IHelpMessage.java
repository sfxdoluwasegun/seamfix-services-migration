package com.sf.biocapture.ws.helpmsg;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public interface IHelpMessage {
	
	@GET
	@Path("/")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getMessage(@QueryParam("req") String req);
	
	@POST
	@Path("/")
	@Produces({MediaType.TEXT_PLAIN})
	public Response postMessage(@FormParam("req") String req);

}
