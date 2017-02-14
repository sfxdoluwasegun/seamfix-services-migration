/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sf.biocapture.ws.ota;

import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.KitMarker;
import com.sf.biocapture.entity.Node;
import com.sf.biocapture.entity.audit.VersionLog;
import com.sf.biocapture.entity.enums.VersionType;
import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.access.NodeData;
import java.util.Date;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import nw.orm.core.exception.NwormQueryException;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Marcel
 * @since 13-Feb-2017
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OTADS extends DataService {

    @Inject
    private BioCache cache;

    public OTAResponse handleOTASettings(OTARequest oTARequest) {
        OTAResponse oTAResponse = new OTAResponse();
        try {
            NodeData nd = cache.getItem("NODEDATA-" + oTARequest.getMacAddress(), NodeData.class); // mac must not be null
            if (nd == null) {
                // Retrieve node data from database
                nd = new NodeData();
                Node node = dbService.getByCriteria(Node.class, Restrictions.eq("macAddress", oTARequest.getMacAddress()));
                if (node != null) {
                    if (node.getLastInstalledUpdate() == null || (node.getLastInstalledUpdate() != null && !oTARequest.getPatchVersion().equals(node.getLastInstalledUpdate()))) {
                        node.setLastInstalledUpdate(oTARequest.getPatchVersion());
                        node.setLastUpdated(new Date(oTARequest.getInstallDate().getTime()));

                        node.setMachineManufacturer(oTARequest.getMachineManufacturer());
                        node.setMachineModel(oTARequest.getMachineModel());
                        node.setMachineOS(oTARequest.getMachineOS());
                        node.setMachineSerialNumber(oTARequest.getMachineSerial());
                        boolean success = dbService.update(node);
                        logger.debug("Node update successful? " + success);

                        VersionLog versionLog = new VersionLog();
                        versionLog.setType(VersionType.KIT_VERSION);
                        versionLog.setVersion(String.valueOf(oTARequest.getPatchVersion()));
                        versionLog.setNode(node);
                        dbService.create(versionLog);
                    }

                    nd.setCorporateKit(node.getCorperateKit());
                    nd.setMacId(node.getMacAddress());
                    Float version = node.getLastInstalledUpdate();
                    nd.setPatchVersion(version);

                    confirmUpdateAvailable(oTAResponse, version, oTARequest);

                    // cache node data
                    cache.setItem("NODEDATA-" + oTARequest.getMacAddress(), nd, 30 * 60);
                }
            } else {
                confirmUpdateAvailable(oTAResponse, nd.getPatchVersion(), oTARequest);
            }
        } catch (NwormQueryException e) {
            logger.error("", e);
        }

        oTAResponse.setCode(ResponseCodeEnum.SUCCESS);
        oTAResponse.setDescription("Success");

        return oTAResponse;
    }

    private void confirmUpdateAvailable(OTAResponse otar, Float patchVersion, OTARequest otar1) {
        Float cv = 0f;
        boolean useList = false;
        try {
            cv = appProps.getFloat("SC_" + otar1.getDeviceType(), 1.23f); // type DROID, WIN, LITE
            useList = appProps.getBool("use-update-kit-list", false);
        } catch (NumberFormatException ex) {
            logger.error("", ex);
        }
        if (useList) {
            KitMarker kMarker = getDbService().getByCriteria(KitMarker.class, Restrictions.eq("macAddress", otar1.getMacAddress().trim()));
            if ((kMarker == null)) {
                otar.setUpdateAvailable(false);
                return;
            }
            setUpdateAvailable(otar, otar1.getDeviceType(), patchVersion, cv);
        } else {
            setUpdateAvailable(otar, otar1.getDeviceType(), patchVersion, cv);
        }
    }

    private void setUpdateAvailable(OTAResponse sr, String type, Float patchVersion, float cv) {
        if ((patchVersion != null) && (cv > patchVersion)) {
            sr.setUpdateAvailable(true);
            sr.setUpdateVersion(appProps.getFloat("SC_" + type, 1.23f)); // type DROID, WIN, LITE
        } else {
            sr.setUpdateAvailable(false);
        }
    }
}
