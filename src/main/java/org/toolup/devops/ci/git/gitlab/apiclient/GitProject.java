package org.toolup.devops.ci.git.gitlab.apiclient;

import java.util.ArrayList;
import java.util.List;

public class GitProject {

	private String name;
	private String id;
	private String pathWithNamespace;
	private String httpUrlToRepo;
	private String webUrl;
	private String fullpath;
	
	private boolean isInGroup;
	private String namespaceId;
	
	private List<GitProjectMemberShip> membershipList = new ArrayList<>();

	public String getWebUrl() {
		return webUrl;
	}
	
	public boolean isInGroup() {
		return isInGroup;
	}

	public GitProject isInGroup(boolean isInGroup) {
		this.isInGroup = isInGroup;
		return this;
	}
	
	
	public String getFullpath() {
		return fullpath;
	}

	public GitProject fullPath(String fullpath) {
		this.fullpath = fullpath;
		return this;
	}
	
	
	public String getNamespaceId() {
		return namespaceId;
	}

	public GitProject namespaceId(String namespaceId) {
		this.namespaceId = namespaceId;
		return this;
	}

	public GitProject webUrl(String webUrl) {
		this.webUrl = webUrl;
		return this;
	}
	
	public String getHttpUrlToRepo() {
		return httpUrlToRepo;
	}

	public GitProject httpUrlToRepo(String httpUrlToRepo) {
		this.httpUrlToRepo = httpUrlToRepo;
		return this;
	}

	public String getName() {
		return name;
	}

	public GitProject name(String name) {
		this.name = name;
		return this;
	}

	public GitProject id(String id) {
		this.id = id;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public List<GitProjectMemberShip> getMembershipList() {
		return new ArrayList<>(membershipList);
	}

	public GitProject setMembershipList(List<GitProjectMemberShip> membershipList) {
		this.membershipList.clear();
		this.membershipList.addAll(membershipList);
		return this;
	}

	public int getNbMembership() {
		return membershipList.size();
	}
	

	@Override
	public String toString() {
		return "GitProject [name=" + name + ", id=" + id + ", pathWithNamespace=" + pathWithNamespace
				+ ", membershipList=" + membershipList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		GitProject other = (GitProject) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	
	
}
