package org.toolup.process;

public class ProcExecStdOutPoolFullException extends ProcExecutorException {
 
	private static final long serialVersionUID = 1112899391566428729L;
  
  public ProcExecStdOutPoolFullException(String msg) { super(msg); }
  
  public ProcExecStdOutPoolFullException(Throwable t) { super(t); }
  
  public ProcExecStdOutPoolFullException(String msg, Throwable t) { super(msg, t); }
}


