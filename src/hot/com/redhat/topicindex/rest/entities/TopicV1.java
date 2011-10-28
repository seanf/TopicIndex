package com.redhat.topicindex.rest.entities;

import java.util.Set;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
public class TopicV1 extends BaseRestV1<Topic>
{
	private Integer id = null;
	private String title = null;
	private String description = null;
	private String xml = null;
	private String html = null;
	private BaseRestCollectionV1<TagV1, Tag> tags = new BaseRestCollectionV1<TagV1, Tag>();
	private BaseRestCollectionV1<TopicV1, Topic> outgoingRelationships = new BaseRestCollectionV1<TopicV1, Topic>();
	private BaseRestCollectionV1<TopicV1, Topic> incomingRelationships = new BaseRestCollectionV1<TopicV1, Topic>();
	private BaseRestCollectionV1<TopicV1, Topic> twoWayRelationships = new BaseRestCollectionV1<TopicV1, Topic>();

	@Override
	public void initialize(final Topic entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		this.id = entity.getTopicId();
		this.title = entity.getTopicTitle();
		this.description = entity.getTopicText();
		this.xml = entity.getTopicXML();
		this.html = entity.getTopicRendered();
		this.setExpand(new String[]
		{ RESTv1.TAGS_EXPANSION_NAME, RESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, RESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, RESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME });

		if (expand.contains(RESTv1.TAGS_EXPANSION_NAME))
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl);
		else
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType);

		if (expand.contains(RESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME))
			outgoingRelationships.initialize(TopicV1.class, entity.getOutgoingTopicsArray(), RESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl);
		else
			outgoingRelationships.initialize(TopicV1.class, entity.getOutgoingTopicsArray(), RESTv1.TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME, dataType);
		
		if (expand.contains(RESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME))
			incomingRelationships.initialize(TopicV1.class, entity.getIncomingRelatedTopicsArray(), RESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl);
		else
			incomingRelationships.initialize(TopicV1.class, entity.getIncomingRelatedTopicsArray(), RESTv1.TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME, dataType);
		
		if (expand.contains(RESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME))
			twoWayRelationships.initialize(TopicV1.class, entity.getIncomingRelatedTopicsArray(), RESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME, dataType, expand, baseUrl);
		else
			twoWayRelationships.initialize(TopicV1.class, entity.getIncomingRelatedTopicsArray(), RESTv1.TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME, dataType);

		super.setLinks(baseUrl, RESTv1.TOPIC_URL_NAME, dataType, this.id);
	}
}
