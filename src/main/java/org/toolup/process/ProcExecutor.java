package org.toolup.process;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.process.business.ProcessExecResult;


public class ProcExecutor implements Closeable{
	
	private static Logger logger = LoggerFactory.getLogger(ProcExecutor.class);
	
	private final ExecutorService procExecutorService;
	private final ExecutorService stdoutGobblerService;
	private final ExecutorService stderrGobblerService;

	private final String name;
	private final AtomicInteger nbExec = new AtomicInteger(0);
	private final AtomicBoolean lock = new AtomicBoolean(false);
	private String lockReason;

	public ProcExecutor(String name, ExecutorService procExecutorService, ThreadPoolExecutor stderrGobblerService, ThreadPoolExecutor stdoutGobblerService) {
		this.name = name;
		this.procExecutorService = procExecutorService;
		this.stderrGobblerService = stderrGobblerService;
		this.stdoutGobblerService = stdoutGobblerService;
	}


	public ProcExecutor(String name, int nbMaxConcurrentExecs) {
		this.name = name;
		this.stderrGobblerService = Executors.newFixedThreadPool(nbMaxConcurrentExecs);
		this.stdoutGobblerService = Executors.newFixedThreadPool(nbMaxConcurrentExecs);
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

	public ProcExecFuture processExec(String runInPath, StringBuffer customBuffer, String... commands) throws ProcExecutorException { 
		return processExec(runInPath, customBuffer, null, null, commands); 
	} 
	
	public ProcExecFuture processExec(String runInPath, StringBuffer customBuffer
			, Callback<String> startCmdCallback, Callback<Process> processCreatedCallback
			, String... commands) throws ProcExecutorException {
		return processExec(runInPath, customBuffer, startCmdCallback, processCreatedCallback, null, commands);
	}

	public ProcExecFuture processExec(String runInPath, StringBuffer customBuffer
			, Callback<String> startCmdCallback, Callback<Process> processCreatedCallback
			, Map<String, String> envts
			, String... commands) throws ProcExecutorException {
		Process p;
		if (this.lock.get()) {
			throw new ProcExecutorException(String.format("ProcessExecutor <%s> : locked. reason : %s ", new Object[] { this.name, this.lockReason }));
		}

		StringBuffer outBuffer;
		if (customBuffer == null) {
			outBuffer = new StringBuffer();
		} else {
			outBuffer = customBuffer;
		}  
		
		nbExec.incrementAndGet();

		ProcessBuilder builder = new ProcessBuilder()
				.command(commands)
				.directory(new File(runInPath));
		if(envts != null) builder.environment().putAll(envts);
		
		String cmd = runInPath + " " + String.join(" ", commands);
		outBuffer.append(cmd).append("\n");

		try {
			p = builder.start();
		} catch (IOException ex) {
			this.nbExec.decrementAndGet();
			throw new ProcExecutorException(ex);
		}
		if(processCreatedCallback != null) processCreatedCallback.call(p);

		CompletableFuture<Void> soutFut;
		try {
			soutFut = readOutStream(p.getInputStream(), outBuffer, this.stdoutGobblerService);
		} catch (RejectedExecutionException ex) {
			throw new ProcExecStdOutPoolFullException(ex);
		} 

		CompletableFuture<Void> serrFut;
		try {
			serrFut = readOutStream(p.getErrorStream(), outBuffer, this.stderrGobblerService);
		} catch (RejectedExecutionException ex) {
			throw new ProcExecStdErrPoolFullException(ex);
		} 

		CompletableFuture<Integer> execFut;
		try {
			execFut = CompletableFuture.supplyAsync(() -> {
				if (startCmdCallback != null) startCmdCallback.call(cmd);
				try {
					return Integer.valueOf(p.waitFor());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Integer.valueOf(-1);
				} 
			}, procExecutorService);
		} catch (RejectedExecutionException ex) {
			throw new ProcExecMainPoolFullException(ex);
		} 
		
		
		CompletableFuture<Void> allFuts = CompletableFuture.allOf(soutFut, serrFut, execFut);
		
		return new ProcExecFuture()
				.process(p)
				.future(allFuts.thenApply(r -> {
					logger.info("joining...");
					return new ProcessExecResult()
							.returnCode(execFut.join().intValue());
					}).whenComplete((r, t) -> {
						
						if(t != null) {
							t.printStackTrace();
							logger.info("got exception {}", t);
						}
						this.nbExec.decrementAndGet();
					})
				);
	}

	public CompletableFuture<Void> readOutStream(InputStream is, StringBuffer outbuf, ExecutorService executorService) {
		return CompletableFuture.supplyAsync(() -> {
			try (InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);
//					BufferedReader br = new BufferedReader(in);
					){
				
				final int bufferSize = 1024;
				final char[] buffer = new char[bufferSize];
				int charsRead;
				while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
					outbuf.append(buffer, 0, charsRead);
				}
//				String inputLine;
//				while ((inputLine = isr.read()) != null) {
//					outbuf.append(inputLine);
//				}
				return null;
			} catch (IOException e) {
				return null;
			} catch (Exception e) {
				throw new ProcessExecThreadException("problem reading program output.", e);
			}
		}, executorService);
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
		close(stdoutGobblerService);
		close(stderrGobblerService);
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
