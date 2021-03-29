package org.toolup.devops.ci.git.gitlab.apiclient;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class GitGroup {

	private List<GitGroup> subGroupList = new ArrayList<>();
	private List<GitProject> projectList = new ArrayList<>();

	private String fullPath;
	private String id;

	public String getFullPath() {
		return fullPath;
	}

	public GitGroup id(String id) {
		this.id = id;
		return this;
	}

	public GitGroup fullPath(String fullPath) {
		this.fullPath = fullPath;
		return this;
	}
	
	public String getId() {
		return id;
	}

	public GitProject addProject(GitProject project) {
		projectList.add(project);
		return project;
	}

	public GitGroup addGroup(GitGroup group) {
		subGroupList.add(group);
		return group;
	}
	
	public GitGroup setProjectList(List<GitProject> projectLst) {
		projectList.clear();
		projectList.addAll(projectLst);
		return this;
	}

	@JsonIgnore
	public int getNbProject() {
		return projectList.size();
	}

	@JsonIgnore
	public int getNbGroup() {
		return subGroupList.size();
	}
	
	@JsonIgnore
	public int getNbMembershipRecur() {
		int result = 0;
		for (GitProject proj : projectList) {
			result += proj.getNbMembership();
		}
		
		for (GitGroup gitGrp : subGroupList) {
			result += gitGrp.getNbMembershipRecur();	
		}
		return result;
	}

	@JsonIgnore
	public int getNbProjectRecur() {
		int result = projectList.size();
		for (GitGroup gitGrp : subGroupList) {
			result += gitGrp.getNbProjectRecur();	
		}
		return result;
	}

	@JsonIgnore
	public int getNbGroupsRecur() {
		int result = subGroupList.size();
		for (GitGroup gitGrp : subGroupList) {
			result += gitGrp.getNbGroupsRecur();	
		}
		return result;
	}
	
	public List<GitProject> getProjectList() {
		return new ArrayList<>(projectList);
	}

	public GitGroup setSubGroupList(List<GitGroup> subGroupList) {
		this.subGroupList.clear();
		this.subGroupList.addAll(subGroupList);
		return this;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	
	public List<GitGroup> getSubGroupList() {
		return subGroupList;
	}

	@Override
	public String toString() {
		return "GitGroup [subGroupList=" + subGroupList + "projectList=" + projectList + ", name=" + fullPath + "]";
	}

	public List<GitGroup> getGroups() {
		return new ArrayList<>(subGroupList);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GitGroup other = (GitGroup) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		return true;
	}
	
	
	
}
