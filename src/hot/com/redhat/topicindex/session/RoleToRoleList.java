package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("roleToRoleList")
public class RoleToRoleList extends EntityQuery<RoleToRole> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -4947370615704235700L;

	private static final String EJBQL = "select roleToRole from RoleToRole roleToRole";

	private static final String[] RESTRICTIONS = {};

	private RoleToRole roleToRole = new RoleToRole();

	public RoleToRoleList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public RoleToRole getRoleToRole() {
		return roleToRole;
	}
}
