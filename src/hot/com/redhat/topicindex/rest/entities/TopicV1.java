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

	@Override
	public void initialize(final Topic entity, final String baseUrl, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		this.id = entity.getTopicId();
		this.title = entity.getTopicTitle();
		this.description = entity.getTopicText();
		this.xml = entity.getTopicXML();
		this.html = entity.getTopicRendered();
		this.setExpand(new String[]	{ RESTv1.TAGS_EXPANSION_NAME });

		if (expand.contains(RESTv1.TAGS_EXPANSION_NAME))
		{			
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, expand, baseUrl);
		}
		else
		{
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME);
		}

		super.setLinks(baseUrl, RESTv1.TOPIC_URL_NAME, this.id);
	}
}
