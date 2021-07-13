package org.toolup.app;

public class ConfigurationException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7852953688948570969L;

	public ConfigurationException(String msg) {
		super(msg);
	}

	public ConfigurationException(Throwable t) {
		super(t);
	}
	
	public ConfigurationException(String msg, Throwable t) {
		super(msg, t);
	}
}
