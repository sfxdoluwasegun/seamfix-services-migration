package com.sf.biocapture;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

import com.sf.biocapture.app.BsClazz;

import sfx.crypto.CryptoReader;

@Singleton
public class CryptController extends BsClazz{

	private Crypter crypter;

	@PostConstruct
	public void init(){
		CryptoReader cr = new CryptoReader("map");
		try {
			crypter = new Crypter(cr);
		} catch (KeyczarException e) {
			logger.error("Exception ", e);
		}
	}

	public Crypter getCrypter() {
		return crypter;
	}

}
