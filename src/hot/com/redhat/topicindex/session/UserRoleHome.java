package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("userRoleHome")
public class UserRoleHome extends EntityHome<UserRole>
{

	/** Serializable version identifier */
	private static final long serialVersionUID = -1204373913303804966L;
	@In(create = true)
	UserHome userHome;
	@In(create = true)
	RoleHome roleHome;

	public void setUserRoleUserRoleId(Integer id)
	{
		setId(id);
	}

	public Integer getUserRoleUserRoleId()
	{
		return (Integer) getId();
	}

	@Override
	protected UserRole createInstance()
	{
		UserRole userRole = new UserRole();
		return userRole;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
		User user = userHome.getDefinedInstance();
		if (user != null)
		{
			getInstance().setUser(user);
		}
		Role role = roleHome.getDefinedInstance();
		if (role != null)
		{
			getInstance().setRole(role);
		}
	}

	public boolean isWired()
	{
		if (getInstance().getUser() == null)
			return false;
		if (getInstance().getRole() == null)
			return false;
		return true;
	}

	public UserRole getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
