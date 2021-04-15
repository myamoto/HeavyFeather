package org.toolup.secu.oauth.jwt.parse.keys;

import java.security.PublicKey;
import java.util.Hashtable;

import org.toolup.javaapi.FactoryFinderException;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.forge.JWTForgeKeysHolder;

public class JWTForgeKeyCache implements IKeysHolder {

	private static final Hashtable<String, PublicKey> EMPTY = new Hashtable<String, PublicKey>();
	
	public JWTForgeKeyCache() throws FactoryFinderException {}
	
	@Override
	public Hashtable<String, PublicKey> getPublicKeys() throws OAuthException {
		return EMPTY;
	}

	@Override
	public PublicKey getDefaultPublicKey() throws OAuthException {
		return JWTForgeKeysHolder.getPublicKey();
	}

	@Override
	public PublicKey getPublicKey(String kid) throws OAuthException {
		return JWTForgeKeysHolder.getPublicKey();
	}


}
