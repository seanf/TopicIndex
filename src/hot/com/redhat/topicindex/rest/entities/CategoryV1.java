package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class CategoryV1 extends BaseRESTEntityV1
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private boolean mutuallyExclusive = false;
	private Integer sort = null;
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

	public boolean isMutuallyExclusive()
	{
		return mutuallyExclusive;
	}

	public void setMutuallyExclusive(boolean mutuallyExclusive)
	{
		this.mutuallyExclusive = mutuallyExclusive;
	}

	public Integer getSort()
	{
		return sort;
	}

	public void setSort(Integer sort)
	{
		this.sort = sort;
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
