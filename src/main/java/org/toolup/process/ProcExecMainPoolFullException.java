package org.toolup.process;
public class ProcExecMainPoolFullException extends ProcExecutorException {
	
	private static final long serialVersionUID = 4299583609720766240L;
	
	public ProcExecMainPoolFullException(String msg) { super(msg); }

	public ProcExecMainPoolFullException(Throwable t) { super(t); }
	
	public ProcExecMainPoolFullException(String msg, Throwable t) { super(msg, t); }
}


