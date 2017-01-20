package com.sf.biocapture.ws.notification;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Nnanna
 * @since 10/11/2016
 */
public interface INotificationService {
	@POST
	@Path("/load")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({MediaType.APPLICATION_JSON})
	public NotificationResponse load(NotificationRequest request);
}
