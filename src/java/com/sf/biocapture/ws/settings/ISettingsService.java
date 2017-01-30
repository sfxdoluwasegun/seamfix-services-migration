package com.sf.biocapture.ws.settings;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * 
 * @author Nnanna
 * @since 23/01/2017
 *
 */
public interface ISettingsService {
	@POST
	@Path("/global")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response settings(@FormParam("tag") String tag, @FormParam("mac") String mac);
}
