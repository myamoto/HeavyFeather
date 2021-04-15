package org.toolup.secu.oauth.jwt.forge;

import java.security.SecureRandom;
import java.security.Signature;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JSONObject;
import org.toolup.secu.oauth.jwt.JWT;

public class JWTForgeOIDC extends JWTForgeFactory{
	
	private static final String JWT = "JWT";
	private static final String JWT_ALGORITHM = "RS256";
	private static final String SIG_ALGORITHM = "SHA256withRSA";
	
	public JWT forge(final String scope,
			final String subject, final Collection<String> roles,
			final Map<String, String> attributes) throws OAuthException {
			
		try {
			JSONObject header = JSONObject.create();
			JSONObject claims = JSONObject.create();

			header.put("typ", JWT);
			header.put("alg", JWT_ALGORITHM);

			long now = System.currentTimeMillis();

			claims.put("iat", now);
			claims.put("nbf", now - 10000L);
			claims.put("exp", now + 300000L);
			claims.put("aud", "urn:org:toolup:oauth");
			claims.put("iss", "urn:org:toolup:oauth");
			claims.put("jti", generateIdentifier());
			claims.put("sub", subject);
			claims.put("scope", scope);
			claims.put("roles", roles);
			claims.putAll(attributes);

			Signature signature = Signature.getInstance(SIG_ALGORITHM);

			String token = Base64.encodeBase64URLSafeString(header.toString().getBytes(CharEncoding.UTF_8));
			token += "." + Base64.encodeBase64URLSafeString(claims.toString().getBytes(CharEncoding.UTF_8));

			signature.initSign(JWTForgeKeysHolder.getPrivateKey());
			signature.update(token.getBytes(CharEncoding.UTF_8));

			token += "." + Base64.encodeBase64URLSafeString(signature.sign());
			return new JWT()
					.token(token)
					.claims(claims)
					.header(header);
		} catch (Exception e) {
			throw new OAuthException(e);
		}
	}

	private static String generateIdentifier() throws OAuthException {
		try {
			byte[] b = new byte[16];

			SecureRandom.getInstance("SHA1PRNG").nextBytes(b);

			return "_" + Hex.encodeHexString(b);

		} catch (Exception e) {
			throw new OAuthException(e);
		}
	}
}
