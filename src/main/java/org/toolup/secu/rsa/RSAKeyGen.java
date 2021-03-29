package org.toolup.secu.rsa;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

public class RSAKeyGen {
	
	private RSAKeyGen() {}
	
	public static RSAKeyPair genKeyPair(String user) throws RSAKeyGenException {
        try {
        	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    		keyPairGenerator.initialize(4096, SecureRandom.getInstanceStrong());
    		KeyPair keyPair = keyPairGenerator.generateKeyPair();
    		
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

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
        } catch (NoSuchAlgorithmException e) {
        	throw new RSAKeyGenException(e);
		}
	}
	
}