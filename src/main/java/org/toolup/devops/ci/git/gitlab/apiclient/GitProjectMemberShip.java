package org.toolup.devops.ci.git.gitlab.apiclient;

public class GitProjectMemberShip {
	
	public enum AccessLevel{OWNER, DEVELOPER, MAINTAINER, GUEST, REPORTER;
		public static AccessLevel parse(int accessLevelInt) throws GITRESTClientException {
			switch(accessLevelInt) {
			case 10 :
				return AccessLevel.GUEST;
			case 20 :
				return AccessLevel.REPORTER;
			case 30 :
				return AccessLevel.DEVELOPER;
			case 40 :
				return AccessLevel.MAINTAINER;
			case 50 :
				return AccessLevel.OWNER;
			default :
				throw new GITRESTClientException(String.format("unknown accessLevel %d.", accessLevelInt));
			}
		}
	}

	private GitUser member;
	private AccessLevel accessLevel;
	
	public GitProjectMemberShip member(GitUser member) {
		this.member = member;
		return this;
	}
	
	public GitProjectMemberShip accessLevel(AccessLevel accessLevel) {
		this.accessLevel = accessLevel;
		return this;
	}

	public GitUser getMember() {
		return member;
	}

	public AccessLevel getAccessLevel() {
		return accessLevel;
	}
	

	@Override
	public String toString() {
		return "GitProjectMemberShip [member=" + member + ", accessLevel=" + accessLevel + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accessLevel == null) ? 0 : accessLevel.hashCode());
		result = prime * result + ((member == null) ? 0 : member.hashCode());
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
		GitProjectMemberShip other = (GitProjectMemberShip) obj;
		if (accessLevel != other.accessLevel)
			return false;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		return true;
	}
	
	
}
