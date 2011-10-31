package com.redhat.topicindex.rest.entities;

public abstract class BaseRestV1
{	
	private String selfLink = null;
	private String editLink = null;
	private String deleteLink = null;
	private String addLink = null;
	private String[] expand = null;
	
	public void setLinks(final String baseUrl, final String restBasePath, final String dataType, final Object id)
	{
		this.setSelfLink(baseUrl + "/1/" + restBasePath + "/get/" + dataType + "/" + id);
		this.setDeleteLink(baseUrl + "/1/" + restBasePath + "/delete/" + dataType + "/" + id);
		this.setAddLink(baseUrl + "/1/" + restBasePath + "/post/" + dataType + "/" + id);
		this.setEditLink(baseUrl + "/1/" + restBasePath + "/put/" + dataType + "/" + id);
	}
	
	public String getSelfLink()
	{
		return selfLink;
	}

	public void setSelfLink(final String selfLink)
	{
		this.selfLink = selfLink;
	}

	public String getEditLink()
	{
		return editLink;
	}

	public void setEditLink(final String editLink)
	{
		this.editLink = editLink;
	}

	public String getDeleteLink()
	{
		return deleteLink;
	}

	public void setDeleteLink(final String deleteLink)
	{
		this.deleteLink = deleteLink;
	}

	public String getAddLink()
	{
		return addLink;
	}

	public void setAddLink(final String addLink)
	{
		this.addLink = addLink;
	}

	public String[] getExpand()
	{
		return expand;
	}

	public void setExpand(final String[] expand)
	{
		this.expand = expand;
	}
}
