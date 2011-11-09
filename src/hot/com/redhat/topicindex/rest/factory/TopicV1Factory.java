package com.redhat.topicindex.rest.factory;

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
	public void sync(final Topic entity, final TopicV1 dataObject)
	{
		if (dataObject.isParameterSet(TopicV1.TITLE_NAME))
			entity.setTopicTitle(dataObject.getTitle());		
	}
}
