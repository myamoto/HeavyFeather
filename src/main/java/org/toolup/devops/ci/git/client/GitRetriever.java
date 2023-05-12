package org.toolup.devops.ci.git.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GitRetriever {

	private static final String LOCAL_BRANCHES_PFX = "refs/heads/";
	private static final String REMOTE_BRANCHES_PFX = "refs/remotes/origin/";

	private static Logger logger = LoggerFactory.getLogger(GitRetriever.class);

	private ProgressMonitor progressMonitor;

	private String defaultMasterBranch = "master";

	private String followBranch = null;

	private GitRetrieverCfg config;

	public GitRetriever progressMonitor(ProgressMonitor progressMonitor) {
		this.progressMonitor = progressMonitor;
		return this;
	}

	public GitRetriever defaultMasterBranch(String defaultMasterBranch) {
		this.defaultMasterBranch = defaultMasterBranch;
		return this;
	}

	public GitRetriever followBranch(String followBranch) throws GITSCMException {
		if(followBranch == null) throw new GITSCMException("branch has to be set.");

		Ref remote = findRef(getOriginBranchName(followBranch));
		if(remote == null) throw new GITSCMException("branch " + followBranch + "not found in origin branches.");

		this.followBranch = followBranch;
		return this;
	}

	private Git getCurrentProject() throws GITSCMException {
		if(config == null)
			throw new GITSCMException("must set config first.");
		File projectDir = new File(config.getProjectDirPath());
		File gitFile = new File(projectDir, ".git");
		if(!projectDir.exists() || !gitFile.exists()) {
			retrieveCurrentFiles(false);
		}
		try {
			Git result = Git.open(gitFile);
			if(followBranch == null) 
				followBranch = getTrackingBranch(result.getRepository());
			return result;
		} catch (IOException e) {
			throw new GITSCMException(e);
		}
	}
	public void checkout() throws GITSCMException {
		checkout(null);
	}

	public void fetch() throws GITSCMException {
		try {
			getCurrentProject()
			.fetch()
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider("", config.getGitPersonalToken()))
			.call();
			logger.info("fetch done.");
		} catch (GitAPIException  e) {
			throw new GITSCMException(e);
		}
	}

	private String getFollowBranch() {
		if(followBranch == null) return defaultMasterBranch;
		return followBranch;
	}
	
	public void checkout(String commitHash) throws GITSCMException {
		String targetCheckoutObj = null;
		Ref targetRef = null;
		final String followBranch = getFollowBranch();
		try (Git git = getCurrentProject()){
			Ref headRef = getHeadRef();
			if(commitHash == null) {
				final String remoteBranch = "refs/remotes/origin/" + followBranch;
				Ref remoteOriginRef = findRef(remoteBranch);
				if(remoteOriginRef != null) {
//					targetCheckoutObj = remoteOriginRef.getObjectId().getName();
//					targetRef = remoteOriginRef;
					if(!remoteOriginRef.getObjectId().equals(headRef.getObjectId())) {
						logger.warn("checkout() : pull may be necessary. remoteOriginRef commitID '{}' last of branch {} different from HeadRef."
								, remoteOriginRef.getObjectId().getName(), remoteBranch);
						debugBehindCommits();
					}
				}
				targetCheckoutObj = followBranch;
				targetRef = getBranchLocalRef();
			}else {
				targetCheckoutObj = commitHash;
				targetRef = findRef(targetCheckoutObj);
			}
			logger.debug("checkout() : will call checkout('{}') from startPoint('{}').", targetCheckoutObj, followBranch);
			logger.debug("checkout : localRef objectId is '{}'", targetRef == null ? null : targetRef.getObjectId().getName());
			logger.debug("checkout : headRef objectId is '{}'", headRef == null ? null : headRef.getObjectId().getName());
			
			if(headRef != null && targetRef != null && headRef.getObjectId().equals(targetRef.getObjectId())) {
				logger.info("checkout : nothing to do. headRef and targetRef are the same '{}'.", headRef.getObjectId().getName());
				return;
			}
			
			CheckoutCommand cmd = git.checkout()
					.setProgressMonitor(progressMonitor)
					.setName(targetCheckoutObj);
			if(commitHash == null)
				cmd
				.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
				.setStartPoint("origin/"+followBranch)
				.setCreateBranch(targetRef == null);
			cmd.call();
			logger.info("checkout done. we now are on ref {}. HEAD is {}", followBranch , getHeadRef().getObjectId().getName());
		} catch (GitAPIException  e) {
			throw new GITSCMException(e, "commitId " + targetCheckoutObj + ", localRef " + targetRef);
		}
	}

	private Ref findRef(String objectIdName) throws GITSCMException {
		return findRef(objectIdName, getAllBranches());
	}
	
	private Ref findRef(String objectIdName, List<Ref> branches) {
		if(objectIdName == null || branches == null) return null;
		Optional<Ref> matchingRef = branches
				.stream()
				.filter(r -> objectIdName.equals(r.getName()))
				.findFirst();
		return matchingRef.isPresent() ? matchingRef.get() : null;
	}

	private Ref getBranchLocalRef() throws GITSCMException {
		return findRef(getLocalBranchName());
	}

	private Ref getBranchOriginRef() throws GITSCMException {
		return findRef(getOriginBranchName());
	}
	
	private List<Ref> getAllBranches() throws GITSCMException{
		try (Git git = getCurrentProject()){
			return git.branchList().setListMode(ListMode.ALL).call();
		} catch (GitAPIException | GITSCMException e) {
			throw new GITSCMException(e);
		}
	}

	private String getLocalBranchName(String branch) {
		return LOCAL_BRANCHES_PFX + branch;
	}

	private String getLocalBranchName() {
		return getLocalBranchName(getFollowBranch());
	}

	private String getOriginBranchName(String branch) {
		return REMOTE_BRANCHES_PFX + branch;
	}

	private String getOriginBranchName() {
		return getOriginBranchName(getFollowBranch());
	}

	public List<String> listBranches() throws GITSCMException {
		List<Ref> allBranches = getAllBranches();
		return allBranches.stream()
				.filter(r -> r.getName().startsWith(REMOTE_BRANCHES_PFX))
				.map(r -> getShortRemoteBranchName(r.getName()))
				.collect(Collectors.toList());
	}

	private String getShortRemoteBranchName(String branchName) {
		if(branchName == null || !branchName.startsWith(REMOTE_BRANCHES_PFX))
			return branchName;
		return branchName.substring(REMOTE_BRANCHES_PFX.length());
	}

	public List<RevCommit> getBehindCommits() throws GITSCMException {
		try (Git git = getCurrentProject(); RevWalk walk = new RevWalk(git.getRepository())) {
			Ref local = getBranchLocalRef();
			if(local == null)
				throw new GITSCMException(getFollowBranch() + ": local branch not found. Forgot to checkout ?");

			Ref remote = getBranchOriginRef();
			if(remote == null)
				throw new GITSCMException(getFollowBranch() + ": origin branch not found. was branch deleted ?");
			RevCommit localCommit = walk.parseCommit(local.getObjectId());
			RevCommit trackingCommit = walk.parseCommit(remote.getObjectId());
			logger.debug("local commit is {}, msg : {}, date : {}", localCommit.getName(), localCommit.getShortMessage(), localCommit.getAuthorIdent().getWhen());
			logger.debug("remote commit is {}, msg : {}, date : {}", trackingCommit.getName(), trackingCommit.getShortMessage(), trackingCommit.getAuthorIdent().getWhen());
			walk.setRevFilter(RevFilter.MERGE_BASE);
			walk.markStart(localCommit);
			walk.markStart(trackingCommit);
			RevCommit mergeBase = walk.next();
			walk.reset();
			walk.setRevFilter(RevFilter.ALL);
			//			int aheadCount = RevWalkUtils.count(walk, localCommit, mergeBase);
			List<RevCommit> result = RevWalkUtils.find(walk, trackingCommit, mergeBase);
			result.remove(localCommit);
			return result;
		}catch(IOException ex) {
			throw new GITSCMException(ex);
		}
	}
	
	private void debugBehindCommits() {
		if(logger.isDebugEnabled()) {
			try {
				List<RevCommit> behindCommits = getBehindCommits();
				logger.debug("{} commits behind head.", behindCommits.size());
				for (RevCommit c : behindCommits) {
					logger.debug(" - {} - {} by {} : {}"
							, c.getAuthorIdent().getWhen()
							, c.getId().getName()
							, c.getAuthorIdent().getName() 
							, c.getShortMessage());
				}
			} catch (GITSCMException e) {
				logger.error("debug trouble {}", e);
			}
		}
	}

	public void pull() throws GITSCMException {
		try (Git git = getCurrentProject();){
			if(logger.isInfoEnabled()) {
				logger.info("pulling {} commits.", getBehindCommits().size());
			}
			debugBehindCommits();
			git.pull()
			.setProgressMonitor(progressMonitor)
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider("", config.getGitPersonalToken()))
			.call();
			logger.info("pull done. Now HEAD is  {}", getHeadRef().getObjectId().getName());
		} catch (GitAPIException e) {
			throw new GITSCMException(e);
		}

	}

	private Ref getHeadRef() throws GITSCMException {
		try (Git git = getCurrentProject()){
			return git.getRepository()
					.getRefDatabase()
					.findRef("HEAD");
		} catch (IOException e) {
			throw new GITSCMException(e);
		}
	}

	public RevCommit getCurrentRevCommit() throws GITSCMException {
		try (Git git = getCurrentProject(); RevWalk walk = new RevWalk(git.getRepository())) {
			return walk.parseCommit(getHeadRef().getObjectId());
		} catch (IOException e) {
			throw new GITSCMException(e);
		}
	}

	public Ref getCurrentRef() throws GITSCMException {
		return getHeadRef();
	}

	public String getFullBranchName() throws GITSCMException {
		try (Git git = getCurrentProject()){
			return git.getRepository().getFullBranch();
		} catch (IOException e) {
			throw new GITSCMException(e);
		}
	}

	public String getCurrentRefShortName() throws GITSCMException {
		Ref currentRef = getHeadRef();
		if(currentRef == null) return null;
		String result = currentRef.getTarget().getName();
		if(result.startsWith(LOCAL_BRANCHES_PFX))
			result = result.substring(LOCAL_BRANCHES_PFX.length());
		return result;
	}

	public String getCurrentCommitId() throws GITSCMException {
		return getHeadRef().getObjectId().getName();
	}

	public Ref retrieveCurrentFiles(boolean forceReload) throws GITSCMException {
		cloneProject(forceReload);
		return getHeadRef();
	}

	public String getTrackingBranch() throws GITSCMException {
		try(Git git = getCurrentProject()){
			return getTrackingBranch(git.getRepository());
		}
	}

	public String getTrackingBranch(Repository repo) throws GITSCMException {
		try {
			return getShortRemoteBranchName(new BranchConfig(repo.getConfig(), repo.getBranch()).getTrackingBranch());
		} catch ( IOException e) {
			throw new GITSCMException(e);
		}
	}

	public List<DiffEntry> getLastChangedFiles(int nbCommits) throws GITSCMException{

		try(Git git = getCurrentProject()){
			Iterator<RevCommit> iter = git.log().setMaxCount(nbCommits).call().iterator();

			if(!iter.hasNext()) return null;
			RevCommit crt = iter.next();
			if(!iter.hasNext()) return null;
			RevCommit previous;
			int cpt = 0;
			do {
				previous = iter.next();
			}while(cpt ++ < nbCommits && iter.hasNext());

			return git.diff()
					.setOldTree(prepareTreeParser(git.getRepository(), crt.getId()))
					.setNewTree(prepareTreeParser(git.getRepository(), previous.getId()))
					.call();
		} catch (GitAPIException e) {
			throw new GITSCMException(e);
		}
	}

	private static AbstractTreeIterator prepareTreeParser(Repository repository, AnyObjectId ref) throws GITSCMException  {
		try {
			RevTree tree = null;
			try (RevWalk walk = new RevWalk(repository);){
				RevCommit commit = walk.parseCommit(ref);
				tree = walk.parseTree(commit.getTree().getId());
			}

			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			try (ObjectReader oldReader = repository.newObjectReader()){
				oldTreeParser.reset(oldReader, tree.getId());
			}
			return oldTreeParser;
		} catch (IOException  e) {
			throw new GITSCMException(e);
		}
	}

	private void cloneProject(boolean forceReload) throws GITSCMException {
		try {
			File projectDir = new File(config.getProjectDirPath());
			if(forceReload) FileUtils.deleteDirectory(projectDir);
			if(!projectDir.exists() && !projectDir.mkdirs())
				throw new GITSCMException(String.format("could not create dir %s", projectDir.getAbsolutePath()));

			File gitFile = new File(projectDir, ".git");
			if(gitFile.exists()) logger.info("git project found locally : {}", gitFile.getAbsolutePath());

			if(!gitFile.exists()) {
				logger.info("cloning {} in {}...", config.getGitProjectURL(), projectDir.getAbsolutePath());
				clone(projectDir.getAbsolutePath(), config.getGitProjectURL());
				logger.info("cloning success complete");
			}
			if(!projectDir.exists() || !gitFile.exists())
				throw new GITSCMException("git retrieval failed.");
		} catch (IOException  e) {
			throw new GITSCMException(e);
		}
	}

	private Git clone(String outputDirectory, String gitProjectUrl) throws GITSCMException {
		if(outputDirectory == null) throw new GITSCMException("outputDirectory cannot be null");
		if((!new File(outputDirectory).exists() && !new File(outputDirectory).mkdirs()) || !new File(outputDirectory).canWrite())
			throw new GITSCMException(String.format("workspaceDirectory %s must be writable", outputDirectory));
		try {
			return Git.cloneRepository()
					.setCredentialsProvider(new UsernamePasswordCredentialsProvider( "", config.getGitPersonalToken() ))
					.setProgressMonitor(progressMonitor)
					.setURI(gitProjectUrl)
					.setDirectory(Paths.get(outputDirectory).toFile())
					.call();
		} catch (GitAPIException e) {
			throw new GITSCMException(e);
		}
	}

	public GitRetriever config(GitRetrieverCfg config) {
		this.config = config;
		return this;
	}

	public GitRetrieverCfg getConfig() {
		return config;
	}

	public String getGitProjectUrl() throws GITSCMException {
		if(config == null)
			throw new GITSCMException("must set config first.");
		return getConfig().getGitProjectURL();
	}


}
