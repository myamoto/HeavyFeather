package org.toolup.network.common;

public class ServerParams {
	
	private String hostName;
	
	private String userName;
	
	private String password;

	public ServerParams(String hostName, String userName, String password) {
		super();
		this.hostName = hostName;
		this.userName = userName;
		this.password = password;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "ServerParams [hostName=" + hostName + ", userName=" + userName + ", password=" + password + "]";
	}
}
