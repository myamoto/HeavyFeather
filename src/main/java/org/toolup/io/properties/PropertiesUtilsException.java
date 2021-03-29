package org.toolup.io.properties;

public class PropertiesUtilsException extends Exception {
	
	private static final long serialVersionUID = 5283272149320654070L;
	
	public PropertiesUtilsException(String msg) {
		super(msg);
	}
	
	public PropertiesUtilsException(Throwable t) {
		super(t);
	}
	
	public PropertiesUtilsException(String msg, Throwable t) {
		super(msg, t);
	}
	
}
