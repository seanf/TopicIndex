package com.redhat.topicindex.rest.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public abstract class BaseRESTEntityV1
{
	/**
	 * Maintains a list of the database fields that have been specifically set
	 * on this object. This allows us to distinguish them from those that are
	 * just null by default
	 */
	private List<String> configuredParameters = null;
	private String selfLink = null;
	private String editLink = null;
	private String deleteLink = null;
	private String addLink = null;
	private String[] expand = null;
	private Boolean addItem = null;
	private Boolean removeItem = null;

	protected void setParamaterToConfigured(final String paramater)
	{
		if (configuredParameters == null)
			configuredParameters = new ArrayList<String>();
		if (!configuredParameters.contains(paramater))
			configuredParameters.add(paramater);
	}
	
	public boolean isParameterSet(final String parameter)
	{
		return configuredParameters != null && configuredParameters.contains(parameter);
	}

	public void setLinks(final String baseUrl, final String restBasePath, final String dataType, final Object id)
	{
		this.setSelfLink(baseUrl + "/1/" + restBasePath + "/get/" + dataType + "/" + id);
		this.setDeleteLink(baseUrl + "/1/" + restBasePath + "/delete/" + dataType + "/" + id);
		this.setAddLink(baseUrl + "/1/" + restBasePath + "/post/" + dataType + "/" + id);
		this.setEditLink(baseUrl + "/1/" + restBasePath + "/put/" + dataType + "/" + id);
	}

	@XmlElement
	public String getSelfLink()
	{
		return selfLink;
	}

	public void setSelfLink(final String selfLink)
	{
		this.selfLink = selfLink;
	}

	@XmlElement
	public String getEditLink()
	{
		return editLink;
	}

	public void setEditLink(final String editLink)
	{
		this.editLink = editLink;
	}

	@XmlElement
	public String getDeleteLink()
	{
		return deleteLink;
	}

	public void setDeleteLink(final String deleteLink)
	{
		this.deleteLink = deleteLink;
	}

	@XmlElement
	public String getAddLink()
	{
		return addLink;
	}

	public void setAddLink(final String addLink)
	{
		this.addLink = addLink;
	}

	@XmlElement
	public String[] getExpand()
	{
		return expand;
	}

	public void setExpand(final String[] expand)
	{
		this.expand = expand;
	}

	public Boolean getAddItem()
	{
		return addItem;
	}

	public void setAddItem(Boolean addItem)
	{
		this.addItem = addItem;
	}

	public Boolean getRemoveItem()
	{
		return removeItem;
	}

	public void setRemoveItem(Boolean removeItem)
	{
		this.removeItem = removeItem;
	}
}
