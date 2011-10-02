package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("roleToRoleHome")
public class RoleToRoleHome extends EntityHome<RoleToRole> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -5090422737619334320L;
	@In(create = true)
	RoleToRoleRelationshipHome roleToRoleRelationshipHome;

	public void setRoleToRoleRoleToRoleId(Integer id) {
		setId(id);
	}

	public Integer getRoleToRoleRoleToRoleId() {
		return (Integer) getId();
	}

	@Override
	protected RoleToRole createInstance() {
		RoleToRole roleToRole = new RoleToRole();
		return roleToRole;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		RoleToRoleRelationship roleToRoleRelationship = roleToRoleRelationshipHome
				.getDefinedInstance();
		if (roleToRoleRelationship != null) {
			getInstance().setRoleToRoleRelationship(roleToRoleRelationship);
		}
	}

	public boolean isWired() {
		if (getInstance().getRoleToRoleRelationship() == null)
			return false;
		return true;
	}

	public RoleToRole getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
