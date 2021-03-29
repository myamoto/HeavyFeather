package org.toolup.process;

import java.util.concurrent.CompletableFuture;

import org.toolup.process.business.ProcessExecResult;


public class ProcExecFuture {

	private CompletableFuture<ProcessExecResult> future;
	private Process process;
	
	public CompletableFuture<ProcessExecResult> getFuture() {
		return future;
	}
	public ProcExecFuture future(CompletableFuture<ProcessExecResult> future) {
		this.future = future;
		return this;
	}
	public Process getProcess() {
		return process;
	}
	public ProcExecFuture process(Process process) {
		this.process = process;
		return this;
	}
	
	@Override
	public String toString() {
		return "ProcExecFuture [future=" + future + ", process=" + process + "]";
	}
}
