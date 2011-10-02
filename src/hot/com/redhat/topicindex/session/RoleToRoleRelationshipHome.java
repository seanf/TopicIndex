package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("roleToRoleRelationshipHome")
public class RoleToRoleRelationshipHome extends
		EntityHome<RoleToRoleRelationship> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 6288265702603501765L;

	public void setRoleToRoleRelationshipRoleToRoleRelationshipId(Integer id) {
		setId(id);
	}

	public Integer getRoleToRoleRelationshipRoleToRoleRelationshipId() {
		return (Integer) getId();
	}

	@Override
	protected RoleToRoleRelationship createInstance() {
		RoleToRoleRelationship roleToRoleRelationship = new RoleToRoleRelationship();
		return roleToRoleRelationship;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public RoleToRoleRelationship getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<RoleToRole> getRoleToRoles() {
		return getInstance() == null ? null : new ArrayList<RoleToRole>(
				getInstance().getRoleToRoles());
	}

}
