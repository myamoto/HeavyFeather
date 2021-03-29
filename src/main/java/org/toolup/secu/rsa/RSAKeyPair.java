package org.toolup.secu.rsa;

public class RSAKeyPair {

	private String privateKey;
	private String prettyPrivateKey;
	
	private String publicKey;
	private String prettyPublicKey;
	
	public String getPrivateKey() {
		return privateKey;
	}
	public RSAKeyPair setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
		return this;
	}
	public String getPublicKey() {
		return publicKey;
	}
	public RSAKeyPair setPublicKey(String publicKey) {
		this.publicKey = publicKey;
		return this;
	}
	
	public String getPrettyPrivateKey() {
		return prettyPrivateKey;
	}
	public RSAKeyPair setPrettyPrivateKey(String prettyPrivateKey) {
		this.prettyPrivateKey = prettyPrivateKey;
		return this;
	}
	public String getPrettyPublicKey() {
		return prettyPublicKey;
	}
	public RSAKeyPair setPrettyPublicKey(String prettyPublicKey) {
		this.prettyPublicKey = prettyPublicKey;
		return this;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((privateKey == null) ? 0 : privateKey.hashCode());
		result = prime * result + ((publicKey == null) ? 0 : publicKey.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RSAKeyPair other = (RSAKeyPair) obj;
		if (privateKey == null) {
			if (other.privateKey != null)
				return false;
		} else if (!privateKey.equals(other.privateKey))
			return false;
		if (publicKey == null) {
			if (other.publicKey != null)
				return false;
		} else if (!publicKey.equals(other.publicKey))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RSAKeyPair [publicKey=" + publicKey + "]";
	}
	
	
}
