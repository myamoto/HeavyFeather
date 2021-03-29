package org.toolup.devops.ci.git.client;

public class GITSCMException extends Exception{
	
	private static final long serialVersionUID = -770447181850530029L;

	public GITSCMException(String msg) {
		super(msg);
	}
	
	public GITSCMException(Throwable t) {
		super(t);
	}
	
	public GITSCMException(Throwable t, String msg) {
		super(msg, t);
	}
}
