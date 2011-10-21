package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("filterOptionHome")
public class FilterOptionHome extends EntityHome<FilterOption>
{

	public void setFilterOptionFilterOptionId(Integer id)
	{
		setId(id);
	}

	public Integer getFilterOptionFilterOptionId()
	{
		return (Integer) getId();
	}

	@Override
	protected FilterOption createInstance()
	{
		FilterOption filterOption = new FilterOption();
		return filterOption;
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

	public FilterOption getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
