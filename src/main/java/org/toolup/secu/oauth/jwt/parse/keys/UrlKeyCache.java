package org.toolup.secu.oauth.jwt.parse.keys;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.app.ParamLoader;
import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.JSONObject;
import org.toolup.secu.oauth.jwt.parse.JWTParserDefault;

public class UrlKeyCache implements IKeysHolder{

	private static Logger logger = LoggerFactory.getLogger(UrlKeyCache.class);
	
	public final static String OAUTH_PUBLIC_KEY_URL_PARAM = JWTParserDefault.class.getPackage().getName() + ".oauthPublicKeyUrl";
	
	private static final int MAX_PERIODICITY = 6000;
	private static final int MIN_PERIODICITY = 60;
	private int expirePeriodicitySeconds = 600;
	
	private Date reloadDate;
	private String oauthPublicKeyUrl;
	
	protected PublicKey defaultPublicKey;
	protected Hashtable<String, PublicKey> publicKeys;
	
	public Hashtable<String, PublicKey> getPublicKeys() throws OAuthException {
		refreshPublicKeys();
		return publicKeys;
	}

	public PublicKey getDefaultPublicKey() throws OAuthException {
		refreshPublicKeys();
		return defaultPublicKey;
	}
	
	public UrlKeyCache oauthPublicKeyUrl(String oauthPublicKeyUrl) {
		this.oauthPublicKeyUrl = oauthPublicKeyUrl;
		return this;
	}
	
	public PublicKey getPublicKey(String kid) throws OAuthException {
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
	
	private String getOauthPublicKeyUrl() throws OAuthException {
		String result = this.oauthPublicKeyUrl;
		if(result == null) {
			result = ParamLoader.load(OAUTH_PUBLIC_KEY_URL_PARAM);
		}
		if(result == null)
			throw new OAuthException(String.format("oauthPublicKeyUrl must be set (env var / system property / program argument :  %s).", OAUTH_PUBLIC_KEY_URL_PARAM));
		return result;
	}
	
	public UrlKeyCache expirePeriodicitySeconds(int expirePeriodicitySeconds) throws OAuthException {
		if(expirePeriodicitySeconds > MAX_PERIODICITY || expirePeriodicitySeconds < MIN_PERIODICITY)
			throw new OAuthException(String.format("expirePeriodicity must be comprised between %d and %d seconds.", MIN_PERIODICITY, MAX_PERIODICITY)); 
		this.expirePeriodicitySeconds = expirePeriodicitySeconds;
		return this;
	}

	private int getExpirePeriodicitySeconds() {
		return expirePeriodicitySeconds;
	}
	
	protected void refreshPublicKeys() throws OAuthException {
		if(!isExpired()) return;

		defaultPublicKey = null;
		publicKeys = null;

		String url = getOauthPublicKeyUrl();
		logger.info("UrlKeyCache #{} - renewing OAuth public key from url {}", hashCode(), url);
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
}
