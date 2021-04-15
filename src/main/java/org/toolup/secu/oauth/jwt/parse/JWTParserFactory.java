package org.toolup.secu.oauth.jwt.parse;

import java.security.PublicKey;
import java.util.Hashtable;

import org.toolup.javaapi.FactoryFinder;
import org.toolup.javaapi.FactoryFinderException;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JWT;

public abstract class JWTParserFactory {
	
	public static JWTParserFactory newInstance() throws OAuthException{
		try {
			return (JWTParserFactory) FactoryFinder.find(JWTParserFactory.class.getName(), JWTParserDefault.class.getName());
		} catch (FactoryFinderException e) {
			throw new OAuthException(e, 500);
		}
	}

	public abstract JWT parse(String token) throws OAuthException;
	
	public abstract Hashtable<String, PublicKey> getPublicKeys() throws OAuthException;
	public abstract PublicKey getDefaultPublicKey() throws OAuthException;
}
