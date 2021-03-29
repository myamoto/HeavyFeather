package org.toolup.network.ftp.config;

import java.util.ArrayList;
import java.util.List;

public class FTPConfiguration {
	
	private String logDirFilePath;
	private final List<FTPServerParams> ftpServerParamList;
	private int refreshIntervalSeconds;
	
	public FTPConfiguration() {
		ftpServerParamList = new ArrayList<FTPServerParams>();
	}
	
	public void setRefreshIntervalSeconds(int refreshIntervalSeconds) {
		this.refreshIntervalSeconds = refreshIntervalSeconds;
	}

	public int getRefreshIntervalSeconds() {
		return refreshIntervalSeconds;
	}
	
	
	public String getLogDirFilePath() {
		return logDirFilePath;
	}
	
	public void setLogDirFilePath(String logDirFilePath) {
		this.logDirFilePath = logDirFilePath;
	}
	
	public List<FTPServerParams> getFtpServerParamList() {
		return clone(ftpServerParamList);
	}

	public void setFtpServerParamList(List<FTPServerParams> ftpServerParamList) {
		this.ftpServerParamList.clear();
		if(ftpServerParamList != null)this.ftpServerParamList.addAll(ftpServerParamList);
	}
	
	
	private List<FTPServerParams> clone(List<FTPServerParams> lst) {
		if(lst == null)return null;
		List<FTPServerParams> res = new ArrayList<FTPServerParams>();
		for (FTPServerParams ftpServerParams : lst) {
			res.add(ftpServerParams);
		}
		return res;
	}

	public String toString() {
		return "Configuration [logDirFilePath=" + logDirFilePath
				+ ", ftpServerParamList=" + ftpServerParamList
				+ ", refreshIntervalSeconds=" + refreshIntervalSeconds +"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ftpServerParamList == null) ? 0 : ftpServerParamList.hashCode());
		result = prime * result + ((logDirFilePath == null) ? 0 : logDirFilePath.hashCode());
		result = prime * result + refreshIntervalSeconds;
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
		FTPConfiguration other = (FTPConfiguration) obj;
		if (ftpServerParamList == null) {
			if (other.ftpServerParamList != null)
				return false;
		} else if (!ftpServerParamList.equals(other.ftpServerParamList))
			return false;
		if (logDirFilePath == null) {
			if (other.logDirFilePath != null)
				return false;
		} else if (!logDirFilePath.equals(other.logDirFilePath))
			return false;
		if (refreshIntervalSeconds != other.refreshIntervalSeconds)
			return false;
		return true;
	}
	
}

