package com.redhat.topicindex.rest;

public class BaseRestV1
{	
	private String selfLink = null;
	private String editLink = null;
	private String deleteLink = null;
	private String addLink = null;
	
	protected void initialize(final String baseUrl, final String restBasePath, final Object id)
	{
		this.setSelfLink(baseUrl + "/" + restBasePath + "/get/" + id);
		this.setDeleteLink(baseUrl + "/" + restBasePath + "/delete/" + id);
		this.setAddLink(baseUrl + "/" + restBasePath + "/post/" + id);
		this.setEditLink(baseUrl + "/" + restBasePath + "/put/" + id);
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

	protected String getAddLink()
	{
		return addLink;
	}

	protected void setAddLink(final String addLink)
	{
		this.addLink = addLink;
	}
}
