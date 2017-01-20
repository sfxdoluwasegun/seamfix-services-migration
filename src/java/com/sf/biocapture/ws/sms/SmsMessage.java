package com.sf.biocapture.ws.sms;

import java.io.Serializable;

public class SmsMessage implements Serializable{
	
	private static final long serialVersionUID = 5736198697751379531L;

	private String sender;
	
	private String body;

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}
