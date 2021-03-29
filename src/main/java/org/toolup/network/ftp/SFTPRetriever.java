package org.toolup.network.ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.ftp.config.FTPServerParams;
import org.toolup.network.ftp.config.RemoteDirectory;
import org.toolup.network.ftp.session.SftpSession;
import org.toolup.network.ftp.session.SftpSessionKey;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPRetriever implements IFTPRetriever {
	
	private final static Logger logger = LoggerFactory.getLogger(SFTPRetriever.class);
	
	
	private Map<SftpSessionKey, SftpSession> sessions;
	
	public SFTPRetriever() {
		sessions = new HashMap<SftpSessionKey, SftpSession>();
	}

	
	private boolean stopped;

	@Override
	public List<FTPFile> listRemoteFiles(String server, int port, String login, String password, String remoteDirPath, boolean explicitClose) {
		if(!remoteDirPath.endsWith("/"))
			remoteDirPath += "/";
		SftpSession sftpsession = null;
		try {
			sftpsession = getSftpSession(server, port, login, password, explicitClose);
			
			List<FTPFile> result = new ArrayList<FTPFile>();

			Vector<?> filelist = sftpsession.getChannel().ls(remoteDirPath);
			for(int i = 0; i < filelist.size() ; i++){
				if(filelist.get(i) instanceof LsEntry){
					if(".".equals(((LsEntry)(filelist.get(i))).getFilename())) continue;
					if("..".equals(((LsEntry)(filelist.get(i))).getFilename())) continue;
					FTPFile ftpFile = new FTPFile();
					ftpFile.setName(remoteDirPath + ((LsEntry)(filelist.get(i))).getFilename());
					int fileType;
					if(((LsEntry)(filelist.get(i))).getAttrs().isDir()){
						fileType = FTPFile.DIRECTORY_TYPE;
					}else if(((LsEntry)(filelist.get(i))).getAttrs().isLink()) {
						fileType = FTPFile.SYMBOLIC_LINK_TYPE;
					}else {
						fileType = FTPFile.FILE_TYPE;
					}
					ftpFile.setType(fileType);
					result.add(ftpFile);
				}
			}

			return result;
		} catch (JSchException|SftpException e) {
			e.printStackTrace();
			logger.error("Error listing remote files", e);
		}finally {
			if(!explicitClose) {
				sftpsession.close();
			}
		}
		return null;
	}
	
	private SftpSession getSftpSession(String server, int port, String login, String password, boolean explicitClose) throws JSchException {
		SftpSession result = null;
		if(explicitClose) {
			SftpSessionKey key = new SftpSessionKey(server, port, login);
			result = sessions.get(key);
			if(result == null) {
				Session session = createSession(server, port, login, password);
				ChannelSftp sftpChannel = (ChannelSftp)session.openChannel("sftp");
				sftpChannel.connect();
				result = new SftpSession(sftpChannel, session);
				sessions.put(key, result);
			}
		}else {
			Session session = createSession(server, port, login, password);
			ChannelSftp sftpChannel = (ChannelSftp)session.openChannel("sftp");
			sftpChannel.connect();
			result = new SftpSession(sftpChannel, session);
		}
		return result;
	}
	
	private Session createSession(String server, int port,  String login, String password) throws JSchException {
		JSch jsch = new JSch();
		Session session = null;
		session = jsch.getSession(login, server, port);
		session.setConfig("StrictHostKeyChecking", "no");
		session.setPassword(password);
		session.connect();
		return session;
	}

	public void downloadFiles(String server, int port,  String login
			, String password, List<RemoteDirectory> remoteDirList, String localPath, boolean explicitClose) throws JSchException, SftpException, IOException, FTPServiceException{
		stopped = false;

		if(remoteDirList != null && !remoteDirList.isEmpty()){
			SftpSession sftpsession = null;
			try {
				sftpsession = getSftpSession(server, port, login, password, explicitClose);

				for (final RemoteDirectory remoteDirectory : remoteDirList) {
					try {
					downloadFiles(remoteDirectory.getPath()
							, remoteDirectory.isRecursiveDownload() 
							, localPath
							, remoteDirectory
							, sftpsession.getChannel()
							, server);
					}catch(FTPServiceException ex) {
						throw new FTPServiceException("problem downloading from " + server, ex);
					}
				}
			}finally {
				if(!explicitClose) {
					sftpsession.close();
				}
			}
		}
	}

	private void downloadFiles(String remotePath, boolean isRecursiveDownload,
			String localPath, SimpleNameFilter fileFilter, ChannelSftp sftpChannel, final String serverName) throws SftpException, IOException, FTPServiceException {
		Vector<?> filelist;
		try {
			filelist = sftpChannel.ls(remotePath);
		}catch(SftpException ex){
			throw new FTPServiceException("can't list " + remotePath , ex);
		}
		for(int i = 0; i < filelist.size() ; i++){
			if(isStopped()){
				break;
			}

			if(filelist.get(i) instanceof LsEntry){

				LsEntry entry = ((LsEntry)filelist.get(i));
				String remoteCompletePath = remotePath + "/" + entry.getFilename();
				if(entry.getAttrs().isDir()){
					if(isRecursiveDownload && !".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())){
						downloadFiles(remoteCompletePath, isRecursiveDownload , localPath, fileFilter, sftpChannel, serverName);
					}
				}else if(fileFilter.accept(entry.getFilename())){
					logger.info(String.format(" -> downloading %s from %s", remoteCompletePath, serverName));
					File newFile = new File(localPath, remoteCompletePath);
					if(!newFile.getParentFile().exists() && !newFile.getParentFile().mkdirs()){
						throw new IOException(String.format("can't create directory: %s", newFile.getParentFile().getAbsolutePath()));
					}
					try(BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile));
							BufferedInputStream bis = new BufferedInputStream(sftpChannel.get(remoteCompletePath));){
						byte[] buffer = new byte[1024];
						int readCount;
						while((readCount = bis.read(buffer)) > 0) {
							bos.write(buffer, 0, readCount);
						}
					}catch(IOException ex){
						logger.error("Error downloading remote files", ex);
					}
				}
			}
		}
	}

	public void stop() {
		stopped = true;
	}

	public boolean isStopped(){
		return stopped;
	}

	@Override
	public boolean canConnect(FTPServerParams ftpParams) {
		JSch jsch = new JSch();
		Session session = null;
		ChannelSftp channel = null;
		try {
			session = jsch.getSession(ftpParams.getUser(), ftpParams.getServer(), ftpParams.getPort());
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword(ftpParams.getPassword());
			session.connect();
			channel = (ChannelSftp)session.openChannel("sftp");
			channel.connect();
			return true;
		} catch (JSchException e) {
			return false;
		}finally {
			if(channel != null)channel.exit();
			if(session != null)session.disconnect();
		}
	}
	@Override
	public void close(String server, int port,  String login) {
		SftpSessionKey key = new SftpSessionKey(server, port, login);
		SftpSession session = sessions.get(key);
		if(session != null) session.close();
		sessions.remove(key);
	}

}
