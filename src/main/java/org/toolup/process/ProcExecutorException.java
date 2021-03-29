package org.toolup.process;

public class ProcExecutorException extends Exception {

	private static final long serialVersionUID = -7696360402364868796L;

	public ProcExecutorException(String msg) { super(msg); }

	public ProcExecutorException(Throwable t) { super(t); }

	public ProcExecutorException(String msg, Throwable t) { super(msg, t); }
}


