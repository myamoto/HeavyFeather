package org.toolup.secu.oauth.jwt.keys;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

public class OAuthKeyPairDefault extends OAuthKeyPairHolder {
	
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public OAuthKeyPairDefault() throws NoSuchAlgorithmException {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(4096, SecureRandom.getInstanceStrong());
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
	}
	
	@Override
	public PublicKey getPublicKey() {
		return publicKey;
	}

	@Override
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

}
