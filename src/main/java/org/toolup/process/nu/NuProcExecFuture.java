package org.toolup.process.nu;

import java.util.concurrent.CompletableFuture;

import org.toolup.process.business.ProcessExecResult;

import com.zaxxer.nuprocess.NuProcess;

public class NuProcExecFuture {

	private CompletableFuture<ProcessExecResult> future;
	private NuProcess process;
	
	public CompletableFuture<ProcessExecResult> getFuture() {
		return future;
	}
	public NuProcExecFuture future(CompletableFuture<ProcessExecResult> future) {
		this.future = future;
		return this;
	}
	public NuProcess getProcess() {
		return process;
	}
	public NuProcExecFuture process(NuProcess process) {
		this.process = process;
		return this;
	}
	
	
	@Override
	public String toString() {
		return "ProcExecFuture [future=" + future + ", process=" + process + "]";
	}
	
	
}
