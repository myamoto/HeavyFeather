package org.toolup.network.ftp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.network.NetTestUtil;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

public class SFTPSimpleClientTest {
	
	private final static Logger logger = LoggerFactory.getLogger(SFTPSimpleClientTest.class);

	private static final String REMOTE_TMP = "/tmp/";

	private SFTPSimpleClient sftpClient;
	
	@Before
	public void before() throws JSchException {
		this.sftpClient = new SFTPSimpleClient(NetTestUtil.env("srv0"));
	}

	@Test
	public void testListFiles() throws JSchException, SftpException {
		List<LsEntry> ftpFiles = sftpClient.listFiles(REMOTE_TMP);
		ftpFiles.forEach(f -> logger.debug(f.getLongname()));
	}
	
	@Test
	public void testUploadFile() throws JSchException, SftpException, IOException {
        Path tempFile = null;

		try {
			tempFile = createTempFile();
			sftpClient.uploadFile(tempFile.toString(), REMOTE_TMP);
			
			Assert.assertTrue(sftpClient.fileExists(REMOTE_TMP  + tempFile.getFileName()));
		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
		        sftpClient.removeFileIfExists(REMOTE_TMP + tempFile.getFileName());
			}
		}
	}
	
	@Test
	public void testDownloadFile() throws JSchException, SftpException, IOException {
		Path tempFile = null;

		try {
			String fileContent = "Test !";
			tempFile = createTempFile(fileContent);

			sftpClient.uploadFile(tempFile.toString(), REMOTE_TMP);

			Path dlFile = Paths.get("c:\\Temp\\test-dl.txt");
			sftpClient.downloadFile(REMOTE_TMP + tempFile.getFileName(), dlFile.toString());

			List<String> lines = Files.readAllLines(dlFile);

			Assert.assertTrue(Files.exists(dlFile));
			Assert.assertNotNull(lines);
			Assert.assertEquals(fileContent, lines.get(0));

		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
				sftpClient.removeFileIfExists(REMOTE_TMP + tempFile.getFileName());
			}
		}
	}
	
	@Test
	public void testRenameFile() throws JSchException, SftpException, IOException {
		
        Path tempFile = null;

		try {
			tempFile = createTempFile();
			sftpClient.uploadFile(tempFile.toString(), REMOTE_TMP);
			sftpClient.renameFile(REMOTE_TMP + tempFile.getFileName(), REMOTE_TMP + "renamed-" + tempFile.getFileName());
			
			Assert.assertTrue(sftpClient.fileExists(REMOTE_TMP + "renamed-" + tempFile.getFileName()));
		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
		        sftpClient.removeFileIfExists(REMOTE_TMP + tempFile.getFileName());
		        sftpClient.removeFileIfExists(REMOTE_TMP + "renamed-" + tempFile.getFileName());
			}
		}
	}
	
	@Test
	public void testRemoveFile() throws JSchException, SftpException, IOException {
		
        Path tempFile = null;

		try {
			tempFile = createTempFile();
			sftpClient.uploadFile(tempFile.toString(), REMOTE_TMP);
			sftpClient.removeFile(REMOTE_TMP + tempFile.getFileName());
			
			Assert.assertFalse(sftpClient.fileExists(REMOTE_TMP + tempFile.getFileName()));
		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
				sftpClient.removeFileIfExists(REMOTE_TMP + tempFile.getFileName());
			}
		}
	}
	
	@Test
	public void testFileExists() throws JSchException, SftpException, IOException {
		
        Path tempFile = null;

		try {
			tempFile = createTempFile();
			sftpClient.uploadFile(tempFile.toString(), REMOTE_TMP);
			Assert.assertTrue(sftpClient.fileExists(REMOTE_TMP + tempFile.getFileName()));
			
			sftpClient.removeFile(REMOTE_TMP + tempFile.getFileName());
			Assert.assertFalse(sftpClient.fileExists(REMOTE_TMP + tempFile.getFileName()));

		} finally {
			if (tempFile != null) {
				Files.delete(tempFile);
				sftpClient.removeFileIfExists(REMOTE_TMP + tempFile.getFileName());
			}
		}

	}
	
	@Test
	public void testPwd() throws JSchException, SftpException {
		String pwd = sftpClient.pwd();
		logger.debug(pwd);
		Assert.assertNotNull(pwd);
	}

	@Test
	public void testConnection() throws JSchException {
		Assert.assertTrue(sftpClient.canConnect());
	}
	
	private Path createTempFile() throws IOException {
		return createTempFile("Test !");
	}
	
	private Path createTempFile(String fileContent) throws IOException {
		Path tempFile = Files.createTempFile(null, null);
		Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        logger.debug("Temp file : {} ", tempFile);
		return tempFile;
	}

}
