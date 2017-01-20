package com.sf.biocapture.ws.helpmsg;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import com.sf.biocapture.app.BsClazz;
import com.sf.biocapture.app.KannelSMS;
import com.sf.biocapture.common.GenericException;
import com.sf.biocapture.entity.Fault;
import com.sf.biocapture.entity.Node;
import java.io.IOException;

@MessageDriven(activationConfig = {@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/bio/queue/HelpMessage")})
public class HelpMessageListener extends BsClazz implements MessageListener{

	@Inject
	private HelpMsgDS hmsm;

	@Inject
	private KannelSMS ksms;

	private String ticketRef = "KYCTR-";
	private String adminMobile;
	private DecimalFormat df = new DecimalFormat("##############################");

	@Override
	public void onMessage(Message msg) {
		try {
			ObjectMessage om = (ObjectMessage) msg;
			String sp = (String) om.getObject();
			try {
				doHelpMsgs(sp);
			} catch (GenericException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
				logger.error("Exception ", e);
			}
		} catch (JMSException e) {
			logger.error("Exception ", e);
		}
	}

	private void doHelpMsgs(String currentMsg) throws GenericException {
		String[] reqItems = currentMsg.split(hmsm.getDelimiter());
		String source = reqItems[0];// TAG::MAC_ADDRESS
		String tag = source.split("::")[0];
		String mac = source.split("::")[1];
		String sender = reqItems[reqItems.length - 1];
		Node node = hmsm.getNodeByMac(mac);
		String str = "";
		for(int r = 1; r < reqItems.length - 1; r++){
			str += reqItems[r] + "\n";
		}

		Fault fault = new Fault();
		fault.setNode(node);
		fault.setFaultStatus("UNRESOLVED");
		fault.setDescription(str);
		fault.setTicketNumber(ticketRef + df.format(new Date().getTime()));
		fault.setRecieptTimeStamp(new Timestamp(new Date().getTime()));
		if(hmsm.getDbService().create(fault) != null){
			String text = "Help Message recieved from client " + tag + " Sent by " + sender;
			alertSupport(text);
		}
	}

	private void alertSupport(String text){
		adminMobile = appProps.getProperty("admin-mobile", "8128008218, 7089326376, 7089323938, 8128008243");
		String[] msisdns = adminMobile.split(",");
		for(String msisdn: msisdns){
                    try {
                        if(!ksms.sendSMS(msisdn, text)){
                            logger.info("Help Message sms not sent to: " + msisdn);
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
		}
	}

}
