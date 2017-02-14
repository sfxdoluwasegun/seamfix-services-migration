/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.ota;

import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.ResponseData;
import java.util.Date;

/**
 *
 * @author Marcel
 * @since 13-Feb-2017
 */
public class OTAResponse extends ResponseData {

    private boolean updateAvailable;
    private Float updateVersion;
    private Date releaseDate;

    public OTAResponse() {
        setCode(ResponseCodeEnum.ERROR);
        setDescription(ResponseCodeEnum.ERROR.getDescription());
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
    }

    public Float getUpdateVersion() {
        return updateVersion;
    }

    public void setUpdateVersion(Float updateVersion) {
        this.updateVersion = updateVersion;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

}
