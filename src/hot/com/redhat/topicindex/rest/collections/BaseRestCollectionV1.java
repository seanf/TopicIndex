package com.redhat.topicindex.rest.collections;

import java.util.List;

public class BaseRestCollectionV1<T>
{
	private Integer size = 0;
	private String expand = null;
	private Integer startExpandIndex = null;
	private Integer endExpandIndex = null;
	private List<T> items = null;
	
	public Integer getSize()
	{
		return size;
	}

	public void setSize(final Integer size)
	{
		this.size = size;
	}

	public String getExpand()
	{
		return expand;
	}

	public void setExpand(final String expand)
	{
		this.expand = expand;
	}

	public List<T> getItems()
	{
		return items;
	}

	public void setItems(final List<T> items)
	{
		this.items = items;
	}

	public Integer getStartExpandIndex()
	{
		return startExpandIndex;
	}

	public void setStartExpandIndex(final Integer startExpandIndex)
	{
		this.startExpandIndex = startExpandIndex;
	}

	public Integer getEndExpandIndex()
	{
		return endExpandIndex;
	}

	public void setEndExpandIndex(final Integer endExpandIndex)
	{
		this.endExpandIndex = endExpandIndex;
	}
}
