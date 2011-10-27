package com.redhat.topicindex.rest.collections;

import java.util.List;

import com.redhat.topicindex.rest.ExpandData;

public abstract class BaseRestCollectionV1<T, U>
{
	private int size = 0;
	private String expand = null;
	private T[] items = null;

	public int getSize()
	{
		return size;
	}

	public void setSize(int size)
	{
		this.size = size;
	}

	public String getExpand()
	{
		return expand;
	}

	public void setExpand(String expand)
	{
		this.expand = expand;
	}

	public T[] getItems()
	{
		return items;
	}

	public void setItems(T[] items)
	{
		this.items = items;
	}
	
	public void initialize(final List<U> entities)
	{
		initialize(entities, null, null);
	}
	
	public abstract void initialize(final List<U> entities, final ExpandData expand, final String baseUrl);
}
