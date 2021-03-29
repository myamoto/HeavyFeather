package org.toolup.network.ftp.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.toolup.network.ftp.SimpleNameFilter;

public class RemoteDirectory implements SimpleNameFilter{

	private List<String> downloadFileExtensions;
	private String path;
	private boolean recursiveDownload;

	public RemoteDirectory() {
		downloadFileExtensions = new ArrayList<String>();
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public boolean isRecursiveDownload() {
		return recursiveDownload;
	}
	public void setRecursiveDownload(boolean recursiveDownload) {
		this.recursiveDownload = recursiveDownload;
	}
	public List<String> getDownloadFileExtensions() {
		return downloadFileExtensions;
	}
	
	public void setDownloadFileExtensions(List<String> downloadFileExt) {
		this.downloadFileExtensions = downloadFileExt;
	}

	public void addAllDownloadFileExt(String... downloadFileExtList) {
		if(downloadFileExtList != null){
			getDownloadFileExtensions().addAll(Arrays.asList(downloadFileExtList));
		}
	}

	public boolean accept(String name) {
		
		if(getDownloadFileExtensions().isEmpty()){
			return name.endsWith(".zip");
		}else if(getDownloadFileExtensions().contains("*")) {
			return true;
		}else {
			for (String downloadFileExt : getDownloadFileExtensions()) {
				if(name.matches(downloadFileExt)){
					return true;
				}
			}
			return false;
		}
	}	
	public String toString() {
		return "RemoteDirectory [downloadFileExt=" + downloadFileExtensions
				+ ", path=" + path + ", recursiveDownload=" + recursiveDownload
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((downloadFileExtensions == null) ? 0 : downloadFileExtensions.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (recursiveDownload ? 1231 : 1237);
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
		RemoteDirectory other = (RemoteDirectory) obj;
		if (downloadFileExtensions == null) {
			if (other.downloadFileExtensions != null)
				return false;
		} else if (!downloadFileExtensions.equals(other.downloadFileExtensions))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (recursiveDownload != other.recursiveDownload)
			return false;
		return true;
	}

	
}
