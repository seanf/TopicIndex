package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("roleHome")
public class RoleHome extends EntityHome<Role> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 6808946968809326435L;

	public void setRoleRoleId(Integer id) {
		setId(id);
	}

	public Integer getRoleRoleId() {
		return (Integer) getId();
	}

	@Override
	protected Role createInstance() {
		Role role = new Role();
		return role;
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

	public Role getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<UserRole> getUserRoles() {
		return getInstance() == null ? null : new ArrayList<UserRole>(
				getInstance().getUserRoles());
	}

}
