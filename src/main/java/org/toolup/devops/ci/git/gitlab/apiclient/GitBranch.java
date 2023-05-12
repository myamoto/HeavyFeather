package org.toolup.devops.ci.git.gitlab.apiclient;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GitBranch {

	private String name;
	private boolean merged;

	@JsonProperty(value = "protected")
	private boolean _protected;
	@JsonProperty(value = "default")
	private boolean _default;
	private boolean developers_can_push;
	private boolean developers_can_merge;
	private boolean can_push;
	private String web_url;
	private GitCommit commit;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isMerged() {
		return merged;
	}
	public void setMerged(boolean merged) {
		this.merged = merged;
	}

	@JsonProperty(value = "protected")
	public boolean is_protected() {
		return _protected;
	}
	@JsonProperty(value = "protected")
	public void set_protected(boolean _protected) {
		this._protected = _protected;
	}
	@JsonProperty(value = "default")
	public boolean is_default() {
		return _default;
	}
	@JsonProperty(value = "default")
	public void set_default(boolean _default) {
		this._default = _default;
	}
	public boolean isDevelopers_can_push() {
		return developers_can_push;
	}
	public void setDevelopers_can_push(boolean developers_can_push) {
		this.developers_can_push = developers_can_push;
	}
	public boolean isDevelopers_can_merge() {
		return developers_can_merge;
	}
	public void setDevelopers_can_merge(boolean developers_can_merge) {
		this.developers_can_merge = developers_can_merge;
	}
	public boolean isCan_push() {
		return can_push;
	}
	public void setCan_push(boolean can_push) {
		this.can_push = can_push;
	}
	public String getWeb_url() {
		return web_url;
	}
	public void setWeb_url(String web_url) {
		this.web_url = web_url;
	}
	public GitCommit getCommit() {
		return commit;
	}
	public void setCommit(GitCommit commit) {
		this.commit = commit;
	}
	@Override
	public String toString() {
		return "GitBranch [name=" + name + ", merged=" + merged + ", _protected=" + _protected + ", _default="
				+ _default + ", developers_can_push=" + developers_can_push + ", developers_can_merge="
				+ developers_can_merge + ", can_push=" + can_push + ", web_url=" + web_url + ", commit=" + commit + "]";
	}


}
