package org.toolup.secu.oauth.jwt.keys;

import java.security.PrivateKey;
import java.security.PublicKey;

import org.toolup.javaapi.FactoryFinder;
import org.toolup.javaapi.FactoryFinderException;
import org.toolup.secu.oauth.OAuthException;

public abstract class OAuthKeyPairHolder {

	public static OAuthKeyPairHolder newInstance() throws  OAuthException {
		try {
			return (OAuthKeyPairHolder) FactoryFinder.find(OAuthKeyPairHolder.class.getName(), OAuthKeyPairDefault.class.getName());
		} catch (FactoryFinderException e) {
			throw new OAuthException(e, 500);
		}
	}

	public abstract PublicKey getPublicKey();
	public abstract PrivateKey getPrivateKey();

}
