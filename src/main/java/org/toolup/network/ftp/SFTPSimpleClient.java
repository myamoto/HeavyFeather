package org.toolup.network.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.common.JSchLogger;
import org.toolup.network.common.ServerParams;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPSimpleClient {

	private final static Logger logger = LoggerFactory.getLogger(SFTPSimpleClient.class);
	
	private ServerParams params;

	public SFTPSimpleClient(ServerParams params) throws JSchException {
		this.params = params;
		JSch.setLogger(new JSchLogger());
	}
	
	public void uploadFile(String localFile, String remoteDir) throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;
		
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.put(localFile, remoteDir);
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public String readFile(String remoteFile) throws JSchException, SftpException, IOException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			
			try(InputStream is = channel.get(remoteFile)){
				return IOUtils.toString(is, StandardCharsets.UTF_8.name());
			}
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public void downloadFile(String remoteFile, String localFile) throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.get(remoteFile, localFile);
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public List<LsEntry> listFiles(String remoteDir) throws SftpException, JSchException {
		Session session = null;
		ChannelSftp channel = null;
		
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
		    List<LsEntry> files = new ArrayList<LsEntry>();
			for (Object o : channel.ls(remoteDir)) {
				files.add((LsEntry) o);
			}
			return files;
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public void removeFile(String remoteFile) throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;
		
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.rm(remoteFile);
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public void removeFileIfExists(String remoteFile) throws JSchException, SftpException {
		if (fileExists(remoteFile)) {
			removeFile(remoteFile);
		}
	}
	
	public void renameFile(String oldName, String newName) throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;
		
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.rename(oldName, newName);
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
		
	}
	
	public String pwd() throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;
		
		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			return channel.pwd();
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public boolean fileExists(String remoteFile) throws JSchException, SftpException {
		Session session = null;
		ChannelSftp channel = null;

		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			return channel.stat(remoteFile) != null;
		} catch (SftpException e) {
			if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
				return false;
			} else {
				throw e;
			}
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}
	
	public boolean canConnect() {
		Session session = null;
		ChannelSftp channel = null;

		try {
			session = createSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			return channel.isConnected();
		} catch (JSchException e) {
			logger.error(e.toString());
			return false;
		} finally {
			if (channel != null) channel.disconnect();
			if (session != null) session.disconnect();
		}
	}

	private Session createSession() throws JSchException {
		Session session = new JSch().getSession(params.getUserName(), params.getHostName());
		session.setPassword(params.getPassword());
		session.setConfig("StrictHostKeyChecking", "no");
		session.connect();
		return session;
	}
	
}
