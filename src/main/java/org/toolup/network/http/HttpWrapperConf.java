package org.toolup.network.http;

import java.util.Properties;

import org.toolup.io.properties.PropertiesUtilsException;

import static org.toolup.io.properties.PropertiesUtils.getMandatoryString;

public class HttpWrapperConf {
	
	public static final String PROP_PROXY_HOST = "ProxyHost";
	public static final String PROP_PROXY_PORT = "ProxyPort";
	public static final String PROP_PROXY_USER = "ProxyUser";
	public static final String PROP_PROXY_PASSWORD = "ProxyPassword";

	private String proxyHost;
	private String proxyPort;
	private String proxyUser;
	private String proxyPassword;
	public String getProxyHost() {
		return proxyHost;
	}
	public HttpWrapperConf proxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
		return this;
	}
	public String getProxyPort() {
		return proxyPort;
	}
	public HttpWrapperConf proxyPort(String proxyPort) {
		this.proxyPort = proxyPort;
		return this;
	}
	public String getProxyUser() {
		return proxyUser;
	}
	public HttpWrapperConf proxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
		return this;
	}
	public String getProxyPassword() {
		return proxyPassword;
	}
	public HttpWrapperConf proxyPassword(String proxyPassword) {
		this.proxyPassword = proxyPassword;
		return this;
	}
	@Override
	public String toString() {
		return "HttpWrapperConf [proxyHost=" + proxyHost + ", proxyPort=" + proxyPort + ", proxyUser=" + proxyUser
				+ ", proxyPassword=" + proxyPassword + "]";
	}
	public static HttpWrapperConf from(Properties props) throws PropertiesUtilsException {
		return new HttpWrapperConf()
		.proxyHost(getMandatoryString(props, PROP_PROXY_HOST))
		.proxyPort(getMandatoryString(props, PROP_PROXY_PORT))
		.proxyUser(getMandatoryString(props, PROP_PROXY_USER))
		.proxyPassword(getMandatoryString(props, PROP_PROXY_PASSWORD));
	}
	
	
}
