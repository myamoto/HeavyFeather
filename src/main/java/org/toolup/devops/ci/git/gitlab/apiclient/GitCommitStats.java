package org.toolup.devops.ci.git.gitlab.apiclient;

public class GitCommitStats {
	private float additions;
	private float deletions;
	private float total;


	// Getter Methods 

	public float getAdditions() {
		return additions;
	}

	public float getDeletions() {
		return deletions;
	}

	public float getTotal() {
		return total;
	}

	// Setter Methods 

	public void setAdditions(float additions) {
		this.additions = additions;
	}

	public void setDeletions(float deletions) {
		this.deletions = deletions;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(additions);
		result = prime * result + Float.floatToIntBits(deletions);
		result = prime * result + Float.floatToIntBits(total);
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
		GitCommitStats other = (GitCommitStats) obj;
		if (Float.floatToIntBits(additions) != Float.floatToIntBits(other.additions))
			return false;
		if (Float.floatToIntBits(deletions) != Float.floatToIntBits(other.deletions))
			return false;
		if (Float.floatToIntBits(total) != Float.floatToIntBits(other.total))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GitCommitStats [additions=" + additions + ", deletions=" + deletions + ", total=" + total + "]";
	}
	
	
}
