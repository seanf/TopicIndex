package com.redhat.topicindex.rest.collections;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class BaseRestCollectionV1<T>
{
	private Integer size = 0;
	private String expand = null;
	private Integer startExpandIndex = null;
	private Integer endExpandIndex = null;
	private List<T> items = null;
	
	@XmlElement
	public Integer getSize()
	{
		return size;
	}

	public void setSize(final Integer size)
	{
		this.size = size;
	}

	@XmlElement
	public String getExpand()
	{
		return expand;
	}

	public void setExpand(final String expand)
	{
		this.expand = expand;
	}

	@XmlElement
	public List<T> getItems()
	{
		return items;
	}

	public void setItems(final List<T> items)
	{
		this.items = items;
	}

	@XmlElement
	public Integer getStartExpandIndex()
	{
		return startExpandIndex;
	}

	public void setStartExpandIndex(final Integer startExpandIndex)
	{
		this.startExpandIndex = startExpandIndex;
	}

	@XmlElement
	public Integer getEndExpandIndex()
	{
		return endExpandIndex;
	}

	public void setEndExpandIndex(final Integer endExpandIndex)
	{
		this.endExpandIndex = endExpandIndex;
	}
}
