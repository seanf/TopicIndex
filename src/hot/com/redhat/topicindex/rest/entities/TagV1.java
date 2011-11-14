package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class TagV1 extends BaseRESTEntityV1
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private BaseRestCollectionV1<CategoryV1> categories = new BaseRestCollectionV1<CategoryV1>();

	public Integer getId()
	{
		return id;
	}

	public void setId(final Integer id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(final String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
	}

	public BaseRestCollectionV1<CategoryV1> getCategories()
	{
		return categories;
	}

	public void setCategories(final BaseRestCollectionV1<CategoryV1> categories)
	{
		this.categories = categories;
	}

}
