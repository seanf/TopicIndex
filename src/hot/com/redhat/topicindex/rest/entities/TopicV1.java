package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
public class TopicV1 extends BaseRestV1
{
	public static final String ID_NAME = "id";
	public static final String TITLE_NAME = "title";
	public static final String DESCRIPTION_NAME = "description";
	public static final String XML_NAME = "xml";
	public static final String HTML_NAME = "html";
	public static final String TAGS_NAME = "tags";
	public static final String OUTGOING_NAME = "outgoingRelationships";
	public static final String INCOMING_NAME = "incomingRelationships";
	public static final String TWO_WAY_NAME = "twoWayRelationships";
	
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

	public void setId(final Integer id)
	{
		this.id = id;
		setParamaterToConfigured(ID_NAME);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(final String title)
	{
		this.title = title;
		setParamaterToConfigured(TITLE_NAME);
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(final String description)
	{
		this.description = description;
		setParamaterToConfigured(DESCRIPTION_NAME);
	}

	public String getXml()
	{
		return xml;
	}

	public void setXml(final String xml)
	{
		this.xml = xml;
		setParamaterToConfigured(XML_NAME);
	}

	public String getHtml()
	{
		return html;
	}

	public void setHtml(final String html)
	{
		this.html = html;
		setParamaterToConfigured(HTML_NAME);
	}

	public BaseRestCollectionV1<TagV1> getTags()
	{
		return tags;
	}

	public void setTags(final BaseRestCollectionV1<TagV1> tags)
	{
		this.tags = tags;
		setParamaterToConfigured(TAGS_NAME);
	}

	public BaseRestCollectionV1<TopicV1> getOutgoingRelationships()
	{
		return outgoingRelationships;
	}

	public void setOutgoingRelationships(final BaseRestCollectionV1<TopicV1> outgoingRelationships)
	{
		this.outgoingRelationships = outgoingRelationships;
		setParamaterToConfigured(OUTGOING_NAME);
	}

	public BaseRestCollectionV1<TopicV1> getIncomingRelationships()
	{
		return incomingRelationships;
	}

	public void setIncomingRelationships(final BaseRestCollectionV1<TopicV1> incomingRelationships)
	{
		this.incomingRelationships = incomingRelationships;
		setParamaterToConfigured(INCOMING_NAME);		
	}

	public BaseRestCollectionV1<TopicV1> getTwoWayRelationships()
	{
		return twoWayRelationships;
	}

	public void setTwoWayRelationships(final BaseRestCollectionV1<TopicV1> twoWayRelationships)
	{
		this.twoWayRelationships = twoWayRelationships;
		setParamaterToConfigured(TWO_WAY_NAME);
	}
}
