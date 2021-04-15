package org.toolup.secu.oauth.jwt.parse;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Hashtable;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.javaapi.FactoryFinderException;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JSONObject;
import org.toolup.secu.oauth.jwt.JWT;
import org.toolup.secu.oauth.jwt.parse.keys.IKeysHolder;
import org.toolup.secu.oauth.jwt.parse.keys.JWTForgeKeyCache;
import org.toolup.secu.oauth.jwt.parse.keys.UrlKeyCache;

public class JWTParserDefault extends JWTParserFactory{

	private static Logger logger = LoggerFactory.getLogger(JWTParserDefault.class);

	private IKeysHolder keyHolder;
	
	public JWTParserDefault() throws FactoryFinderException {
		if(System.getProperty(UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM) != null)
			keyHolder = new UrlKeyCache();
		else
			keyHolder = new JWTForgeKeyCache();
	}

	public Hashtable<String, PublicKey> getPublicKeys() throws OAuthException {
		return keyHolder.getPublicKeys();
	}

	public PublicKey getDefaultPublicKey() throws OAuthException {
		return keyHolder.getDefaultPublicKey();
	}

	public JWT parse(final String token) throws OAuthException {
		try {
			if( StringUtils.countMatches(token, '.') > 2)
				throw new OAuthException("invalid JWT");
			int i = token.indexOf('.');
			int j = token.lastIndexOf('.');

			String token1 = token.substring(0, i);
			String token2 = token.substring(i + 1, j);
			String token12 = token.substring(0, j);
			String token3 = token.substring(j + 1);

			JSONObject header = JSONObject.create(new String(Base64.decodeBase64(token1), CharEncoding.UTF_8));
			JSONObject claims = JSONObject.create(new String(Base64.decodeBase64(token2), CharEncoding.UTF_8));

			if(logger.isDebugEnabled()) {
				logger.debug("decoding header as  : {}", new String(Base64.decodeBase64(token1), CharEncoding.UTF_8));
				logger.debug("parsed header as  : {}", header.toString());


				logger.debug("decoding claims as  : {}", new String(Base64.decodeBase64(token2), CharEncoding.UTF_8));
				logger.debug("parsed claims as  : {}", claims.toString());
			}
			StringBuilder errors = new StringBuilder();
			long now = System.currentTimeMillis();
			Object nbfObj = claims.get("nbf");
			if(nbfObj == null || !Long.class.isAssignableFrom(nbfObj.getClass()))
				errors.append("\nmandatory claim 'nbf' should be a long. it is of type " + nbfObj.getClass());

			Object expObj = claims.get("exp");
			if(expObj == null || !Long.class.isAssignableFrom(expObj.getClass()))
				errors.append("\nmandatory claim  'exp' should be a long. it is of type " + expObj.getClass());


			Object kidObj = claims.get("kid");
			if(kidObj != null && !String.class.isAssignableFrom(kidObj.getClass())) 
				errors.append("\noptional 'kid' claim should be a string.");

			if(!errors.toString().isEmpty())
				throw new OAuthException("invalid JWT : " + errors.toString());

			long notBefore = getTimeMillis((long) nbfObj);
			long notOnOrAfter = getTimeMillis((long) expObj);

			Signature signature = Signature.getInstance("SHA256withRSA");

			PublicKey publicKey = keyHolder.getPublicKey((String)kidObj);

			logger.debug("veryfing Bearer signature.");
			logger.debug("signed content : {}", token12);
			logger.debug("signature : {}", token3);
			logger.debug("public key : {}", publicKey);

			signature.initVerify(publicKey);
			signature.update(token12.getBytes(CharEncoding.UTF_8));

			if ((notBefore > (now + 10000)) || (notOnOrAfter < (now - 10000))
					|| (!signature.verify(Base64.decodeBase64(token3)))) {
				throw new OAuthException("invalid signature");
			}
			return new JWT()
					.token(token)
					.claims(claims)
					.header(header);
		} catch (OAuthException e) {
			throw e;
		} catch (Exception e) {
			throw new OAuthException(e);
		}
	}

	private static long getTimeMillis(final long time) { return (time > 99999999999L) ? time : time * 1000; }

}
