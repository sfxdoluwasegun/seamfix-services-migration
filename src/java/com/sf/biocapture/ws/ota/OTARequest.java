/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.ota;

import java.sql.Timestamp;
import java.util.Date;

/**
 *
 * @author Marcel
 * @since 13-Feb-2017
 */
public class OTARequest {

    private String kitTag;
    private String macAddress;

    private Float patchVersion;
    private Date installDate;

    private String machineModel;
    private String machineSerial;
    private String machineManufacturer;
    private String machineOS;

    private String deviceType;

    public String getKitTag() {
        return kitTag;
    }

    public void setKitTag(String kitTag) {
        this.kitTag = kitTag;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Float getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(Float patchVersion) {
        this.patchVersion = patchVersion;
    }

    public Date getInstallDate() {
        return installDate;
    }

    public void setInstallDate(Date installDate) {
        this.installDate = installDate;
    }

    public String getMachineModel() {
        return machineModel;
    }

    public void setMachineModel(String machineModel) {
        this.machineModel = machineModel;
    }

    public String getMachineSerial() {
        return machineSerial;
    }

    public void setMachineSerial(String machineSerial) {
        this.machineSerial = machineSerial;
    }

    public String getMachineManufacturer() {
        return machineManufacturer;
    }

    public void setMachineManufacturer(String machineManufacturer) {
        this.machineManufacturer = machineManufacturer;
    }

    public String getMachineOS() {
        return machineOS;
    }

    public void setMachineOS(String machineOS) {
        this.machineOS = machineOS;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

}
