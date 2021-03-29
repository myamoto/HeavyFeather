package org.toolup.secu.oauth.jwt;

import java.security.PublicKey;
import java.util.Hashtable;

import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.oidc.JWTBuilderOIDC;

public abstract class JWTBuilderFactory {
	
	public static JWTBuilderFactory newInstance() throws OAuthException{
		return (JWTBuilderFactory) FactoryFinder.find(JWTBuilderFactory.class.getName(), JWTBuilderOIDC.class.getName());
	}

	public abstract JWT build(String token) throws OAuthException;
	
	public abstract Hashtable<String, PublicKey> getPublicKeys() throws OAuthException;
	public abstract PublicKey getDefaultPublicKey() throws OAuthException;
}
