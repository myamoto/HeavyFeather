package org.toolup.secu.oauth.jwt;

import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.oidc.JWTBuilderOIDC;

public abstract class JWTBuilderFactory {
	
	public static JWTBuilderFactory newInstance() throws OAuthException{
		return (JWTBuilderFactory) FactoryFinder.find(JWTBuilderFactory.class.getName(), JWTBuilderOIDC.class.getName());
	}

	public abstract JWT build(String token) throws OAuthException;
}
