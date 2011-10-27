package com.redhat.topicindex.rest;

import com.redhat.topicindex.entity.Tag;

public class TagV1 extends BaseRestV1<Tag> 
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	
	@Override
	protected void initialize(final Tag entity, final String baseUrl, final String restBasePath, final String expand)
	{
		this.id = entity.getTagId();
		this.name = entity.getTagName();
		this.description = entity.getTagDescription();
		
		super.initialize(baseUrl, restBasePath, expand);
	}
}
