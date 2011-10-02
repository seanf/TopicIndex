package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

/** A generated Seam EntityHome object for the FilterTag table */
@Name("filterTagHome")
public class FilterTagHome extends EntityHome<FilterTag>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -6519004005099769126L;
	/** The filter that this tag belongs to */
	@In(create = true)
	private FilterHome filterHome;

	/**
	 * Sets the FilterTag ID
	 * @param id The FilterTag ID
	 */
	public void setFilterTagFilterTagId(final Integer id)
	{
		setId(id);
	}

	/**
	 * @return The FilterTag ID
	 */
	public Integer getFilterTagFilterTagId()
	{
		return (Integer) getId();
	}

	@Override
	protected FilterTag createInstance()
	{
		FilterTag filterTag = new FilterTag();
		return filterTag;
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
		Filter filter = filterHome.getDefinedInstance();
		if (filter != null)
		{
			getInstance().setFilter(filter);
		}
	}

	public boolean isWired()
	{
		if (getInstance().getFilter() == null)
			return false;
		return true;
	}

	public FilterTag getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
