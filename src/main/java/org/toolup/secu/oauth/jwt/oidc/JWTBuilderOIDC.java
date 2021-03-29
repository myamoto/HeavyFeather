package org.toolup.secu.oauth.jwt.oidc;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.app.ParamLoader;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JSONObject;
import org.toolup.secu.oauth.jwt.JWT;
import org.toolup.secu.oauth.jwt.JWTBuilderFactory;

public class JWTBuilderOIDC extends JWTBuilderFactory{

	private static Logger logger = LoggerFactory.getLogger(JWTBuilderOIDC.class);

	public final static String OAUTH_PUBLIC_KEY_URL_PARAM = JWTBuilderOIDC.class.getPackage().getName() + ".oauthPublicKeyUrl";

	private static final int MAX_PERIODICITY = 6000;
	private static final int MIN_PERIODICITY = 60;
	private int expirePeriodicitySeconds = 600;
	private String oauthPublicKeyUrl;

	private Date reloadDate;
	private PublicKey defaultPublicKey;
	private Hashtable<String, PublicKey> publicKeys;

	public JWTBuilderOIDC oauthPublicKeyUrl(String oauthPublicKeyUrl) {
		this.oauthPublicKeyUrl = oauthPublicKeyUrl;
		return this;
	}

	public JWTBuilderOIDC expirePeriodicitySeconds(int expirePeriodicitySeconds) throws OAuthException {
		if(expirePeriodicitySeconds > MAX_PERIODICITY || expirePeriodicitySeconds < MIN_PERIODICITY)
			throw new OAuthException(String.format("expirePeriodicity must be comprised between %d and %d seconds.", MIN_PERIODICITY, MAX_PERIODICITY)); 
		this.expirePeriodicitySeconds = expirePeriodicitySeconds;
		return this;
	}

	private int getExpirePeriodicitySeconds() {
		return expirePeriodicitySeconds;
	}

	public Hashtable<String, PublicKey> getPublicKeys() throws OAuthException {
		refreshPublicKeys();
		return publicKeys;
	}

	public PublicKey getDefaultPublicKey() throws OAuthException {
		refreshPublicKeys();
		return defaultPublicKey;
	}

	private PublicKey getPublicKey(String kid) throws OAuthException {
		Hashtable<String, PublicKey> currentKeys = getPublicKeys();
		if(kid == null)
			return defaultPublicKey;
		PublicKey result = currentKeys.get(kid);
		if(result == null)
			throw new OAuthException(String.format("public key with id %s not found in keys from url %s (last loaded on %s)"
					, kid
					, getOauthPublicKeyUrl()
					, reloadDate));
		return result;
	}

	private boolean isExpired() {
		Date expireDate;
		if(defaultPublicKey == null)
			return true;
		if(reloadDate == null) {
			expireDate = null;
		}else {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(reloadDate.getTime());
			c.add(Calendar.SECOND, getExpirePeriodicitySeconds());
			expireDate =  c.getTime();
		}

		return expireDate != null && new Date().after(expireDate);
	}
	private void refreshPublicKeys() throws OAuthException {
		if(!isExpired()) return;

		defaultPublicKey = null;
		publicKeys = null;

		String url = getOauthPublicKeyUrl();
		logger.info("{} - renewing OAuth public key from url {}", hashCode(), url);
		reloadDate = new Date();
		publicKeys = new Hashtable<String, PublicKey>();
		try {
			URLConnection connection = new URL(url).openConnection();
			JSONObject obj = JSONObject.create(IOUtils.toString(connection.getInputStream(), Charset.forName("utf-8")));
			Object keysObj = obj.get("keys");
			if(keysObj == null || !List.class.isAssignableFrom(keysObj.getClass()))
				throw new OAuthException("invalid keys. Should be a json array.");
			List<?> keys = (List<?>)obj.get("keys");
			if(keys.isEmpty())
				throw new OAuthException("invalid keys : keys should not be empty.");

			for (int i = 0; i < keys.size(); ++i ) {
				Object keyObj = keys.get(i);
				if(keyObj == null || !JSONObject.class.isAssignableFrom(keyObj.getClass()))
					throw new OAuthException(String.format("invalid keys : keys[%d] should be a json object.", i));

				JSONObject key = (JSONObject) keyObj;

				BigInteger n;
				BigInteger e;
				try {
					n = new BigInteger(Base64.decodeBase64((String) key.get("n")));
					e = new BigInteger(Base64.decodeBase64((String) key.get("e")));
				}catch(NumberFormatException ex) {
					throw new OAuthException(String.format("invalid keys : keys[%d] has invalid 'n' or 'e' attribute. Should be Base64 encoded BigInteger.", i ));
				}

				String kid = (String)key.get("kid");

				PublicKey publicKey = KeyFactory.getInstance("RSA")
						.generatePublic(new RSAPublicKeySpec(n, e));
				if(kid != null) publicKeys.put(kid, publicKey);
				if(i == 0) defaultPublicKey = publicKey;
			}
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
			throw new OAuthException(e);
		}
	}

	private String getOauthPublicKeyUrl() throws OAuthException {
		String result = this.oauthPublicKeyUrl;
		if(result == null) {
			result = ParamLoader.load(OAUTH_PUBLIC_KEY_URL_PARAM);
		}
		if(result == null)
			throw new OAuthException(String.format("oauthPublicKeyUrl must be set (env var / system property / program argument :  %s).", OAUTH_PUBLIC_KEY_URL_PARAM));
		return result;
	}

	public JWT build(final String token) throws OAuthException {
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

			PublicKey publicKey = getPublicKey((String)kidObj);

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
