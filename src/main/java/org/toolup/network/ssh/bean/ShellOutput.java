package org.toolup.network.ssh.bean;

public class ShellOutput {
    private String outputStream;
    private String errorStream;
    private int exitStatus;
    
	public String getOutputStream() {
		return outputStream;
	}
	public void setOutputStream(String outputStream) {
		this.outputStream = outputStream;
	}
	public String getErrorStream() {
		return errorStream;
	}
	public void setErrorStream(String errorStream) {
		this.errorStream = errorStream;
	}
	public int getExitStatus() {
		return exitStatus;
	}
	public void setExitStatus(int exitStatus) {
		this.exitStatus = exitStatus;
	}
	
	@Override
	public String toString() {
		return "ShellOutput [outputStream=" + outputStream + ", errorStream=" + errorStream + ", exitStatus=" + exitStatus + "]";
	}
}