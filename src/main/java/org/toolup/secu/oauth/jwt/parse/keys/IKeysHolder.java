package org.toolup.secu.oauth.jwt.parse.keys;

import java.security.PublicKey;
import java.util.Hashtable;

import org.toolup.secu.oauth.OAuthException;

public interface IKeysHolder {
	public Hashtable<String, PublicKey> getPublicKeys() throws OAuthException;
	public PublicKey getDefaultPublicKey() throws OAuthException;
	public PublicKey getPublicKey(String kid) throws OAuthException;
}
