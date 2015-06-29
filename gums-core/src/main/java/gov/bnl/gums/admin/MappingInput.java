
package gov.bnl.gums.admin;

import java.util.Objects;

import gov.bnl.gums.GridUser;

public class MappingInput {

	private GridUser user;
	private String hostname;
	private String userDN;
	private String fqan;
	private boolean verified;

        public MappingInput(GridUser user, String hostname, String userDN, String fqan, boolean verified)
	{
		this.user = user;
		this.hostname = hostname;
		this.userDN = userDN;
		this.fqan = fqan;
		this.verified = verified;
	}

	public int hashCode()
	{
		return Objects.hash(user, hostname, userDN, fqan);
	}

	public boolean equals(Object o)
	{
		if (!(o instanceof MappingInput)) {return false;}
		MappingInput m = (MappingInput)o;
		return Objects.equals(this.user, m.getAuthUser()) &&
			Objects.equals(this.hostname, m.getHostname()) &&
			Objects.equals(this.userDN, m.getUserDN()) &&
			Objects.equals(this.fqan, m.getFQAN()) &&
			(this.verified == m.getVerified());
	}

	public GridUser getAuthUser() {return user;}
	public String getHostname() {return hostname;}
	public String getUserDN() {return userDN;}
	public String getFQAN() {return fqan;}
	public boolean getVerified() {return verified;}
}
