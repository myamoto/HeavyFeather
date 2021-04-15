package org.toolup.network.ssh;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.NetTestUtil;
import org.toolup.network.ssh.bean.ShellCommand;
import org.toolup.network.ssh.bean.ShellOutput;

import com.jcraft.jsch.JSchException;

public class SSHSimpleClientTest {

	private final static Logger logger = LoggerFactory.getLogger(SSHSimpleClientTest.class);

	@Test
	public void testExecSingleCmd() throws JSchException, IOException, InterruptedException {
		
		ShellCommand cmd = new ShellCommand().command("ls").arg("-l");
				
		ShellOutput out = new SSHSimpleClient(NetTestUtil.env("srv1")).execCmd(cmd);
		
		logger.debug("{}", out);
		
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getExitStatus(), 0);
	}
	
	@Test
	public void testExecMultipleCmd() throws JSchException, IOException, InterruptedException {

		List<ShellCommand> cmdList = new ArrayList<ShellCommand>();
		cmdList.add(new ShellCommand().command("echo").arg("\"stdout\""));
		cmdList.add(new ShellCommand().command("echo").arg("\"stderr\" 1>&2"));
		cmdList.add(new ShellCommand().command("exit").arg("123"));
		
		ShellOutput out = new SSHSimpleClient(NetTestUtil.env("srv1")).execCmd(cmdList);
		
		logger.debug("{}", out);
		
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getOutputStream(), "stdout\n");
		Assert.assertEquals(out.getErrorStream(), "stderr\n");
		Assert.assertEquals(out.getExitStatus(), 123);
	}
	
	@Test
	public void testExecMultipleCmd2() throws JSchException, IOException, InterruptedException {
		
		StringBuilder cmd = new StringBuilder().append("echo \"stdout\";")
							.append("echo \"stderr\" 1>&2;")
							.append("exit 123;");
		
		ShellOutput out = new SSHSimpleClient(NetTestUtil.env("srv1")).execCmd(cmd.toString());
		
		logger.debug("{}", out);
		
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getOutputStream(), "stdout\n");
		Assert.assertEquals(out.getErrorStream(), "stderr\n");
		Assert.assertEquals(out.getExitStatus(), 123);
	}

}
