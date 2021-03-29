package org.toolup.io.json;

public class JSonException extends Exception {


	private static final long serialVersionUID = -2617587163072202095L;

	public JSonException(String message, Throwable cause) {
        super(message, cause);
    }
	
	public JSonException(String msg) {
		super(msg);
	}
	
	public JSonException(Throwable cause) {
		super(cause);
	}

}

