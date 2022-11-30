package org.toolup.network.common;

public class ServerParams {
	
	private String hostName;
	
	private String userName;
	
	private String password;
	public ServerParams() {}
	public ServerParams(String hostName, String userName, String password) {
		super();
		this.hostName = hostName;
		this.userName = userName;
		this.password = password;
	}

	public String getHostName() {
		return hostName;
	}

	public ServerParams setHostName(String hostName) {
		this.hostName = hostName;
		return this;
	}

	public String getUserName() {
		return userName;
	}

	public ServerParams setUserName(String userName) {
		this.userName = userName;
		return this;
	}

	public String getPassword() {
		return password;
	}

	public ServerParams setPassword(String password) {
		this.password = password;
		return this;
	}

	@Override
	public String toString() {
		return "ServerParams [hostName=" + hostName + ", userName=" + userName + ", password=" + password + "]";
	}
}
