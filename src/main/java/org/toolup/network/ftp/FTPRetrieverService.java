package org.toolup.network.ftp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.ftp.config.FTPServerParams;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;



public class FTPRetrieverService{
	
	private final static Logger logger = LoggerFactory.getLogger(FTPRetrieverService.class);
	
	private boolean explicitClose;

	private enum ProtocolFTP {sftp, ftp}

	private static Map<ProtocolFTP, IFTPRetriever> ftpRetrieverMap;

	static{
		ftpRetrieverMap = new HashMap<>();
		ftpRetrieverMap.put(ProtocolFTP.sftp, new SFTPRetriever());
		ftpRetrieverMap.put(ProtocolFTP.ftp, new FTPRetriever());
	}

	private boolean stopped;
	
	public void setExplicitClose(boolean explicitClose) {
		this.explicitClose = explicitClose;
	}

	private void unZipIt(String zipFilePath, String outputFolder) throws IOException{
		File zipFile = new File(zipFilePath);
		if(!zipFile.exists()) return;
		
		String outputFolderToUse;
		if(outputFolder != null){
			File folder = new File(outputFolder);
			if(!folder.exists()) folder.mkdirs();
			outputFolderToUse = outputFolder;
		}else{
			String folderName = zipFile.getName().endsWith(".zip") ? 
					zipFile.getName().substring(0, zipFile.getName().length() - 4) : zipFile.getName() + "_dir";
			outputFolderToUse = new File(zipFile.getParent(), folderName).getAbsolutePath();
		}

		new File(outputFolderToUse).mkdirs();
		ZipFile zipFilee = null;
		try{

			logger.debug(String.format(String.format("unzipping %s...", zipFile.getAbsolutePath())));

			zipFilee = new ZipFile(zipFile);
			Enumeration<?> entries = zipFilee.entries();
			InputStream zeIs = null;
			byte[] buffer = new byte[1024];

			for (Object o = entries.nextElement(); entries.hasMoreElements(); o = entries.nextElement()) {

				if(isStopped()){
					break;
				}

				if(o instanceof ZipEntry){
					ZipEntry ze = (ZipEntry)o;
					String fileName = ze.getName();
					File newFile = new File(outputFolderToUse, fileName);
					if(!newFile.toPath().normalize().startsWith(outputFolderToUse)) {
						throw new IOException("Bad zip entry");
					}
					if(ze.isDirectory()){
						newFile.mkdirs();
					}else{
						FileOutputStream fos = new FileOutputStream(newFile);  
						try{
							int len;

							zeIs = zipFilee.getInputStream(ze);

							while ((len = zeIs.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
						}catch(IOException ex){
							throw new IOException(String.format("error on : %s", fileName), ex);
						}finally{
							fos.close();  
							if(zeIs != null){
								zeIs.close();
							}
						} 
					}
				}
			}
		}catch(ZipException ex){
			throw new IOException(String.format("error on : %s", zipFile.getAbsolutePath()), ex);
		}finally{
			if(zipFilee != null){
				zipFilee.close();
			}
		}
	}
	
	private IFTPRetriever getFtpRetriever(FTPServerParams ftpParams) {
		ProtocolFTP protocol;
		if(ftpParams.isSftp()){
			protocol = ProtocolFTP.sftp;
		}else{
			protocol = ProtocolFTP.ftp;
		}

		return ftpRetrieverMap.get(protocol);
	}
	
	public List<FTPFile> listRemoteFiles(FTPServerParams ftpParams, String remoteDirPath){
		return getFtpRetriever(ftpParams).listRemoteFiles(ftpParams.getServer()
				, ftpParams.getPort()
				, ftpParams.getUser()
				, ftpParams.getPassword()
				, remoteDirPath, explicitClose);
	}
	
	public File downloadAndUnzip(FTPServerParams ftpParams, String outputDirPath, boolean overrideDirectory, boolean unzip) throws IOException, JSchException, SftpException, FTPServiceException{
		return downloadAndUnzip(ftpParams, outputDirPath, null, overrideDirectory, false, unzip);
	}

	public File downloadAndUnzip(FTPServerParams ftpParams, String outputDirPath, SimpleNameFilter fileFilter, boolean overrideDirectory, boolean overrideFiles, boolean unzip) throws IOException, JSchException, SftpException, FTPServiceException{
		stopped = false;
		File localDir = new File(outputDirPath, ftpParams.getServer());

		if(localDir.exists() && !overrideDirectory){
			logger.info(String.format(String.format("missing directory (%s), ignoring server %s.", localDir.getAbsolutePath(), ftpParams.getServer())));
		}else{
			if(!overrideFiles) {
				deleteDirectoryRecur(localDir);
				if(!localDir.mkdirs()){
					throw new IOException(String.format("can't create directory %s.", localDir.getAbsolutePath()));
				}
			}
			
			getFtpRetriever(ftpParams).downloadFiles(
					ftpParams.getServer()
					, ftpParams.getPort()
					, ftpParams.getUser()
					, ftpParams.getPassword()
					, ftpParams.getRemoteDirList()
					, localDir.getAbsolutePath()
					, explicitClose);

			if(unzip)unzipRecur(outputDirPath, null);
		}
		return localDir;
	}
	
	public void close(FTPServerParams ftpParams) {
		getFtpRetriever(ftpParams).close(ftpParams.getServer(), ftpParams.getPort(), ftpParams.getUser());
	}

	public static void deleteDirectoryRecur(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectoryRecur(files[i]);
				} else {
					files[i].delete();
				}
			}
		}

		path.delete();
	}

	private void unzipRecur(String logDirPath, String outputDirPath) throws IOException {
		unzipRecur(logDirPath, outputDirPath, true);
	}

	public void unzipRecur(String logDirPath, String outputDirPath, boolean delete) throws IOException {
		File logDirFile = new File(logDirPath);
		if(logDirFile.isDirectory()){
			File[] fileList = logDirFile.listFiles();
			for (File file : fileList) {

				if(isStopped()){
					break;
				}

				if(file.getName().endsWith(".zip")){
					unZipIt(file.getAbsolutePath(), outputDirPath);
					if(delete && !file.delete()){
						logger.warn(String.format(String.format("failed deleting : %s", file.getAbsolutePath())));
					}
				}else if(file.isDirectory()){
					unzipRecur(file.getAbsolutePath(), outputDirPath);
				}
			}
		}
	}

	public void stop() {
		stopped = true;
		for (ProtocolFTP protocol : ftpRetrieverMap.keySet()) {
			ftpRetrieverMap.get(protocol).stop();
		}	
	}

	public boolean isStopped(){
		return stopped;
	}
	
	public boolean canConnect(FTPServerParams ftpParams) {
		return ftpRetrieverMap.get(ProtocolFTP.sftp).canConnect(ftpParams);
	}



}
