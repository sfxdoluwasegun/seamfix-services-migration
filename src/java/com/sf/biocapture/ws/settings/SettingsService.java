package com.sf.biocapture.ws.settings;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * 
 * @author Nnanna
 * @since 23/01/2017
 * 
 */

@Path("/settings")
public class SettingsService implements ISettingsService {
	@Inject
	SettingsDs settings;
	
	@Override
	public Response settings(String tag, String mac) {
		ClientSettings resp = new ClientSettings();
		if(tag != null && mac != null){
			resp = settings.getClientSettings();
		}
		return Response.ok(resp).status(Status.OK).build();
	}

}