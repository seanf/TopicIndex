package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("roleToRoleRelationshipList")
public class RoleToRoleRelationshipList extends
		EntityQuery<RoleToRoleRelationship> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -5009295690110515794L;

	private static final String EJBQL = "select roleToRoleRelationship from RoleToRoleRelationship roleToRoleRelationship";

	private static final String[] RESTRICTIONS = { "lower(roleToRoleRelationship.description) like lower(concat(#{roleToRoleRelationshipList.roleToRoleRelationship.description},'%'))", };

	private RoleToRoleRelationship roleToRoleRelationship = new RoleToRoleRelationship();

	public RoleToRoleRelationshipList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public RoleToRoleRelationship getRoleToRoleRelationship() {
		return roleToRoleRelationship;
	}
}
