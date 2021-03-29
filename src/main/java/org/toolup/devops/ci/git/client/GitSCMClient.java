package org.toolup.devops.ci.git.client;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;


public class GitSCMClient {
	
	public enum GITFLOWBRANCH{
		develop("develop"), 
		feature("feature/myfeature"), 
		release("release/myrelease"),
		hotfix("hotfix/myhotfix");
		private String label;
		
		private GITFLOWBRANCH(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	private String gitToken;
	
	public GitSCMClient gitToken(String gitToken) {
		this.gitToken = gitToken;
		return this;
	}
	
	public Git clone(String outputDirectory, String gitProjectUrl) throws GITSCMException, URISyntaxException {
		if(outputDirectory == null) throw new GITSCMException("outputDirectory cannot be null");
		if((!new File(outputDirectory).exists() && !new File(outputDirectory).mkdirs()) || !new File(outputDirectory).canWrite())
			throw new GITSCMException(String.format("workspaceDirectory %s must be writable", outputDirectory));
		try {
			Git result = Git.cloneRepository()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "", gitToken ))
					.setProgressMonitor(new TextProgressMonitor())
					.setURI(gitProjectUrl)
					.setDirectory(Paths.get(outputDirectory).toFile())
					.call();
			return result;
		} catch (GitAPIException e) {
			throw new GITSCMException(e);
		}
	}
	
	
	
	public void createGitflowBranches(Git git, GITFLOWBRANCH... branches) throws GITSCMException {
		if(git == null) throw new GITSCMException("git cannot be null.");
		if(branches == null || branches.length == 0) return;
		try {
			for (GITFLOWBRANCH branch : branches) {
				if("master".equals(branch.toString())) continue;
				String branchName = branch.toString();
				git.checkout()
				.setName(branchName
						)
				.setCreateBranch(true)
				.call();
				
				git.push()
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "", gitToken))
				.setRemote("origin")
				.setRefSpecs(new RefSpec(branchName+":"+branchName))
				.call();
			}
		}catch (GitAPIException e) {
			throw new GITSCMException(e);
		}
	}
	
	public void commitpush(Git git, String msg) throws GITSCMException {
		commitpush(git, msg, null);
	}
		
	public void commitpush(Git git, String msg, String branchName) throws GITSCMException {
		if(git == null) throw new GITSCMException("git cannot be null.");
		try {
			if(branchName != null)
				git.checkout().setCreateBranch(false).setName(branchName).call();
			
			git
			.add()
			.addFilepattern(".")
			.call();
			
			git
			.commit()
			.setMessage(msg)
			.call();

			git.push()
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "", gitToken ))
			.setRemote("origin")
			.call();
		}catch (GitAPIException e) {
			throw new GITSCMException(e);
		}
	}
	
}
