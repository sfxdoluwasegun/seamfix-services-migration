package com.sf.biocapture.ws.helpmsg;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/msginbox")
public class HelpMessage implements IHelpMessage {

	@Inject
	private HelpMsgDS hmsm;

	@Override
	@RolesAllowed({"smartclient", "lite"})
	public Response getMessage(String req) {
		String response = hmsm.getMessage(req);
		return Response.status(200).entity(response).build();
	}

	@Override
	public Response postMessage(String req) {
		return getMessage(req);
	}

}
