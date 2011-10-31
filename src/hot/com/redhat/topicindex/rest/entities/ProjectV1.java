package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class ProjectV1 extends BaseRestV1
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private BaseRestCollectionV1<TagV1> tags = null;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public BaseRestCollectionV1<TagV1> getTags()
	{
		return tags;
	}

	public void setTags(BaseRestCollectionV1<TagV1> tags)
	{
		this.tags = tags;
	}

}
