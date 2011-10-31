package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
public class TopicV1 extends BaseRestV1
{
	private Integer id = null;
	private String title = null;
	private String description = null;
	private String xml = null;
	private String html = null;
	private BaseRestCollectionV1<TagV1> tags = null;
	private BaseRestCollectionV1<TopicV1> outgoingRelationships = null;
	private BaseRestCollectionV1<TopicV1> incomingRelationships = null;
	private BaseRestCollectionV1<TopicV1> twoWayRelationships = null;

	public Integer getId()
	{
		return id;
	}

	public void setId(Integer id)
	{
		this.id = id;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getXml()
	{
		return xml;
	}

	public void setXml(String xml)
	{
		this.xml = xml;
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(String html)
	{
		this.html = html;
	}

	public BaseRestCollectionV1<TagV1> getTags()
	{
		return tags;
	}

	public void setTags(BaseRestCollectionV1<TagV1> tags)
	{
		this.tags = tags;
	}

	public BaseRestCollectionV1<TopicV1> getOutgoingRelationships()
	{
		return outgoingRelationships;
	}

	public void setOutgoingRelationships(final BaseRestCollectionV1<TopicV1> outgoingRelationships)
	{
		this.outgoingRelationships = outgoingRelationships;
	}

	public BaseRestCollectionV1<TopicV1> getIncomingRelationships()
	{
		return incomingRelationships;
	}

	public void setIncomingRelationships(final BaseRestCollectionV1<TopicV1> incomingRelationships)
	{
		this.incomingRelationships = incomingRelationships;
	}

	public BaseRestCollectionV1<TopicV1> getTwoWayRelationships()
	{
		return twoWayRelationships;
	}

	public void setTwoWayRelationships(final BaseRestCollectionV1<TopicV1> twoWayRelationships)
	{
		this.twoWayRelationships = twoWayRelationships;
	}
}
