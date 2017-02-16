package com.sf.biocapture.ws.settings;

import com.sf.biocapture.app.BsClazz;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sf.biocapture.ws.access.SettingsResponse;

/**
 *
 * @author Nnanna
 * @since 23/01/2017
 *
 */
@Path("/settings")
public class SettingsService extends BsClazz implements ISettingsService {

    @Inject
    SettingsDs settingsDs;

    @Override
    public Response settings(String tag, String mac) {
        GlobalSettingResponse resp = new GlobalSettingResponse();
        try {
            if (tag != null && mac != null) {
                resp = settingsDs.getGlobalSettings();
            } else {
                return Response.ok(resp).status(Status.OK).build();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return Response.ok(resp).status(Status.OK).build();
    }

    @Override
    public Response clientSetting(String tag, String mac) {
        ClientSettingResponse clientSettingResponse = new ClientSettingResponse();
        try {
            if (tag != null && mac != null) {
                clientSettingResponse = settingsDs.getClientSettings();
            } else {
                return Response.ok(clientSettingResponse).status(Status.OK).build();
            }
        } catch (Exception e) {
            logger.error("", e);
        }
        return Response.ok(clientSettingResponse).status(Status.OK).build();
    }

}
