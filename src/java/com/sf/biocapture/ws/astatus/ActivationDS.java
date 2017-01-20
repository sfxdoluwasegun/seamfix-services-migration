package com.sf.biocapture.ws.astatus;

import com.sf.biocapture.agl.integration.AgilityResponse;
import com.sf.biocapture.agl.integration.FetchSubscriptionListIntegrator;
import com.sf.biocapture.agl.integration.GetSubscriberDetails;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import nw.orm.core.query.QueryAlias;
import nw.orm.core.query.QueryModifier;

import com.sf.biocapture.app.BioCache;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.entity.SmsActivationRequest;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.criterion.Junction;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ActivationDS extends DataService {

    private static final String PHONE_NUMBER = "phoneNumber";

    @Inject
    private BioCache cache;
    private SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyyhhmmss");

    @Inject
    FetchSubscriptionListIntegrator subscriptionStatus;

    @Inject
    GetSubscriberDetails getSubscriberDetails;

    public AStatus checkStatus(String key, String uniqueId, boolean useMsisdn) {
        logger.debug("**** Inside check status of Activation Status : msisdn/serial : " + key + " ; uniqueId : " + uniqueId);
        if ((key == null) || (key.isEmpty())) {
            return getActivationStatus(-1, "MSISDN/Serial cannot be empty. Please specify one and try again.", null, null, null);
        }

        if (useMsisdn && ((uniqueId == null) || uniqueId.isEmpty())) {
            Calendar cal = Calendar.getInstance();
            Date d = cal.getTime();
            uniqueId = "SFX_" + key + "_" + sdf.format(d);
        } else if (!useMsisdn && ((uniqueId == null) || uniqueId.isEmpty())) {
            return getActivationStatus(-1, "Unique Id cannot be empty. Please specify one and try again.", null, null, null);
        }

        if ((useMsisdn && (key.length() != 11)) || (!useMsisdn && (key.length() != 20))) {
            return getActivationStatus(-1, "Invalid msisdn/serial specified. Msisdn must be 11 digits while serials must be 20 digits.", null, null, null);
        }

        key = key.replaceAll("[^\\d.]", "").replaceAll("\\W", "");
        String resp = cache.getItem(key + "-AS-" + uniqueId, String.class);

        if ((resp == null) || (resp.isEmpty())) {
            return getActivationStatusFromAgility(key, uniqueId, useMsisdn);
        }
        logger.debug("ActivationDS : resp at no go area is : " + resp);
        String[] a = resp.split(",");
        if ((a != null) && (a.length >= 3) && NumberUtils.isDigits(a[1])) {
            return getActivationStatus(0, "Successfully retrieved the activation status", a[0], getTimestampFromTime(Long.valueOf(a[1])), a[2]);
        } else {
            return getActivationStatusFromAgility(key, uniqueId, true);
        }
    }

    private Timestamp getTimestampFromTime(long time) {
        return new Timestamp(time);
    }

    public AStatus getActivationStatusFromAgility(String key, String uniqueId, boolean useMsisdn) {

        String msisdn = "";

        if (!useMsisdn) {  // This is for sim serial. Currently not supported.
            QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
            qm.setPaginated(0, 1);
            qm.addOrderBy(Order.desc("msisdnUpdateTimestamp"));
            qm.addProjection(Projections.property(PHONE_NUMBER));
            Junction j = Restrictions.conjunction().add(Restrictions.isNotNull("msisdnUpdateTimestamp")).
                    add(Restrictions.eq("serialNumber", key));//.add(Restrictions.isNotEmpty("msisdnUpdateTimestamp"));
            String phoneNumber = getDbService().getByCriteria(String.class, qm, j);
            logger.debug("The serial is : " + key + " : the msisdn returned is : " + phoneNumber);
            if ((phoneNumber == null) || phoneNumber.isEmpty()) {
                return getActivationStatus(-1, "No msisdn associated with this serial. Try again later.", null, null, null);
            } else {
                return getActivationStatusFromAgility(phoneNumber, uniqueId, true);
            }
        } else {
            msisdn = key;
        }

        AgilityResponse response = getSubscriberDetails.getSubscriberDetails(msisdn, null, uniqueId);

//                    AgilityResponse response  = subscriptionStatus.fetchServiceLevelSubscriptionList(msisdn, uniqueId);
        if ((response == null) || (response.getCode() == null) || (response.getCode().equalsIgnoreCase("-2"))) {//network or an unknown error 
            return getActivationStatus(-2, "A network error occurred. Failed to connect to service.", null, null, null);
        }

        //if response indicates that msisdn does not exist
        if (response.getCode() != null && !response.getCode().equalsIgnoreCase("0")) {
            return getActivationStatus(-1, "Either the msisdn or sim serial is invalid.", null, null, null);
        }

        //if unique ID returned from response does not match unique id sent
        if (response.getBioData() != null && response.getBioData().getUniqueId() != null && !response.getBioData().getUniqueId().equalsIgnoreCase(uniqueId)) {
            return getActivationStatus(-1, "MSISDN/Sim serial has not been activated.", null, null, null);
        }

        AStatus aStatus = getActivationStatus(0, "Successfully retrieved the activation status", response.getActivationStatus(),
                response.getActivationDate(), response.getBioData().getUniqueId());
        String resp = response.getActivationStatus() + "," + response.getActivationDate().getTime() + "," + response.getBioData().getUniqueId();
        cache.setItem(key + "-AS-" + uniqueId, resp, 60 * 5);// chill for 5 mins   
        logger.debug("**** status response is : " + resp);

        return aStatus;
    }

    public AStatus checkStatus(String msisdn) {
        if ((msisdn == null) || (msisdn.isEmpty())) {
            return getActivationStatus(-1, "MSISDN cannot be empty. Please specify one and try again.", null, null, null);
        }

        msisdn = msisdn.replaceAll("[^\\d.]", "").replaceAll("\\W", "");
        String response = cache.getItem(msisdn + "-CAS", String.class);
        if (response == null) {
            AgilityResponse ar = subscriptionStatus.fetchStatusLevelSubscriptionList(msisdn, "SFX_" + msisdn + "_" + sdf.format(new Date()));

            if (ar != null && ar.getCode() != null && ar.getCode().equalsIgnoreCase("-2")) {
                return getActivationStatus(-2, "Unable to connect to remote service.", null, null, null);
            }

            if (ar != null && ar.getCode() != null && !ar.getCode().equalsIgnoreCase("0")) {
                return getActivationStatus(-3, ar.getDescription(), null, null, ""); //desc at this time is most likely 'Failure'
            }

            cache.setItem(msisdn + "-CAS", ar.getAssetStatus(), 60 * 5);// chill for 5 mins	                
            return getActivationStatus(0, "Successfully retrieved the eligibility status.", ar.getAssetStatus(), null, "");
        }

        return getActivationStatus(0, "Successfully retrieved the eligibility status.", response, null, null);
    }

    public String checkStatusOld(String msisdn) {
        String key = "-AS";
        msisdn = msisdn.replaceAll("[^\\d.]", "").replaceAll("\\W", "");
        String response = cache.getItem(msisdn + key, String.class);
        if (response == null) {
            List<AStatus> ad = getActivationData(msisdn);
            if (ad != null && !ad.isEmpty()) {
                AStatus as = ad.get(0); // get the first entry (most recent)
                AStatus sms = ad.get(ad.size() - 1); // oldest
                String status = "NOT_ACTIVATED";
                Long d = as.getActivationTimestamp() != null ? as.getActivationTimestamp().getTime() : new Date().getTime();
                if ("ACTIVATED".equalsIgnoreCase(as.getStatus()) || "ACTIVATED_WITH_SMS_UNSENT".equalsIgnoreCase(as.getStatus())) {
                    status = "ACTIVATED";
                    logger.debug(msisdn + " STATUS: " + as.getStatus() + " TIME ACTIVATED " + as.getActivationTimestamp());
                    d = as.getActivationTimestamp() != null ? as.getActivationTimestamp().getTime() : new Date().getTime();
                    response = status + "," + d + "," + sms.getUniqueId();
                    cache.setItem(msisdn + key, response, 60 * 60 * 24);
                } else if ("ACTIVATION_REQUEST_PENDING".equalsIgnoreCase(as.getStatus())
                        || "ACTIVATION_PENDING".equalsIgnoreCase(as.getStatus()) || "ACTIVATION_RESPONSE_PENDING".equalsIgnoreCase(as.getStatus())) {
                    response = "REGISTERED,NA,NA";
                    cache.setItem(msisdn + key, response, 60 * 5);// chill for 5 mins
                } else if ("NOT_ACTIVATED".equalsIgnoreCase(as.getStatus()) || "CHURNED".equalsIgnoreCase(as.getStatus())) {
                    response = status + "," + d + "," + sms.getUniqueId();
                    cache.setItem(msisdn + key, response, 60 * 60 * 24);// 24 hrs
                }
            } else if (isChurned(msisdn)) {
                response = "CHURNED," + new Date().getTime() + ",XX"; // possibly not registered, keep in cache for 20 minutes
                cache.setItem(msisdn + key, response, 60 * 20);
            } else {
                response = "NOT_REGISTERED,NA,NA"; // possibly not registered, keep in cache for 20 minutes
                cache.setItem(msisdn + key, response, 60 * 20);
            }
        }
        return response;
    }

    private Boolean isChurned(String msisdn) {
        QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
        qm.addAlias(new QueryAlias("phoneNumberStatus", "pns"));
        qm.addProjection(Projections.count(PHONE_NUMBER));
        return dbService.getByCriteria(Long.class, qm, Restrictions.eq(PHONE_NUMBER, msisdn), Restrictions.isNull("pns.churnTimestamp")) > 0;
    }

    private List<AStatus> getActivationData(String msisdn) {
        QueryModifier qm = new QueryModifier(SmsActivationRequest.class);
        qm.addAlias(new QueryAlias("phoneNumberStatus", "pns"));
        qm.addOrderBy(Order.desc("receiptTimestamp"));
        qm.addProjection(Projections.property(PHONE_NUMBER).as(PHONE_NUMBER));
        qm.addProjection(Projections.property("pns.status").as("status"));
        qm.addProjection(Projections.property("uniqueId").as("uniqueId"));
        qm.addProjection(Projections.property("pns.finalTimestamp").as("activationTimestamp"));
        qm.transformResult(true);
        return dbService.getListByCriteria(AStatus.class, qm,
                Restrictions.eq(PHONE_NUMBER, msisdn), Restrictions.isNull("pns.churnTimestamp"));
    }

    public void setStatus(String msisdn, Long date, String status) {
//        if ("SUCCESS".equalsIgnoreCase(status)) {
//
//        }
    }

    public AStatus getActivationStatus(int code, String message, String activationStatus, Timestamp activationDate, String uniqueId) {
        AStatus aStatus = new AStatus();
        aStatus.setCode(code);
        aStatus.setMessage(message);
        aStatus.setActivationTimestamp(activationDate);
        aStatus.setUniqueId(uniqueId);
        aStatus.setStatus(activationStatus);
        return aStatus;
    }

}
