package com.sf.biocapture.ws.usecase;

import javax.ws.rs.Path;

import com.sf.biocapture.ws.biodata.BioDataResponse;

/**
 * Service implementations for validating
 * client use cases
 * TODO: use this class to refactor/implement
 * all use case validations
 * @author Nnanna
 * @since 23/01/2017
 */

@Path("/usecase")
public class UseCaseValidation implements IUseCaseValidation {

	@Override
	public BioDataResponse validateAll(String msisdn, String serial, String puk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BioDataResponse validateMsisdn(String msisdn, String puk) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BioDataResponse validateSerial(String serial, String puk) {
		// TODO Auto-generated method stub
		return null;
	}

}
