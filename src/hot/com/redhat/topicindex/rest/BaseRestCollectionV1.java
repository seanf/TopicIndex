package com.redhat.topicindex.rest;

public class BaseRestCollectionV1<T>
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

}
