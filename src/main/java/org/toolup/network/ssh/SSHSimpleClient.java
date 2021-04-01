package org.toolup.network.ssh;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.common.JSchLogger;
import org.toolup.network.common.ServerParams;
import org.toolup.network.ssh.bean.ShellCommand;
import org.toolup.network.ssh.bean.ShellOutput;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHSimpleClient {

	private static final Logger logger = LoggerFactory.getLogger(SSHSimpleClient.class);
	
	private ServerParams params;

	public SSHSimpleClient(ServerParams params) {
		this.params = params;
		JSch.setLogger(new JSchLogger());
	}
	
	public ShellOutput execCmd(ShellCommand command) throws JSchException, IOException, InterruptedException {
		return execCmd(command.toString());
	}
	
	public ShellOutput execCmd(List<ShellCommand> commands) throws JSchException, IOException, InterruptedException {
		return execCmd(commands.stream().map(ShellCommand::toString).collect(Collectors.joining(";")));
	}
	
	public ShellOutput execCmd(String command) throws JSchException, IOException, InterruptedException {
		logger.debug("execCmd : {}", command);
		Session session = null;
		ChannelExec channel = null;

        try (ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
    		session = new JSch().getSession(params.getUserName(), params.getHostName());
    		session.setPassword(params.getPassword());
    		session.setConfig("StrictHostKeyChecking", "no");
    		session.connect();

    		channel = (ChannelExec) session.openChannel("exec");
    		channel.setCommand(command);
    		channel.setErrStream(errorStream);
            channel.connect();
            
    		ShellOutput sshOut = new ShellOutput();
            sshOut.setOutputStream(readChannelInputStream(channel));
            sshOut.setErrorStream(new String(errorStream.toByteArray(), StandardCharsets.UTF_8));
            sshOut.setExitStatus(channel.getExitStatus());
    		return sshOut;
        } finally {
    		if (channel != null) channel.disconnect();
    		if (session != null) session.disconnect();
		}
	}
	
	private String readChannelInputStream(ChannelExec channel) throws IOException, InterruptedException {
		try (InputStream is = channel.getInputStream();
			StringWriter sw = new StringWriter();) {
			while (true) {
				IOUtils.copy(is, sw, StandardCharsets.UTF_8);
				if (channel.isClosed()) {
					if (is.available() > 0) continue;
					return sw.toString();
				}
				Thread.sleep(1000);
			}
		}
	}
	

}
