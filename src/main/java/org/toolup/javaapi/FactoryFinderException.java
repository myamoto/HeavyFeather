package org.toolup.javaapi;
public class FactoryFinderException extends Exception{

	private static final long serialVersionUID = 1229915997665070926L;

	public FactoryFinderException(Throwable cause) {
		super(cause);
	}

	public FactoryFinderException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public FactoryFinderException(String msg) {
		super(msg);
	}
}