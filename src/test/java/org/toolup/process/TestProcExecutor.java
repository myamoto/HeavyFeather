package org.toolup.process;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestProcExecutor {

	private static Logger logger = LoggerFactory.getLogger(TestProcExecutor.class);
	
	@Test
	public void testSimpleCmd() throws URISyntaxException {
		String cmd = new File(getClass().getResource("sleepCommand.bat").toURI()).getAbsolutePath();
		
		int returncode = -1;
		try (ProcExecutor exec = new ProcExecutor("test", 10 )){
			StringBuffer out = new StringBuffer();
			final ProcExecFuture fut = exec.processExec(".", out, cmd, "5");
			
			new Thread(() -> {
				while(fut.getProcess().isAlive()) {
					logger.info("\n\n\n"+ out.toString() + "\n\n\n");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
				}
			}).start();
			Thread.sleep(1000);
			
			try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(fut.getProcess().getOutputStream()));){
				pw.write("y");
			}
			
			Thread.sleep(6000);
			
			/*
			 * https://stackoverflow.com/questions/10630303/java-process-destroy-source-code-for-linux
			 * 
			 * process.destroy() -> UNIXProcess_md.c@Java_java_lang_UNIXProcess_destroyProcess -> kill(pid, SIGTERM)
			 * 
			 * 
			 */
			if(fut.getProcess().isAlive()) {
				logger.error("calling destroy().");
				fut.getProcess().destroy();
				logger.error("*********** destroyed process");
			}
			logger.info("final output :\n" + out.toString());
			returncode = fut.getProcess().exitValue();
			Assert.assertEquals(0, returncode);
		} catch (ProcExecutorException e) {
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
	}
}
