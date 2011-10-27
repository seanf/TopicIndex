package com.redhat.topicindex.rest;

public abstract class BaseRestV1<T>
{	
	private String selfLink = null;
	private String editLink = null;
	private String deleteLink = null;
	private String addLink = null;
	private String[] expand = null;
	
	public void initialize(final T entity, final String baseUrl, final String restBasePath, final String expand)
	{
		/* account for the fact that expand could be null */
		final String fixedExpand = expand != null ? "" : expand;
		
		this.initialize(entity, baseUrl, restBasePath, new ExpandData(fixedExpand));
	}
	
	public abstract void initialize(final T entity, final String baseUrl, final String restBasePath, final ExpandData expand);
	
	protected void setLinks(final String baseUrl, final String restBasePath, final Object id)
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

	public String[] getExpand()
	{
		return expand;
	}

	public void setExpand(String[] expand)
	{
		this.expand = expand;
	}
}
