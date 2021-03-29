package org.toolup.process.nu;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.process.ProcExecutorException;
import org.toolup.process.TestProcExecutor;

public class TestNuProcExecutor {

	private static Logger logger = LoggerFactory.getLogger(TestNuProcExecutor.class);
	
//	@Test
	public void test() throws URISyntaxException {
		String cmd = new File(TestProcExecutor.class.getClassLoader().getResource("sleepCommand.bat").toURI()).getAbsolutePath();
		
		NuProcExecFuture fut = null;
		try (NuProcExecutor exec = new NuProcExecutor("test", 10 )){
			fut = exec.processExec(new File(cmd).getParentFile().getAbsolutePath(), null, cmd, "3");
			Thread.sleep(5000);
			logger.error("calling destroy().");
			
			/*
			 * https://stackoverflow.com/questions/10630303/java-process-destroy-source-code-for-linux
			 * 
			 * process.destroy() -> UNIXProcess_md.c@Java_java_lang_UNIXProcess_destroyProcess -> kill(pid, SIGTERM)
			 * 
			 * 
			 */
			fut.getProcess().destroy(true);
			fut.getProcess().writeStdin(ByteBuffer.wrap("y".getBytes()));
			logger.error("after destroy");
		} catch (ProcExecutorException e) {
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		} finally {
			logger.error("all done.");
		}
	}
}
