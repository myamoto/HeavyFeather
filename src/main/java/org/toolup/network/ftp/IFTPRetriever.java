package org.toolup.network.ftp;

import java.io.IOException;
import java.util.List;

import org.apache.commons.net.ftp.FTPFile;
import org.toolup.network.ftp.config.FTPServerParams;
import org.toolup.network.ftp.config.RemoteDirectory;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public interface IFTPRetriever {
	void downloadFiles(String server, int port,  String login
			, String password
			, List<RemoteDirectory> remoteDirList
			, String localPath, boolean explicitClose) throws JSchException, SftpException, IOException, FTPServiceException;

	void stop();


	boolean canConnect(FTPServerParams ftpParams);

	List<FTPFile> listRemoteFiles(String server, int port, String userName, String password, String remoteDirPath, boolean explicitClose);

	void close(String server, int port,  String login);

	
}
