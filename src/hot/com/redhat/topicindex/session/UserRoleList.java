package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("userRoleList")
public class UserRoleList extends EntityQuery<UserRole>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 3058814831126417538L;

	private static final String EJBQL = "select userRole from UserRole userRole";

	private static final String[] RESTRICTIONS = {};

	private UserRole userRole = new UserRole();

	public UserRoleList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public UserRole getUserRole()
	{
		return userRole;
	}
}
