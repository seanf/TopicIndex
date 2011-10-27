package com.redhat.topicindex.rest;

import com.redhat.topicindex.entity.Topic;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
public class TopicV1 implements RestRepresentation<Topic>
{
	private Integer id = null;
	private String title = null;
	private String description = null;
	private String xml = null;
	private String html = null;

	@Override
	public void initialize(final Topic entity)
	{
		assert entity != null : "Parameter topic can not be null";
		
		this.id = entity.getTopicId();
		this.title = entity.getTopicTitle();
		this.description = entity.getTopicText();
		this.xml = entity.getTopicXML();
		this.html = entity.getTopicRendered();
	}
}
