package org.toolup.process;

public class ProcExecStdErrPoolFullException extends ProcExecutorException {
	
	private static final long serialVersionUID = 4773754563609807469L;

	public ProcExecStdErrPoolFullException(String msg) { super(msg); }

	public ProcExecStdErrPoolFullException(Throwable t) { super(t); }
	
	public ProcExecStdErrPoolFullException(String msg, Throwable t) { super(msg, t); }
}


