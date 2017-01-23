package com.sf.biocapture.ws.settings;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.ws.access.SettingsResponse;

/**
 * 
 * @author Nnanna
 * @since 23/01/2017
 * 
 */

@Path("/settings")
public class SettingsService implements ISettingsService {
	
	@Inject
	AccessDS access;

	@Override
	public Response settings(String tag, String mac) {
		SettingsResponse resp = new ClientSettings();
		if(tag != null && mac != null){
			resp = access.getGlobalSettings();
		}
		return Response.ok(resp).status(Status.OK).build();
	}

}
