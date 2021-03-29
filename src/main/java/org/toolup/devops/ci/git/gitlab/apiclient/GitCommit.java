package org.toolup.devops.ci.git.gitlab.apiclient;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GitCommit {
	private String id;
	private String short_id;
	private String created_at;
	private final List<String> parent_ids;
	private String title;
	private String message;
	private String author_name;
	private String author_email;
	private String authored_date;
	private String committer_name;
	private String committer_email;
	private String committed_date;
	private GitCommitStats StatsObject;
	private String status = null;
	
	
	private float project_id;
	private String web_url;

	public GitCommit() {
		parent_ids = new ArrayList<String>();
	}

	// Getter Methods 

	public String getId() {
		return id;
	}

	public String getShort_id() {
		return short_id;
	}

	public String getCreated_at() {
		return created_at;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public String getAuthor_name() {
		return author_name;
	}

	public String getAuthor_email() {
		return author_email;
	}

	public String getAuthored_date() {
		return authored_date;
	}

	public String getCommitter_name() {
		return committer_name;
	}

	public String getCommitter_email() {
		return committer_email;
	}

	public String getCommitted_date() {
		return committed_date;
	}

	public GitCommitStats getStats() {
		return StatsObject;
	}

	public String getStatus() {
		return status;
	}


	public float getProject_id() {
		return project_id;
	}

	// Setter Methods 

	public void setId(String id) {
		this.id = id;
	}

	public void setShort_id(String short_id) {
		this.short_id = short_id;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}

	public void setAuthor_email(String author_email) {
		this.author_email = author_email;
	}

	public void setAuthored_date(String authored_date) {
		this.authored_date = authored_date;
	}

	public void setCommitter_name(String committer_name) {
		this.committer_name = committer_name;
	}

	public void setCommitter_email(String committer_email) {
		this.committer_email = committer_email;
	}

	public void setCommitted_date(String committed_date) {
		this.committed_date = committed_date;
	}

	public void setStats(GitCommitStats statsObject) {
		this.StatsObject = statsObject;
	}

	public void setStatus(String status) {
		this.status = status;
	}


	public void setProject_id(float project_id) {
		this.project_id = project_id;
	}

	public GitCommitStats getStatsObject() {
		return StatsObject;
	}

	public void setStatsObject(GitCommitStats statsObject) {
		StatsObject = statsObject;
	}

	public List<String> getParent_ids() {
		return parent_ids;
	}
	
	public String getWeb_url() {
		return web_url;
	}

	public void setWeb_url(String web_url) {
		this.web_url = web_url;
	}

	public Date authored_date() throws GITRESTClientException  {
		try {
			return authored_date != null ? Date.from(OffsetDateTime.parse(authored_date).toInstant()) : null;
		}catch(DateTimeParseException ex) {
			throw new GITRESTClientException(ex);
		}
	}


	@Override
	public String toString() {
		return "GitCommit [id=" + id + ", short_id=" + short_id + ", created_at=" + created_at + ", parent_ids="
				+ parent_ids + ", title=" + title + ", message=" + message + ", author_name=" + author_name
				+ ", author_email=" + author_email + ", authored_date=" + authored_date + ", committer_name="
				+ committer_name + ", committer_email=" + committer_email + ", committed_date=" + committed_date
				+ ", StatsObject=" + StatsObject + ", status=" + status + ", project_id=" + project_id + ", web_url="
				+ web_url + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		GitCommit other = (GitCommit) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}


