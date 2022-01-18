package org.toolup.devops.ci.git.gitlab.apiclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.toolup.devops.ci.git.gitlab.apiclient.GitProjectMemberShip.AccessLevel;
import org.toolup.network.http.HTTPWrapper;
import org.toolup.network.http.HTTPWrapperException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

public class GitRESTClient {

	final Logger logger = LoggerFactory.getLogger(GitRESTClient.class);

	private final HTTPWrapper httpWrapper = new HTTPWrapper();

	private Map<String, GitUser> gitUserCache = new HashMap<>();

	private String gitBaseUrl;
	private String gitPersonalApiToken;

	private boolean failWhenNotAdmin = true;
	
	private static final  ObjectMapper objectMapper = new ObjectMapper();
	static {
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	protected void protectedmethod() {}
	
	public GitRESTClient failWhenNotAdmin(boolean failWhenNotAdmin) {
		this.failWhenNotAdmin = failWhenNotAdmin;
		return this;
	}

	public GitRESTClient gitBaseUrl(String gitBaseUrl) throws GITRESTClientException {
		if(gitPersonalApiToken == null)
			throw new GITRESTClientException("first set the gitPersonalApiToken.");
		if(gitBaseUrl == null || gitBaseUrl.isEmpty()) {
			throw new GITRESTClientException("GIT base url can't be empty.");
		}
		//test token validity
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();){
			httpGETParsedJsonDocument(String.format("%sapi/v4/projects",HTTPWrapper.slashTerminatedUrl(gitBaseUrl)), httpClient);
		} catch (GITRESTClientException e) {
			if(e.getCause() instanceof HTTPWrapperException && ((HTTPWrapperException)e.getCause()).getStatusCode() != 500) throw new GITRESTClientException(e);
		} catch (IOException e) {
			throw new GITRESTClientException(e);
		}

		if(failWhenNotAdmin) {
			//check user the token belongs too has admin priviledges
			try(CloseableHttpClient httpClient = HttpClientBuilder.create().build();){
				httpGETParsedJsonDocument(String.format("%sapi/v4/user/activities",HTTPWrapper.slashTerminatedUrl(gitBaseUrl)), httpClient);
			} catch (IOException e) {
				throw new GITRESTClientException(e);
			}
		}

		this.gitBaseUrl = gitBaseUrl;
		return this;
	}

	private String getGitUrlCommits(String projectId) {
		return String.format("%s/repository/commits", getGitUrlProjectById(projectId));
	}

	private String getGitUrlCommit(String projectId, String commitId) {
		return String.format("%s/%s", getGitUrlCommits(projectId), commitId);
	}

	private Header getGitSecurityHeader() {
		return new BasicHeader("Private-Token", gitPersonalApiToken);
	}

	private String getGitApiUrl(){
		return String.format("%sapi/v4", HTTPWrapper.slashTerminatedUrl(gitBaseUrl));
	}

	private String getGitUrlAllGroups(){
		return String.format("%s/groups", getGitApiUrl());
	}

	private String getGitUrlGroupById(String groupFullPath) throws UnsupportedEncodingException{
		return String.format("%s/groups/%s", getGitApiUrl(), URLEncoder.encode(groupFullPath, Consts.UTF_8.name()));
	}

	private String getGitUrlGroupSubgroupsById(String groupFullPath) throws UnsupportedEncodingException {
		return String.format("%s/subgroups", getGitUrlGroupById(groupFullPath));
	}
	private String getGitUrlProjects() {
		return  String.format("%s/projects", getGitApiUrl());
	}
	private String getGitUrlProjectMembersById(String memberId) {
		return  String.format("%s/%s/members/all", getGitUrlProjects(), memberId);
	}

	private String getGitUrlUserById(String userId) throws UnsupportedEncodingException {
		return  String.format("%s/users/%s", getGitApiUrl(), URLEncoder.encode(userId, Consts.UTF_8.name()));
	}

	private String getGitUrlProjectById(String projectId){
		return String.format("%s/projects/%s", getGitApiUrl(), projectId);
	}

	private String getGitUrlUserCurrent(){
		return String.format("%s/user", getGitApiUrl());
	}

	private String getGitUrlCreateProject(String projectName, String projectPath, String namespaceId) throws UnsupportedEncodingException{
		return String.format("%s/projects?name=%s&path=%s%s"
				, getGitApiUrl()
				, URLEncoder.encode(projectName, Consts.UTF_8.name())
				, URLEncoder.encode(projectPath, Consts.UTF_8.name())
				, namespaceId != null ? String.format("&namespace_id=%s", URLEncoder.encode(namespaceId, Consts.UTF_8.name())) : ""
				);
	}

	private String getGitUrlDeleteProject(String projectPath) throws UnsupportedEncodingException{
		return String.format("%s/projects/%s", getGitApiUrl(), URLEncoder.encode(projectPath, Consts.UTF_8.name()));
	}

	private String getGitUrlCreateGroup(String groupId, String groupPath) throws UnsupportedEncodingException{

		return String.format("%s/groups?name=%s&path=%s", getGitApiUrl()
				, URLEncoder.encode(groupId, Consts.UTF_8.name())
				, URLEncoder.encode(groupPath != null ? groupPath : groupId, Consts.UTF_8.name()));
	}

	private String getGitUrlDeleteGroup(String groupId) throws UnsupportedEncodingException{
		return String.format("%s/groups/%s", getGitApiUrl(), URLEncoder.encode(groupId, Consts.UTF_8.name()));
	}

	public GitRESTClient gitPersonalAPIToken(String gitPersonalApiToken) throws GITRESTClientException {
		this.gitPersonalApiToken = gitPersonalApiToken;
		if(gitPersonalApiToken == null || gitPersonalApiToken.isEmpty()) {
			throw new GITRESTClientException("GIT personal API Token can't be empty.");
		}
		return this;
	}

	public String getGitPersonalApiToken() {
		return gitPersonalApiToken;
	}

	private Object httpGETParsedJsonDocument(String url, CloseableHttpClient httpClient) throws GITRESTClientException {
		try {
			return httpWrapper.httpGETParsedJson(url, httpClient, Arrays.asList(getGitSecurityHeader()));
		}catch(HTTPWrapperException e) {
			if(e.getStatusCode() == 404) return null;
			handleSecurityException(e);
		}
		return null;
	}

	private CloseableHttpResponse httpPOST(String url, CloseableHttpClient httpClient) throws GITRESTClientException {
		try (CloseableHttpResponse resp = httpWrapper.httpPOST(url, httpClient, Arrays.asList(getGitSecurityHeader()))){
			return resp;
		}catch(HTTPWrapperException e) {
			handleSecurityException(e);
			return null;
		} catch (UnsupportedOperationException e) {
			throw new GITRESTClientException(e);
		} catch (IOException e) {
			throw new GITRESTClientException(e);
		}
	}

	private void httpDelete(String url, CloseableHttpClient httpClient) throws GITRESTClientException {
		try {
			httpWrapper.httpDelete(url, httpClient, Arrays.asList(getGitSecurityHeader()));
		}catch(HTTPWrapperException e) {
			handleSecurityException(e);
		}
	}

	private void handleSecurityException(HTTPWrapperException e) throws GITRESTClientException {
		if(e.getStatusCode() == 401) {
			throw new GITRESTClientException("check your git token validity.", e);
		}else if(e.getStatusCode() == 403) {
			throw new GITRESTClientException("make sure your git user has admin priviledges.", e);
		}else {
			throw new GITRESTClientException(e);
		}
	}
	
	public List<String> retrieveAllGitGroupsPath(CloseableHttpClient httpClient) throws GITRESTClientException {
		return retrieveAllGitGroupsPath(httpClient, 100, 0, 0);
	}
		
	public List<String> retrieveAllGitGroupsPath(CloseableHttpClient httpClient, int nbPerPage, int limit, int offset) throws GITRESTClientException {
		List<Object> fpObjLst = httpGETParsedJsonDocumentPaginatedList(getGitUrlAllGroups(), ".full_path", nbPerPage, limit, offset, httpClient);
		List<String> result = new ArrayList<>();
		for (Object object : fpObjLst) {
			result.add((String)object);
		}
		return result;
	}


	public List<GitGroup> retrieveAllGitGroups(CloseableHttpClient httpClient, boolean withSubgroups, boolean withProjects) throws GITRESTClientException {
		List<String> allGroupPath = retrieveAllGitGroupsPath(httpClient);
		List<GitGroup> result = new ArrayList<>();
		for (String fullpath : allGroupPath) {
			result.add(retrieveGitGroup(fullpath, httpClient, withSubgroups, withProjects));
		}
		return result;
	}

	public GitGroup retrieveGitGroup(String gitGroupFullPath, CloseableHttpClient httpClient, boolean withSubGroups, boolean withProjects) throws GITRESTClientException {
		try {
			Object groupJsonObj = httpGETParsedJsonDocument(getGitUrlGroupById(gitGroupFullPath), httpClient);

			if(groupJsonObj == null) return null;
			GitGroup result = new GitGroup().fullPath(gitGroupFullPath)
					.id(((Map<?, ?>)groupJsonObj).get("id").toString());

			if(withProjects) {
				List<Object> projectsObj = JsonPath.read(groupJsonObj, "$.projects");
				for (Object jsonProj : projectsObj) {
					result.addProject(createGitProject(jsonProj, httpClient));
				}

				for (GitProject gitProject : result.getProjectList()) {
					logger.debug("gitProject {}", gitProject.getName());
					for (GitProjectMemberShip membership : gitProject.getMembershipList()) {
						logger.debug(" - git user {} has access level {}", membership.getMember().getName(), membership.getAccessLevel());
					}
				}
			}

			if(withSubGroups) result.setSubGroupList(retrieveGitSubGroups(gitGroupFullPath, withSubGroups, withProjects, httpClient));

			return result;
		}catch(IOException ex) {
			throw new GITRESTClientException(ex);
		}

	}

	public List<GitGroup> retrieveGitSubGroups(String gitGroupFullPath, boolean withSubGroups, boolean withProjects, CloseableHttpClient httpClient) throws GITRESTClientException {
		try {
			List<GitGroup> result = new ArrayList<>();

			/*
			 * no way to retrieve all group projects in a single request.
			 * https://gitlab.com/gitlab-org/gitlab-ce/issues/50325
			 * workaround : recursive loading.
			 */
			List<Object> subGroupsObjs = httpGETParsedJsonDocumentPaginatedList(getGitUrlGroupSubgroupsById(gitGroupFullPath), "$.*", 20, 0, 0, httpClient);
			if(subGroupsObjs.isEmpty())return result;

			for(Object sgObj : subGroupsObjs) {
				if(sgObj == null)
					continue;
				Map<?, ?> groupObj = (Map<?, ?>)sgObj;
				result.add(retrieveGitGroup(groupObj.get("full_path").toString(), httpClient, withSubGroups, withProjects));
			}
			return result;
		}catch(IOException ex) {
			throw new GITRESTClientException(ex);
		}

	}

	private GitProject createGitProject(Object jsonProj, CloseableHttpClient httpClient) throws GITRESTClientException, IOException {
		if(jsonProj == null) return null;
		List<Object> namespaceJsonObj = JsonPath.read(jsonProj, ".namespace");
		GitProject result = new GitProject();
		if(namespaceJsonObj != null && !namespaceJsonObj.isEmpty()) {
			Map<?, ?> namespaceMap = (Map<?, ?>)namespaceJsonObj.get(0);
			result
			.namespaceId(namespaceMap.get("id").toString())
			.isInGroup("group".equals(namespaceMap.get("kind").toString()));
		}

		result.fullPath(((Map<?, ?>)jsonProj).get("path_with_namespace").toString())
		.namespaceId(((Map<?, ?>)jsonProj).get("namespace").toString())
		.id(((Map<?, ?>)jsonProj).get("id").toString())
		.name(((Map<?, ?>)jsonProj).get("name").toString())
		.httpUrlToRepo(((Map<?, ?>)jsonProj).get("http_url_to_repo").toString())
		.webUrl(((Map<?, ?>)jsonProj).get("web_url").toString());

		result.setMembershipList(retrieveMembershipList(result.getId(), httpClient));

		return result;
	}

	private List<Object> httpGETParsedJsonDocumentPaginatedList(String url, String jsonPath, int nbPerPage, int limit, int offset, CloseableHttpClient httpClient) throws GITRESTClientException{
		String paginationParams;
		if(url.contains("?")) {
			paginationParams = "&per_page=%d&page=%d";
		}else {
			paginationParams = "?per_page=%d&page=%d";
		}

		final int maxPerPageGIT = 100;
		final int minPerPageGIT = 20;
		if(nbPerPage < minPerPageGIT) nbPerPage = minPerPageGIT;
		if(nbPerPage > maxPerPageGIT) nbPerPage = maxPerPageGIT;

		int itemIndex = 0;
		List<Object> result = new ArrayList<Object>();
		for (int pageIndex = 1; ; pageIndex++) {
			String paginationSuffixThisPage = String.format(paginationParams, nbPerPage, pageIndex);
			String urlThisPage = url + paginationSuffixThisPage;
			Object jsonLstObj = httpGETParsedJsonDocument(urlThisPage, httpClient);
			if(jsonLstObj == null) return result;
			List<Object> pageObjectLst = JsonPath.read(jsonLstObj, jsonPath);
			if(pageObjectLst == null) return result;

			for (Object object : pageObjectLst) {
				if(offset == 0 || itemIndex >= offset)
					result.add(object);

				if(limit > 0 && result.size() == limit) return result;

				itemIndex++;
			}
			if(limit > 0 && result.size() == limit) return result;


			if(pageObjectLst.size() < nbPerPage)
				return result;
		}
	}

	public List<GitProjectMemberShip> retrieveMembershipList(String projectId, CloseableHttpClient httpClient) throws GITRESTClientException, IOException {
		List<GitProjectMemberShip> result = new ArrayList<>();
		List<Object> projMembersObjList = httpGETParsedJsonDocumentPaginatedList(getGitUrlProjectMembersById(projectId), "$.*", 20, 0, 0, httpClient);
		for(Object projMember : projMembersObjList) {
			final String userId = ((Map<?, ?>)projMember).get("id").toString();
			GitUser gitUser;
			try {
				gitUser = gitUserCache.computeIfAbsent(userId, k -> {
					try {
						return retrieveUser(k, httpClient);
					} catch (IOException | GITRESTClientException e) {
						throw new GITRESTClientRuntimeException(e);
					}
				});
			}catch(GITRESTClientRuntimeException e) {
				throw new GITRESTClientException(e);
			}

			AccessLevel accessLvl = AccessLevel.parse((Integer)((Map<?, ?>)projMember).get("access_level"));

			if(accessLvl == null)
				throw new GITRESTClientException("accessLevel cannot be null");
			GitProjectMemberShip memberShip = new GitProjectMemberShip()
					.member(gitUser)
					.accessLevel(accessLvl);
			if(!result.contains(memberShip))result.add(memberShip);
		}
		return result;
	}

	private GitUser retrieveUser(String userId, CloseableHttpClient httpClient) throws IOException, GITRESTClientException {
		String url = getGitUrlUserById(userId);
		Object userObj = httpGETParsedJsonDocument(url, httpClient);
		String userName = ((Map<?,?>)userObj).get("username").toString();
		Object mailVal = ((Map<?,?>)userObj).get("public_email");
		String email = mailVal == null ? null : mailVal.toString();
		if(email == null || email.isEmpty())
			logger.warn("public_email was empty for user {} on {}", userName, url);
		return new GitUser()
				.id(userId)
				.name(userName)
				.email(email);
	}

	public int getNbGitUser() {
		return gitUserCache.size();
	}

	public CloseableHttpResponse createGitGroup(String groupId, String groupPath, CloseableHttpClient httpClient) throws GITRESTClientException {
		try{
			return httpPOST(getGitUrlCreateGroup(groupId, groupPath), httpClient);
		}catch(Exception ex) {
			throw new GITRESTClientException(ex);
		}
	}

	public void deleteGitGroup(String groupId, CloseableHttpClient httpClient) throws GITRESTClientException {
		try{
			httpDelete(getGitUrlDeleteGroup(groupId), httpClient);
		}catch(IOException ex) {
			throw new GITRESTClientException(ex);
		}
	}

	public void deleteGitProject(String projectPath, CloseableHttpClient httpClient) throws GITRESTClientException {
		try{
			httpDelete(getGitUrlDeleteProject(projectPath), httpClient);
		}catch(IOException ex) {
			throw new GITRESTClientException(ex);
		}
	}

	public CloseableHttpResponse createGitProject(String projectName, String projectPath, String namespaceId, CloseableHttpClient httpClient) throws GITRESTClientException {
		try{
			return httpPOST(getGitUrlCreateProject(projectName, projectPath, namespaceId), httpClient);
		}catch(Exception ex) {
			throw new GITRESTClientException(ex);
		}
	}

	public GitProject retrieveGitProject(String projectFullPath, CloseableHttpClient httpClient) throws GITRESTClientException {
		try{
			Object jsonObj = httpGETParsedJsonDocument(getGitUrlProjectById(URLEncoder.encode(projectFullPath, Consts.UTF_8.name())), httpClient);
			GitProject result = createGitProject(jsonObj, httpClient);
			return result;
		}catch(IOException ex) {
			throw new GITRESTClientException(ex);
		}
	}

	public GitUser retrieveUserCurrent(CloseableHttpClient httpClient) throws GITRESTClientException {
		Object userObj = httpGETParsedJsonDocument(getGitUrlUserCurrent(), httpClient);
		String userName = ((Map<?,?>)userObj).get("username").toString();
		String userId = ((Map<?,?>)userObj).get("id").toString();
		String email = ((Map<?,?>)userObj).get("public_email").toString();
		return new GitUser()
				.id(userId)
				.name(userName)
				.email(email);
	}

	public GitCommit retrieveGitCommit(String projectId, String commitId, CloseableHttpClient httpClient) throws GITRESTClientException {
		Object commitObj = httpGETParsedJsonDocument(getGitUrlCommit(projectId, commitId), httpClient);
		String asStr = null;
		try {
			asStr = objectMapper.writeValueAsString(commitObj);
			return objectMapper.readValue(asStr, GitCommit.class);
		} catch (IOException ex) {
			throw new GITRESTClientException("value as string : " + asStr, ex);
		}
	}

	public List<GitCommit> retrieveGitCommits(String projectId, int limit, int offset, CloseableHttpClient httpClient) throws GITRESTClientException {
		List<Object> commistObj = httpGETParsedJsonDocumentPaginatedList(getGitUrlCommits(projectId), "$.[*]", limit, limit, offset, httpClient);
		List<GitCommit> result = new ArrayList<>();
		try {
			
			for (Object commitJson : commistObj) {
				result.add(objectMapper.readValue(objectMapper.writeValueAsString(commitJson), GitCommit.class));
			}
			
			return result;
		} catch (IOException ex) {
			throw new GITRESTClientException(ex);
		}
		
	}
}
