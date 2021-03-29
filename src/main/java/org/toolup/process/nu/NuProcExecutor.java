package org.toolup.process.nu;

import java.io.Closeable;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.process.ProcExecMainPoolFullException;
import org.toolup.process.ProcExecutorException;
import org.toolup.process.business.ProcessExecResult;

import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import com.zaxxer.nuprocess.NuProcessHandler;


public class NuProcExecutor implements Closeable{
	private static Logger logger = LoggerFactory.getLogger(NuProcExecutor.class);
	
	private final ExecutorService procExecutorService;

	private final String name;
	private final AtomicInteger nbExec = new AtomicInteger(0);
	private final AtomicBoolean lock = new AtomicBoolean(false);
	private String lockReason;

	public NuProcExecutor(String name, ExecutorService procExecutorService) {
		this.name = name;
		this.procExecutorService = procExecutorService;
	}


	public NuProcExecutor(String name, int nbMaxConcurrentExecs) {
		this.name = name;
		this.procExecutorService = Executors.newFixedThreadPool(nbMaxConcurrentExecs);
	}
	public  void lock(String lockReason) {
		if(!lock.getAndSet(true)) {
			synchronized (this) {
				this.lockReason = lockReason;
			}
		}
	}

	public  void unlock() {
		lock.set(false);
		synchronized (this) {
			this.lockReason = null;
		}
	}

	public boolean isRunning() {
		return nbExec.get() > 0;
	}

	public NuProcExecFuture processExec(String runInPath, StringBuffer customBuffer, String... commands) throws ProcExecutorException { 
		return processExec(runInPath, customBuffer, null, null, commands); 
	} 
	
	public NuProcExecFuture processExec(String runInPath, StringBuffer customBuffer
			, NuCallback<String> startCmdCallback, NuCallback<NuProcess> processCreatedCallback
			, String... commands) throws ProcExecutorException {
		return processExec(runInPath, customBuffer, startCmdCallback, processCreatedCallback, null, commands);
	}

	public NuProcExecFuture processExec(String runInPath, StringBuffer customBuffer
			, NuCallback<String> startCmdCallback, NuCallback<NuProcess> processCreatedCallback
			, Map<String, String> envts
			, String... commands) throws ProcExecutorException {
		if (this.lock.get()) {
			throw new ProcExecutorException(String.format("ProcessExecutor <%s> : locked. reason : %s ", this.name, this.lockReason));
		}
		nbExec.incrementAndGet();
		NuProcessBuilder builder = new NuProcessBuilder(commands);
		if(envts != null) builder.environment().putAll(envts);
		builder.setCwd(Paths.get(runInPath));
		
		final ProcessExecResult result = new ProcessExecResult();
		StringBuffer stdBuffer = customBuffer == null ? new StringBuffer() : customBuffer;
		NuProcessHandler handler = new NuAbstractProcessHandler() {
			@Override
			public void onStdout(ByteBuffer buffer, boolean closed) {
				logger.info("processBldr onStdout {}", closed);
				if (!closed) {
			         byte[] bytes = new byte[buffer.remaining()];
			         // You must update buffer.position() before returning (either implicitly,
			         // like this, or explicitly) to indicate how many bytes your handler has consumed.
			         buffer.get(bytes);
			         stdBuffer
			         	.append(new String(bytes))
			         	.append(System.lineSeparator());
			      }
			}
			
			@Override
			public void onStderr(ByteBuffer buffer, boolean closed) {
				logger.info("processBldr onStderr {}", closed);
				if (!closed) {
			         byte[] bytes = new byte[buffer.remaining()];
			         // You must update buffer.position() before returning (either implicitly,
			         // like this, or explicitly) to indicate how many bytes your handler has consumed.
			         buffer.get(bytes);

			         stdBuffer
			         	.append(new String(bytes))
			         	.append(System.lineSeparator());
		         }
			}
			@Override
			public void onExit(int arg0) {
				logger.info("processBldr exited");
				NuProcExecutor.this.nbExec.decrementAndGet();
			}
			
			@Override
			public boolean onStdinReady(ByteBuffer arg0) {
				logger.info("processBldr onStdinReady");
				return false;
			}
			
			
			@Override
			public void onStart(NuProcess arg0) {
				logger.info("processBldr onStart");
			}
			
			@Override
			public void onPreStart(NuProcess arg0) {
				logger.info("processBldr onPreStart");
			}
		};
		builder.setProcessListener(handler);
		
		String cmd = runInPath + " " + String.join(" ", commands);
		
		final NuProcess p;
		try {
			p = builder.start();
		} catch (Exception ex) {
			this.nbExec.decrementAndGet();
			throw new ProcExecutorException(ex);
		}
		p.setProcessHandler(new NuProcessHandler() {
			
			@Override
			public void onStdout(ByteBuffer arg0, boolean closed) {
				logger.info("onStdout {}", closed);
				
			}
			
			@Override
			public boolean onStdinReady(ByteBuffer arg0) {
				logger.info("onStdinReady");
				return false;
			}
			
			@Override
			public void onStderr(ByteBuffer arg0, boolean closed) {
				logger.info("onStderr {}", closed);
			}
			
			@Override
			public void onStart(NuProcess arg0) {
				logger.info("onStart");
			}
			
			@Override
			public void onPreStart(NuProcess arg0) {
				logger.info("onPreStart");
			}
			
			@Override
			public void onExit(int arg0) {
				logger.info("onExit");
			}
		});
		if(processCreatedCallback != null) processCreatedCallback.call(p);
		
		CompletableFuture<ProcessExecResult> execFut;
		try {
			execFut = CompletableFuture.supplyAsync(() -> {
				if (startCmdCallback != null) startCmdCallback.call(cmd); 
				try {
					return result.returnCode(p.waitFor(0, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return result.returnCode(-1);
				}finally {
					logger.info("process execution finished");
				}
			}, procExecutorService);
		} catch (RejectedExecutionException ex) {
			throw new ProcExecMainPoolFullException(ex);
		} 

		return new NuProcExecFuture()
				.process(p)
				.future(execFut);
	}

	protected static class ProcessExecThreadException extends RuntimeException{

		private static final long serialVersionUID = 8123319375265700079L;

		public ProcessExecThreadException(Throwable t) {
			super(t);
		}
		public ProcessExecThreadException(String msg, Throwable t) {
			super(msg, t);
		}
	}

	public void close() {
		close(procExecutorService);
	}

	private void close(ExecutorService es) {
		if(es == null) return;
		if(!es.isTerminated())
			try {
				es.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.error(e.getMessage(), e);
			}
		if(es.isTerminated() && !es.isShutdown())es.shutdown();
	}


}
