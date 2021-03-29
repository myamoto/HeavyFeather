package org.toolup.network.ftp.config;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class FTPServerParams {
	private String name;
	private int port;
	private String server, user, password;
	

	private boolean sftp;
	private boolean enabled;
	private final List<RemoteDirectory> remoteDirList;
	
	public FTPServerParams() {
		remoteDirList = new ArrayList<RemoteDirectory>();
	}
	
	public String getServer() {
		return server;
	}
	
	public FTPServerParams setServer(String server) {
		this.server = server;
		return this;
	}
	
	public String getUser() {
		return user;
	}
	
	public FTPServerParams setUser(String user) {
		this.user = user;
		return this;
	}
	
	public String getPassword() {
		return password;
	}
	
	public FTPServerParams setPassword(String password) {
		this.password = password;
		return this;
	}
	
	public boolean isSftp() {
		return sftp;
	}
	
	public FTPServerParams setSftp(boolean sftp) {
		this.sftp = sftp;
		return this;
	}
	
	public int getPort() {
		return port;
	}
	
	public FTPServerParams setPort(int port) {
		this.port = port;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public FTPServerParams setName(String name) {
		this.name = name;
		return this;
	}
	
	public List<RemoteDirectory> getRemoteDirList() {
		return clone(remoteDirList);
	}
	
	public void setRemoteDirList(List<RemoteDirectory> remoteDirectoryList) {
		clearRemoteDirectoryList();
		if(remoteDirectoryList != null)
			this.remoteDirList.addAll(remoteDirectoryList);
	}

	public void clearRemoteDirectoryList() {
		remoteDirList.clear();
	}

	public boolean addRemoteDirPathList(String path, boolean isResursiveDownload, List<String> downloadFileExtList){
		RemoteDirectory remoteDir = new RemoteDirectory();
		remoteDir.setPath(path);
		remoteDir.setRecursiveDownload(isResursiveDownload);
		remoteDir.addAllDownloadFileExt(downloadFileExtList.toArray(new String[downloadFileExtList.size()]));
		return remoteDirList.add(remoteDir);
	}
	
	public boolean addRemoteDirPath(String path, boolean isResursiveDownload, String... downloadFileExtList){
		RemoteDirectory remoteDir = new RemoteDirectory();
		remoteDir.setPath(path);
		remoteDir.setRecursiveDownload(isResursiveDownload);
		remoteDir.addAllDownloadFileExt(downloadFileExtList);
		return remoteDirList.add(remoteDir);
	}
	
	private List<RemoteDirectory> clone(List<RemoteDirectory> lst) {
		if(lst == null)return null;
		List<RemoteDirectory> res = new ArrayList<>();
		for (RemoteDirectory ftpServerParams : lst) {
			res.add(ftpServerParams);
		}
		return res;
	}
	
	@JsonIgnore
	public String getFTPUrl(){
		return String.format("%s://%s:%s@%s:%d", (isSftp() ? "sftp" : "ftp"), user, password, server, port);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public FTPServerParams setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}

	public String toString() {
		return "FTPServerParams [port=" + port + ", server=" + server
				+ ", userName=" + user + ", password=" + password
				+ ", sftp=" + sftp + ", remoteDirectoryList="
				+ remoteDirList + "]";
	}
	
	
	@JsonIgnore
	public String getHTMLDesc() {
		return String.format(
				"<html><ul>" +
				"<li>Serveur : %s</li>" +
				"<li>Port : %d</li>" +
				"<li>usr : %s</li>" +
				"<li>pw : %s</li>" +
				"<li>sftp : %b</li>" +
				"<li>repertoires : %s</li>" +
				"</ul></html>", 
				server, port, user, password, sftp, getHTMLDescRemoteDirList());
	}

	private String getHTMLDescRemoteDirList() {
		StringBuilder sb = new StringBuilder();
		sb.append("<ul>");
		for (RemoteDirectory remoteDirectory : remoteDirList) {
			sb.append(String.format("<li>%s</li>", remoteDirectory.getPath()));
		}
		sb.append("</ul>");
		return sb.toString();
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + port;
		result = prime * result + ((remoteDirList == null) ? 0 : remoteDirList.hashCode());
		result = prime * result + ((server == null) ? 0 : server.hashCode());
		result = prime * result + (sftp ? 1231 : 1237);
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		FTPServerParams other = (FTPServerParams) obj;
		if (enabled != other.enabled)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (port != other.port)
			return false;
		if (remoteDirList == null) {
			if (other.remoteDirList != null)
				return false;
		} else if (!remoteDirList.equals(other.remoteDirList))
			return false;
		if (server == null) {
			if (other.server != null)
				return false;
		} else if (!server.equals(other.server))
			return false;
		if (sftp != other.sftp)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}

	public FTPServerParams clone() {
		FTPServerParams result = new FTPServerParams();
		result.setEnabled(isEnabled());
		result.setName(getName());
		result.setPort(getPort());
		result.setServer(getServer());
		result.setSftp(isSftp());
		result.setUser(getUser());
		result.setPassword(getPassword());
		
		return result;
	}
	
	
}
