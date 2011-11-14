package com.redhat.topicindex.rest.factory;

import javax.persistence.EntityManager;

import org.jboss.resteasy.spi.BadRequestException;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.BaseRESTv1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicV1;

public class TopicV1Factory implements RESTDataObjectFactory<TopicV1, Topic>
{
	@Override
	public TopicV1 create(final Topic entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		final TopicV1 retValue = new TopicV1();

		retValue.setId(entity.getTopicId());
		retValue.setTitle(entity.getTopicTitle());
		retValue.setDescription(entity.getTopicText());
		retValue.setXml(entity.getTopicXML());
		retValue.setHtml(entity.getTopicRendered());
		retValue.setLastModified(entity.getLatestRevisionDate());
		retValue.setCreated(entity.getTopicTimeStamp());
		retValue.setExpand(new String[]
		{ BaseRESTv1.TAGS_EXPANSION_NAME, BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, BaseRESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME });

		if (expand.contains(BaseRESTv1.TAGS_EXPANSION_NAME))
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));
		else
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType));

		if (expand.contains(BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME))
			retValue.setOutgoingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getOutgoingTopicsArray(), BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl));
		else
			retValue.setOutgoingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getOutgoingTopicsArray(), BaseRESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType));

		if (expand.contains(BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME))
			retValue.setIncomingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl));
		else
			retValue.setIncomingRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType));

		if (expand.contains(BaseRESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME))
			retValue.setTwoWayRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl));
		else
			retValue.setTwoWayRelationships(new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(new TopicV1Factory(), entity.getIncomingRelatedTopicsArray(), BaseRESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME, dataType));

		retValue.setLinks(baseUrl, BaseRESTv1.TOPIC_URL_NAME, dataType, retValue.getId());

		return retValue;
	}

	@Override
	public TopicV1 create(final Topic entity, final String baseUrl, final String dataType, final String expand)
	{
		/* account for the fact that expand could be null */
		final String fixedExpand = expand == null ? "" : expand;

		return this.create(entity, baseUrl, dataType, new ExpandData(fixedExpand));
	}

	@Override
	public void sync(final EntityManager entityManager, final Topic entity, final TopicV1 dataObject)
	{
		/* sync the basic properties */
		if (dataObject.isParameterSet(TopicV1.TITLE_NAME))
			entity.setTopicTitle(dataObject.getTitle());
		if (dataObject.isParameterSet(TopicV1.DESCRIPTION_NAME))
			entity.setTopicText(dataObject.getDescription());
		if (dataObject.isParameterSet(TopicV1.XML_NAME))
			entity.setTopicXML(dataObject.getXml());

		if (dataObject.isParameterSet(TopicV1.OUTGOING_NAME))
		{
			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				if (topic.getAddItem())
				{
					entity.addRelationshipTo(entityManager, topic.getId());
				}
				else if (topic.getRemoveItem())
				{
					entity.removeRelationshipTo(entityManager, topic.getId());
				}
			}
		}

		if (dataObject.isParameterSet(TopicV1.INCOMING_NAME))
		{
			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
				if (otherTopic == null)
					throw new BadRequestException("No entity was found with the primary key " + topic.getId());
				
				if (topic.getAddItem())
				{
					otherTopic.addRelationshipTo(entityManager, entity.getTopicId());
				}
				else if (topic.getRemoveItem())
				{
					otherTopic.removeRelationshipTo(entityManager, entity.getTopicId());
				}
				
			}
		}
		
		if (dataObject.isParameterSet(TopicV1.TWO_WAY_NAME))
		{
			for (final TopicV1 topic : dataObject.getIncomingRelationships().getItems())
			{
				final Topic otherTopic = entityManager.find(Topic.class, topic.getId());
				if (otherTopic == null)
					throw new BadRequestException("No entity was found with the primary key " + topic.getId());
				
				if (topic.getAddItem())
				{
					entity.addRelationshipTo(entityManager, topic.getId());
					otherTopic.addRelationshipTo(entityManager, entity.getTopicId());
				}
				else if (topic.getRemoveItem())
				{
					entity.removeRelationshipTo(entityManager, topic.getId());
					otherTopic.removeRelationshipTo(entityManager, entity.getTopicId());
				}				
			}
		}
		
		if (dataObject.isParameterSet(TopicV1.TAGS_NAME))
		{
			for (final TagV1 tag : dataObject.getTags().getItems())
			{
				if (tag.getAddItem())
				{
					entity.addTag(entityManager, tag.getId());
				}
				else if (tag.getRemoveItem())
				{
					entity.removeTag(tag.getId());
				}
			}
		}
	}

	@Override
	public Topic create(final EntityManager entityManager, final TopicV1 dataObject)
	{
		final Topic entity = new Topic();
		this.sync(entityManager, entity, dataObject);
		return entity;		
	}
}
