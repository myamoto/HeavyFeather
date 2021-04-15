package org.toolup.secu.oauth.jwt.forge;

import java.util.Collection;
import java.util.Map;

import org.toolup.javaapi.FactoryFinder;
import org.toolup.javaapi.FactoryFinderException;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JWT;

public abstract class JWTForgeFactory {
	
	public static JWTForgeFactory newInstance() throws OAuthException{
		try {
			return ((JWTForgeFactory) FactoryFinder.find(JWTForgeFactory.class.getName(), JWTForgeOIDC.class.getName()));
		} catch (FactoryFinderException e) {
			throw new OAuthException(e, 500);
		}
	}
	

	public abstract JWT forge(final String scope,
			final String subject, final Collection<String> roles,
			final Map<String, String> attributes) throws OAuthException;
	
}
