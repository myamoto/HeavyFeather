package org.toolup.secu.oauth.jwt.parse;

import java.security.InvalidKeyException;
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
		
		if(System.getProperty(UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM) != null) {
			logger.info("System property {} found : going with UrlKeyCache - url = {}."
					, UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM
					, System.getProperty(UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM));
			keyHolder = new UrlKeyCache();
		}else {
			logger.info("System property {} not found : going with UrlKeyCache.", UrlKeyCache.OAUTH_PUBLIC_KEY_URL_PARAM);
			keyHolder = new JWTForgeKeyCache();
		}
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
			
			Object kidObj = claims.get("kid");
			if(kidObj != null && !String.class.isAssignableFrom(kidObj.getClass())) 
				errors.append("\noptional 'kid' claim should be a string.");

			if(!errors.toString().isEmpty())
				throw new OAuthException("invalid JWT : " + errors.toString());

			Signature signature = Signature.getInstance("SHA256withRSA");

			PublicKey publicKey = keyHolder.getPublicKey((String)kidObj);

			logger.debug("veryfing Bearer signature.");
			logger.debug("signed content : {}", token12);
			logger.debug("signature : {}", token3);
			logger.debug("public key : {}", publicKey);
			try {
				signature.initVerify(publicKey);
			}catch (InvalidKeyException e) {
				logger.error("invalid public key");
				throw e;
				
			}
			signature.update(token12.getBytes(CharEncoding.UTF_8));


			long now = System.currentTimeMillis();
			long notBefore = getTimeMillis(claims.getLong("nbf"));
			long notOnOrAfter = getTimeMillis(claims.getLong("exp"));
			
			if (notBefore > (now + 10000)) {
				throw new OAuthException("token expired #1");
			}
			if (notOnOrAfter < (now - 10000)) {
				throw new OAuthException("token expired #2");
			}

			if(!signature.verify(Base64.decodeBase64(token3))) {
				logger.debug("signature verify failed.");
				throw new OAuthException("invalid signature");
			}
			
			return new JWT()
					.token(token)
					.claims(claims)
					.header(header);
		} catch (Exception e) {
			throw new OAuthException(e);
		}
	}

	private static long getTimeMillis(final long time) { return (time > 99999999999L) ? time : time * 1000; }

	
	public static void main(String[] args) throws OAuthException {
		System.setProperty("org.toolup.secu.oauth.jwt.parse.oauthPublicKeyUrl", "https://auth-qualif.si.cnaf.info/oauth/keys");
		String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiJmZmQ0YzBmOC1hMjJkLTQ4MWUtODM3OC1lNzgwYmM5YTNhN2QiLCJzdWIiOiJtY29ydDc1NSIsIm5iZiI6MTY3MTc5MTE3NDQ2OCwiZG9tYWluIjoicHJpdmF0ZSIsInJvbGVzIjpbXSwic2NvcGUiOiJwdWJsaWMiLCJpc3MiOiJodHRwczovL2F1dGgtcXVhbGlmLnNpLmNuYWYuaW5mbyIsImV4cCI6MTY3MTc5MTQ4NDQ2OCwiaWF0IjoxNjcxNzkxMTg0NDY4LCJqdGkiOiI0MWQ5ODU4Ny0wZjQ2LTQwYmQtYTY3Mi03MjgyMjA4MjIyY2EifQ.m-K-yoOw7hzmD8Wam843OkmGespB07v4dETERvrjOTVNiAyjc2eEOcV3dlxZdt1wmIi7BHRv7PAtB0Pa8Jf8Hs98-qY8jGINH03X-oSQcKldSFAIcQ14cWA31ATVv9dqj0CWcpZfD5vI3I8e_GMHmkrYhU5dRq-TOedpXPzZa4oVkEFyEgYGPQGvvdz1iAGj_2O6pY1Bx3Ye13yQa1sjmW4L7Y4TUf3ZDe-jXVppXsiVumLCN5iGDzR8vxyYhS4dhEClLvZf9fVrdaOA5g6fROsuR7ycBA1O0DddnfBqy4MQW-_iVjD7F6dbR-5nCUnGcUXQ9FaAEO_RJsMiClN5_Q";
		
		JWTParserFactory.newInstance().parse(token);
		
	}
}
