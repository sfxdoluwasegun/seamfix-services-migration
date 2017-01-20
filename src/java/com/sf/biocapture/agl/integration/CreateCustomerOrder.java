package com.sf.biocapture.agl.integration;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.mtn.esf.xmlns.wsdl.createcustomerorder.CreateCustomerOrderPortType;
import com.mtn.esf.xmlns.xsd.common.CommonComponentsType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.AdditionalInfoType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.AdditionalStatusType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.CreateCustomerOrderRequestType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.CreateCustomerOrderResponseType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.EffectiveTimePeriodType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.IdentificationType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.IdentificationType2;
import com.mtn.esf.xmlns.xsd.createcustomerorder.ProductDetailsType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.SalesOrderHeaderType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.SalesOrderLineType;
import com.mtn.esf.xmlns.xsd.createcustomerorder.StatusInfoType;
import com.sf.biocapture.ds.DataService;
import com.sf.biocapture.ws.ResponseCodeEnum;
import com.sf.biocapture.ws.swap.SimSwapResponse;

/**
 * Calls ESF to do sim swap
 *
 * @author Nnanna
 * @since 27/10/2016
 *
 */
@Stateless
public class CreateCustomerOrder extends DataService {

    private static final String _234 = "234";

    @Inject
    private SimSwapIntegrator simSwapIntegrator;

    @SuppressWarnings("PMD")
    public SimSwapResponse doSimSwap(String msisdn, String transactionId, String orderNumber, String newPUK, String newSimSerial) {
        CreateCustomerOrderPortType port = null;
        try {
            port = simSwapIntegrator.getCustomerOrderPort();
            CreateCustomerOrderRequestType req = new CreateCustomerOrderRequestType();
            CommonComponentsType cType = new CommonComponentsType();
            if (msisdn != null) {
                cType.setMSISDNNum(_234 + msisdn.replaceFirst("0", "")); //strip the leading zero
            }
            cType.setOpCoID("NG");
            cType.setSenderID(appProps.getProperty("ss-sender-id", "Channel"));
            cType.setProcessingNumber(transactionId);
            //
            req.setCommonComponents(cType);
            req.setLevelCode(appProps.getProperty("ss-level-code", "ServiceLevel"));
            req.setExtUser(appProps.getProperty("ss-ext-user", "Biosmartuser"));
            req.setPrefLang("en");
            req.setRequestType(appProps.getProperty("ss-req-type", "SIMSwap"));
            //
            SalesOrderHeaderType salesOrder = new SalesOrderHeaderType();
            IdentificationType2 idType = new IdentificationType2();
            idType.setIdType("OrderNumber");
            idType.setIdValue(orderNumber == null ? transactionId : orderNumber);
            salesOrder.getIdentification().add(idType);
            salesOrder.setOrderType(appProps.getProperty("ss-order-type", "SIMChange"));
            req.setSalesOrderHeader(salesOrder);
            //
            SalesOrderLineType salesOrderLine = new SalesOrderLineType();
            IdentificationType idType2 = new IdentificationType();
            idType2.setIdType("ServiceID");
            idType2.setIdValue(_234 + msisdn.replaceFirst("0", ""));
            salesOrderLine.getIdentification().add(idType2);
            //
            salesOrderLine.setServiceActionCode(appProps.getProperty("ss-service-action-code", "Immediate"));
            salesOrderLine.setReasonCode(appProps.getProperty("ss-reason-code", "FSIM"));
            salesOrderLine.setNarration(appProps.getProperty("ss-narration", "sim swap"));
            //
            EffectiveTimePeriodType effectiveTime = new EffectiveTimePeriodType();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(sdf.format(new Date()));
                effectiveTime.setStartDateTime(xmlDate);
                salesOrderLine.setEffectiveTimePeriod(effectiveTime);
            } catch (DatatypeConfigurationException e) {
                logger.error("", e);
            }
            //
            ProductDetailsType productDetails = new ProductDetailsType();
            productDetails.setTypeCode(appProps.getProperty("ss-type-code", "SIMPO"));
            AdditionalInfoType additionalInfo1 = new AdditionalInfoType();
            additionalInfo1.setName("NewPUKNumber");
            additionalInfo1.setValue(newPUK);
            AdditionalInfoType additionalInfo2 = new AdditionalInfoType();
            additionalInfo2.setName("NewSIMNumber");
            additionalInfo2.setValue(newSimSerial);
            productDetails.getAdditionalInfo().add(additionalInfo1);
            productDetails.getAdditionalInfo().add(additionalInfo2);
            salesOrderLine.getItemDetails().add(productDetails);
            req.getSalesOrderLine().add(salesOrderLine);
            //
            String requestXml = simSwapIntegrator.getMessage(CreateCustomerOrderRequestType.class, req);
            logger.debug("requestXml: " + requestXml);

            CreateCustomerOrderResponseType resp = null;
            try {
                resp = port.createCustomerOrderOperation(req);
            } catch (Exception ex) {
                logger.error("Exception thrown in getting sim swap response from server:", ex);
                return new SimSwapResponse(ResponseCodeEnum.ERROR);
            }

            String responseXml = simSwapIntegrator.getMessage(CreateCustomerOrderResponseType.class, resp);
//		logger.debug("responseXml: " + responseXml);

            StatusInfoType info = resp.getStatusInfo();
            logger.debug("Code: " + info.getStatusCode() + "; Description: " + info.getStatusDesc());

            //log request and response
            logActivity(requestXml, responseXml, info.getStatusCode(), info.getStatusDesc(), newSimSerial, "SIM_SWAP", msisdn);

            String clientResp = null;
            //get a better description of the failure response
            if (info.getStatusCode() != null && !info.getStatusCode().equalsIgnoreCase("0")) {
                for (AdditionalStatusType sType : info.getAdditionalStatus()) {
                    clientResp = info.getStatusDesc() + " - " + sType.getStatusDesc();
                    break;
                }
            } else {
                clientResp = "SIM Swap submission successful";
            }

            return new SimSwapResponse(info.getStatusCode(), clientResp);
        } catch (Exception e) {
            logger.error("Error in getting sim swap port:", e);
            return new SimSwapResponse(ResponseCodeEnum.ERROR);
        }
    }
}
