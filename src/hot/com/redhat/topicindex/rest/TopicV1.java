package com.redhat.topicindex.rest;

import java.util.Set;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTag;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
public class TopicV1 extends BaseRestV1<Topic>
{
	private static final String TAGS_EXPANSION_NAME = "tags";
	private Integer id = null;
	private String title = null;
	private String description = null;
	private String xml = null;
	private String html = null;
	private BaseRestCollectionV1<TagV1> tags = new BaseRestCollectionV1<TagV1>();

	@Override
	public void initialize(final Topic entity, final String baseUrl, final String restBasePath, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";

		this.id = entity.getTopicId();
		this.title = entity.getTopicTitle();
		this.description = entity.getTopicText();
		this.xml = entity.getTopicXML();
		this.html = entity.getTopicRendered();
		this.setExpand(new String[]	{ TAGS_EXPANSION_NAME });

		this.tags.setSize(entity.getTopicToTags().size());
		this.tags.setExpand(TAGS_EXPANSION_NAME);

		if (expand.contains(TAGS_EXPANSION_NAME))
		{
			final ExpandData secondLevelExpandData = expand.getNextLevel(TAGS_EXPANSION_NAME);
			
			final Set<TopicToTag> topicToTags = entity.getTopicToTags();
			final TagV1[] tagsArray = new TagV1[topicToTags.size()];

			int i = 0;
			for (final TopicToTag topicToTag : topicToTags)
			{
				final Tag tag = topicToTag.getTag();
				final TagV1 tagV1 = new TagV1();
				tagV1.initialize(tag, baseUrl, "tag", secondLevelExpandData);
				tagsArray[i] = tagV1;
				++i;
			}

			this.tags.setItems(tagsArray);
		}

		super.setLinks(baseUrl, restBasePath, this.id);
	}
}
