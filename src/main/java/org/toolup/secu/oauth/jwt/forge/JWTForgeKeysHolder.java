package org.toolup.secu.oauth.jwt.forge;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.keys.OAuthKeyPairHolder;

public class JWTForgeKeysHolder {
	
	private static Logger logger = LoggerFactory.getLogger(JWTForgeKeysHolder.class);
	
	private static OAuthKeyPairHolder keys;
	
	private JWTForgeKeysHolder() {}
	
	private static OAuthKeyPairHolder getKeys() throws OAuthException {
		if(keys == null) {
			logger.info("initializing key pair...");
			keys = OAuthKeyPairHolder.newInstance();

			logger.info("global public key is {}", getPublicKeyString());
		}
		return keys;
	}
	
	public static String getPublicKeyString() throws OAuthException {
		return Base64.getEncoder().encodeToString(getKeys().getPublicKey().getEncoded());
	}
	
	public static PublicKey getPublicKey() throws OAuthException {
		return getKeys().getPublicKey();
	}
	
	public static PrivateKey getPrivateKey() throws OAuthException {
		return getKeys().getPrivateKey();
	}
}
