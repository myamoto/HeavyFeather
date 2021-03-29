package org.toolup.network.ftp;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.ftp.config.FTPServerParams;
import org.toolup.network.ftp.config.RemoteDirectory;


public class FTPRetriever implements IFTPRetriever {

	private final static Logger logger = LoggerFactory.getLogger(FTPRetriever.class);
	
	private boolean stopped;

	private FTPClient createFTPClient(String server, int port,  String login , String password) throws SocketException, IOException {
		FTPClient ftp = new FTPClient();
		FTPClientConfig config = new FTPClientConfig();

		ftp.configure(config);
		int reply;
		ftp.connect(server, port);
		ftp.login(login, password);

		reply = ftp.getReplyCode();

		if(!FTPReply.isPositiveCompletion(reply)) {
			ftp.disconnect();
			throw new IOException(String.format("!FTPReply.isPositiveCompletion(%d)", reply));
		}
		return ftp;
	}

	@Override
	public List<FTPFile> listRemoteFiles(String server, int port, String login, String password, String remoteDirPath, boolean explicitClose) {

		FTPClient ftp = null;
		try {
			ftp = createFTPClient(server, port, login, password);

			if(!remoteDirPath.endsWith("/")){
				remoteDirPath = remoteDirPath.concat("/");
			}
			FTPFile[] files = ftp.listFiles(remoteDirPath);
			List<FTPFile> result = new ArrayList<FTPFile>();
			for (FTPFile ftpFile : files) {
				if(".".equals(ftpFile.getName())) continue;
				if("..".equals(ftpFile.getName())) continue;
				FTPFile finalFtpFile = new FTPFile();
				finalFtpFile.setType(ftpFile.getType());
				finalFtpFile.setName(remoteDirPath + ftpFile.getName());
				result.add(ftpFile);
			}
			return result;
		} catch(IOException e) {
			logger.error(String.format("Error listing files from %s", server), e);
		} finally {
			if(ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {}
			}
		}
		return null;
	}

	public void downloadFiles(String server, int port,  String login
			, String password, List<RemoteDirectory> remoteDirList, String localPath, boolean explicitClose){
		stopped = false;
		FTPClient ftp = null;
		try {
			ftp = createFTPClient(server, port, login, password);
			for (final RemoteDirectory remoteDirectory : remoteDirList) {

				String remotePath = remoteDirectory.getPath();
				boolean isRecursiveDownload = remoteDirectory.isRecursiveDownload();

				downloadFiles(server, port, login, password, remotePath, isRecursiveDownload, localPath, remoteDirectory, ftp);

			}
			ftp.logout();
		} catch(IOException e) {
			logger.error(String.format("Error downloading files from %s", server), e);
		} finally {
			if(ftp != null && ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch(IOException ioe) {}
			}
		}
	}

	private void downloadFiles(String server, int port, String login,
			String password, String remotePath, boolean isRecursiveDownload,
			String localPath, SimpleNameFilter fileFilter, FTPClient ftp) throws IOException {
		if(!remotePath.endsWith("/")){
			remotePath = remotePath.concat("/");
		}

		FTPFile[] files = ftp.listFiles(remotePath);

		for (FTPFile ftpFile : files) {

			if(isStopped()){
				break;
			}
			String remoteCompletePath = remotePath + ftpFile.getName();

			if(ftpFile.isDirectory() && isRecursiveDownload && !".".equals(ftpFile.getName()) && !"..".equals(ftpFile.getName())){

				downloadFiles(server, port,  login , password, remoteCompletePath, isRecursiveDownload , localPath, fileFilter, ftp);
			}else if(fileFilter.accept(ftpFile.getName())){
				BufferedOutputStream bos = null;
				logger.info(String.format("  -> downloading %s", ftpFile.getName()));
				try{
					File newFile = new File(localPath, remoteCompletePath);
					if(!newFile.getParentFile().mkdirs() || !newFile.createNewFile()){
						throw new IOException(String.format("can't create file %s", newFile.getAbsolutePath()));
					}
					bos = new BufferedOutputStream(new FileOutputStream(newFile));
					ftp.retrieveFile(remoteCompletePath, bos);
				}finally{
					if(bos != null){
						bos.close();
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
		FTPClient ftp = new FTPClient();
		try {
			FTPClientConfig config = new FTPClientConfig();

			ftp.configure(config);
			ftp.connect(ftpParams.getServer(), ftpParams.getPort());
			return ftp.login(ftpParams.getUser(), ftpParams.getPassword());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally {
			try {
				ftp.logout();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close(String server, int port,  String login) {
		
	}
}
