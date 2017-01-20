package com.sf.biocapture.ws.helpmsg;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;

import com.sf.biocapture.app.JmsSender;
import com.sf.biocapture.ds.DataService;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class HelpMsgDS extends DataService {

	private JmsSender appSession;

	public HelpMsgDS() {
		// Default Mechanism
	}

	@Inject
	public HelpMsgDS(JmsSender appSession) {
		this.appSession = appSession;
	}

	public String getMessage(String msg){
		if(msg == null){
			return "false";
		}else{
			logger.info("Help Message Received: " + msg);
			return appSession.queueHelpMessage(msg);
		}
	}

}
