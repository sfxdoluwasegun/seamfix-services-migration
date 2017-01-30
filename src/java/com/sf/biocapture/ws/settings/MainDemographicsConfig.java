package com.sf.biocapture.ws.settings;

/**
 * 
 * @author Nnanna
 * @since 24/01/2017
 */
public class MainDemographicsConfig {
	private String field;
	private boolean mandatory = Boolean.TRUE;
	
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public boolean isMandatory() {
		return mandatory;
	}
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}
}
