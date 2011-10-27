package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;

public class TagV1 extends BaseRestV1<Tag> 
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	
	@Override
	public void initialize(final Tag entity, final String baseUrl, final ExpandData expand)
	{
		this.id = entity.getTagId();
		this.name = entity.getTagName();
		this.description = entity.getTagDescription();
		
		super.setLinks(baseUrl, RESTv1.TAG_URL_NAME, this.id);
	}
}
