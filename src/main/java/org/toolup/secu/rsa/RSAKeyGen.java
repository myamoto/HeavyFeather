package org.toolup.secu.rsa;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.toolup.secu.oauth.OAuthException;
import org.toolup.secu.oauth.jwt.keys.OAuthKeyPairHolder;

public class RSAKeyGen {

	private RSAKeyGen() {}

	public static RSAKeyPair genKeyPair(String user) throws RSAKeyGenException {
		OAuthKeyPairHolder keyBldr;
		try {
			keyBldr = OAuthKeyPairHolder.newInstance();
		} catch (OAuthException e) {
			throw new RSAKeyGenException(e);
		}
		PrivateKey privateKey = keyBldr.getPrivateKey();
		PublicKey publicKey = keyBldr.getPublicKey();

		Base64.Encoder encoder = Base64.getEncoder();

		String ppk = encoder.encodeToString(privateKey.getEncoded());
		String pub = encoder.encodeToString(publicKey.getEncoded());

		StringBuilder sbPk = new StringBuilder();
		for (int i = 0; i < ppk.length(); i++) {
			if(i % 64 == 0)sbPk.append("\n");
			sbPk.append(ppk.charAt(i));

		}
		return new RSAKeyPair()
				.setPrivateKey(sbPk.toString())
				.setPublicKey(pub)
				.setPrettyPrivateKey("-----BEGIN RSA PRIVATE KEY-----" 
						+ sbPk.toString() 
						+ "\n-----END RSA PRIVATE KEY-----\n")
				.setPrettyPublicKey( "ssh-rsa " + pub + " " + user);

	}

}