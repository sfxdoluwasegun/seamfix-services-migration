package com.sf.biocapture;

import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TimerService;
import javax.inject.Inject;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.ds.AccessDS;
import com.sf.biocapture.krm.ZonalKrm;
import com.sf.biocapture.postactivation.PostActivationService;
import com.sf.biocapture.sim.churn.ChurnProcessor;
import com.sf.biocapture.sync.PostSyncValidator;
import com.sf.biocapture.ws.resync.ResyncJob;

@Singleton
@Startup
@DependsOn("BioService")
public class BioScheduler extends BsClazz {

    @Resource
    protected TimerService timerService;

    @Inject
    protected PostActivationService pas;

    @Inject
    protected ResyncJob rsync;

    @Inject
    protected AccessDS accessDS;

    @Inject
    protected ChurnProcessor churnProcessor;

    @Inject
    protected PostSyncValidator psv;

    @Inject
    protected ZonalKrm zkrm;
    
    public class TimeZone {

        public static final String AFRICA_LAGOS = "Africa/Lagos";
    }

    @Schedule(hour = "*", timezone = TimeZone.AFRICA_LAGOS, persistent = false)
    public void testTimer() {
        logger.debug("Timer Test executed");
    }

    @Schedule(dayOfMonth = "*", dayOfWeek = "*", hour = "*", minute = "*/20", timezone = TimeZone.AFRICA_LAGOS, persistent = false)
    public void zonalSyncSummarization() {
        if (appProps.getInt("process-krm", -1) == 1) {
            logger.debug("KYC Report Model Processor Activated");
            zkrm.loadZonalSyncData();
        } else {
            logger.debug("KYC Report Model Processor Not Activated");
        }
    }

    @Schedule(dayOfMonth = "*", dayOfWeek = "*", hour = "8,16", timezone = TimeZone.AFRICA_LAGOS, persistent = false)
    public void cleanNullActivationTimestamp() {
        if (appProps.getInt("process-null-activation-timestamp", -1) == 1) {
            logger.debug("KYC Null Activation Timestamp Processor Activated");
            psv.updateNullBiometrics();
        } else {
            logger.debug("Null clean up service Not Activated");
        }
    }

    @Schedule(dayOfMonth = "*", hour = "0,2,4,8-23", timezone = TimeZone.AFRICA_LAGOS, persistent = false)// 08080595525
    public void cleanUpUnsentSMS() {
        if (appProps.getInt("process-activated-with-sms-unsent", -1) == 1) {
            pas.notifyActivatedSubscribers();
        } else {
            logger.info("Unsent activation sms not checked");
        }
    }

    @Schedule(dayOfMonth = "*", hour = "23", timezone = TimeZone.AFRICA_LAGOS, persistent = false)// 08080595525
    public void resyncRecords() {
        if (appProps.getInt("process-resync-records", -1) == 1) {
            logger.debug("Resync Listing initiated");
            rsync.listResyncRecords();
        }
    }

    @Schedule(dayOfMonth = "*", hour = "*", minute = "*/30", timezone = TimeZone.AFRICA_LAGOS, persistent = false)
    public void loadBlackList() {
        accessDS.preloadBlacklist();
    }

    @Schedule(dayOfMonth = "*", dayOfWeek = "*", hour = "*", minute = "*/15", timezone = TimeZone.AFRICA_LAGOS, persistent = false)
    public void processChurnList() {
        if (appProps.getInt("process-churn-list", -1) == 1) {
            appProps.setProperty("process-churn-list", "0");
            churnProcessor.processChurnedLines();
        } else {
            logger.info("Churn List processing not activated");
        }
    }
   
}