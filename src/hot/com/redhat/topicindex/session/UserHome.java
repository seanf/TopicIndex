package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("userHome")
public class UserHome extends EntityHome<User>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4678394145285598496L;

	public void setUserUserId(Integer id)
	{
		setId(id);
	}

	public Integer getUserUserId()
	{
		return (Integer) getId();
	}

	@Override
	protected User createInstance()
	{
		User user = new User();
		return user;
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
	}

	public boolean isWired()
	{
		return true;
	}

	public User getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public List<UserRole> getUserRoles()
	{
		return getInstance() == null ? null : new ArrayList<UserRole>(getInstance().getUserRoles());
	}

}
