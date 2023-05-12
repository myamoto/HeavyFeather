package org.toolup.devops.ci.git.client;

import java.io.File;

public class GitRetrieverCfg {
	
	private String gitlabBaseURL;
	private String gitProjectURL;
	private String workingDirectoryPath;
	private String gitPersonalToken;
	private String workspace;
	private String branch;
	
	public String getBranch() {
		return branch;
	}

	public GitRetrieverCfg branch(String branch) {
		this.branch = branch;
		return this;
	}

	public GitRetrieverCfg gitProjectURL(String gitProjectURL) {
		this.gitProjectURL = gitProjectURL;
		return this;
	}

	public String getGitProjectURL() {
		return gitProjectURL;
	}
	
	
	public String getWorkingDirectoryPath() {
		return workingDirectoryPath;
	}

	public GitRetrieverCfg workingDirectoryPath(String workingDirectory) {
		this.workingDirectoryPath = workingDirectory;
		return this;
	}
	
	public String getGitPersonalToken() {
		return gitPersonalToken;
	}

	public GitRetrieverCfg gitPersonalToken(String gitPersonalToken) {
		this.gitPersonalToken = gitPersonalToken;
		return this;
	}

	public String getGitlabBaseURL() {
		return gitlabBaseURL;
	}

	public GitRetrieverCfg gitlabBaseURL(String gitlabBaseURL) {
		this.gitlabBaseURL = gitlabBaseURL;
		return this;
	}

	public String getProjectRelativeDir() {
		StringBuilder result = new StringBuilder();
		if(workspace != null) result.append(workspace).append("/");
		
		String gitProjectPath = gitProjectURL.substring(gitlabBaseURL.length());
		if(gitProjectPath.endsWith(".git"))
			gitProjectPath = gitProjectPath.substring(0, gitProjectPath.length() - 4);
		result.append(gitProjectPath);
		if(branch != null) result.append("/").append(branch);
		return result.toString();
	}

	public GitRetrieverCfg workspace(String workspace) {
		this.workspace = workspace;
		return this;
	}
	
	public String getWorkspace() {
		return workspace;
	}
	
	public String getGitProjectFullPath() {
		if(gitProjectURL == null || gitlabBaseURL == null) return null;
		
		String result = gitProjectURL.substring(gitlabBaseURL.length(), gitProjectURL.length());
		String unwantedSufix = ".git";
		if(result.endsWith(unwantedSufix)) result = result.substring(0, result.length() - unwantedSufix.length());
		if(result.startsWith("/")) result = result.substring(1);
		return result;
	}

	public String getProjectDirPath() {
		return new File(getWorkingDirectoryPath(), getProjectRelativeDir()).getAbsolutePath();
	}

	@Override
	public String toString() {
		return "GitRetrieverCfg [gitlabBaseURL=" + gitlabBaseURL + ", gitProjectURL=" + gitProjectURL
				+ ", workingDirectoryPath=" + workingDirectoryPath + ", gitPersonalToken=" + gitPersonalToken
				+ ", workspace=" + workspace + ", branch=" + branch + "]";
	}

	
}
