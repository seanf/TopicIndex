package com.redhat.topicindex.utils.structures.tags;

import com.redhat.ecs.commonutils.ExceptionUtilities;

/**
 * This class defines the common properties to tags and categories.
 */
public class UITagDataBase
{
	/** The name */
	protected String name = "";
	/** The id (primary key) */
	protected Integer id = null;
	/** The description */
	protected String description = "";
	/** The sorting order */
	protected Integer sort;
	/** Whether or not this object has been selected */
	protected boolean selected = false;
	/** Whether or not this object has been "not selected" */
	protected boolean notSelected = false;

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(final boolean selected)
	{
		this.selected = selected;
	}

	public boolean isNotSelected()
	{
		return notSelected;
	}

	public void setNotSelected(final boolean notSelected)
	{
		this.notSelected = notSelected;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getNameWithoutSpaces()
	{
		if (name == null)
			return null;
		return name.replaceAll(" ", "");
	}

	public Integer getId()
	{
		return id;
	}

	public void setId(final Integer id)
	{
		this.id = id;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public Integer getSort()
	{
		return sort;
	}

	public void setSort(final Integer sort)
	{
		this.sort = sort;
	}
	
	public String getSortString()
	{
		return this.sort == null ? null : this.sort.toString();
	}
	
	public void setSortString(final String sort)
	{
		try
		{
			this.sort = Integer.parseInt(sort);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	public UITagDataBase(final String name, final String description, final Integer id, final Integer sort, final boolean selected, final boolean notSelected)
	{
		this.name = name;
		this.id = id;
		this.description = description;
		this.sort = sort;
		this.selected = selected;
		this.notSelected = notSelected;
	}
}
