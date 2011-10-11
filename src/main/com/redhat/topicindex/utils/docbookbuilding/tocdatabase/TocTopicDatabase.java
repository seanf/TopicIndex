package com.redhat.topicindex.utils.docbookbuilding.tocdatabase;

import java.util.ArrayList;
import java.util.List;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTag;

/**
 * This class represents all the topics that will go into a docbook build, along
 * with some function to retrieve topics based on a set of tags to match or
 * exclude.
 */
public class TocTopicDatabase
{
	private List<Topic> topics = new ArrayList<Topic>();

	public boolean containsTopic(final Topic topic)
	{
		return topics.contains(topic);
	}

	public boolean containsTopic(final Integer topicId)
	{
		return getTopic(topicId) != null;
	}

	public Topic getTopic(final Integer topicId)
	{
		for (final Topic topic : topics)
			if (topic.getTopicId().equals(topicId))
				return topic;

		return null;
	}

	public boolean containsTopicsWithTag(final Tag tag)
	{
		return getMatchingTopics(tag).size() != 0;
	}

	public List<Topic> getMatchingTopics(final List<Tag> matchingTags, final List<Tag> excludeTags, final boolean haveOnlyMatchingTags)
	{
		if (matchingTags == null || excludeTags == null)
			return null;

		final List<Topic> topicList = new ArrayList<Topic>();

		for (final Topic topic : topics)
		{
			/* check to see if the topic has only the matching tags */
			if (haveOnlyMatchingTags && topic.getTopicToTags().size() != matchingTags.size())
				continue;

			/* check for matching tags */
			boolean foundMatchingTag = true;
			for (final Tag matchingTag : matchingTags)
			{
				if (!topic.isTaggedWith(matchingTag))
				{
					foundMatchingTag = false;
					break;					
				}
			}
			if (!foundMatchingTag)
				continue;

			/* check for excluded tags */
			boolean foundExclusionTag = false;
			for (final Tag excludeTag : excludeTags)
			{
				if (topic.isTaggedWith(excludeTag))
				{
					foundExclusionTag = true;
					break;
				}
			}
			if (foundExclusionTag)
				continue;

			topicList.add(topic);
		}

		return topicList;
	}

	public List<Topic> getMatchingTopics(final Tag matchingTag, final List<Tag> excludeTags, final boolean haveOnlyMatchingTags)
	{
		final List<Tag> matchingTags = new ArrayList<Tag>();
		matchingTags.add(matchingTag);
		return getMatchingTopics(matchingTags, excludeTags, haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopics(final Tag matchingTag, final Tag excludeTag, final boolean haveOnlyMatchingTags)
	{
		final List<Tag> excludeTags = new ArrayList<Tag>();
		excludeTags.add(excludeTag);
		return getMatchingTopics(matchingTag, excludeTags, haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopics(final List<Tag> matchingTags, final Tag excludeTag, final boolean haveOnlyMatchingTags)
	{
		final List<Tag> excludeTags = new ArrayList<Tag>();
		matchingTags.add(excludeTag);
		return getMatchingTopics(matchingTags, excludeTags, haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopics(final List<Tag> matchingTags, final List<Tag> excludeTags)
	{
		return getMatchingTopics(matchingTags, excludeTags, false);
	}

	public List<Topic> getMatchingTopics(final Tag matchingTag, final List<Tag> excludeTags)
	{
		return getMatchingTopics(matchingTag, excludeTags, false);
	}

	public List<Topic> getMatchingTopics(final Tag matchingTag, final Tag excludeTag)
	{
		return getMatchingTopics(matchingTag, excludeTag, false);
	}

	public List<Topic> getMatchingTopics(final List<Tag> matchingTags, final Tag excludeTag)
	{
		return getMatchingTopics(matchingTags, excludeTag, false);
	}

	public List<Topic> getMatchingTopics(final Tag matchingTag)
	{
		return getMatchingTopics(matchingTag, new ArrayList<Tag>(), false);
	}

	public List<Topic> getMatchingTopics(final List<Tag> matchingTags)
	{
		return getMatchingTopics(matchingTags, new ArrayList<Tag>(), false);
	}

	public List<Topic> getTopics()
	{
		return topics;
	}

	public void setTopics(List<Topic> topics)
	{
		this.topics = topics;
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final List<TopicToTag> matchingTags, final List<TopicToTag> excludeTags)
	{
		return getMatchingTopics(convertToTagArray(matchingTags), convertToTagArray(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final TopicToTag matchingTag, final List<TopicToTag> excludeTags)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopics(matchingTag.getTag(), convertToTagArray(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final TopicToTag matchingTag, final TopicToTag excludeTag)
	{
		if (matchingTag == null || excludeTag == null)
			return null;

		return getMatchingTopics(matchingTag.getTag(), excludeTag.getTag(), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final List<TopicToTag> matchingTags, final TopicToTag excludeTag)
	{
		if (excludeTag == null)
			return null;

		return getMatchingTopics(convertToTagArray(matchingTags), excludeTag.getTag(), false);
	}

	private List<Tag> convertToTagArray(final List<TopicToTag> topicToTags)
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TopicToTag topicToTag : topicToTags)
			if (topicToTag != null)
				retValue.add(topicToTag.getTag());
		return retValue;
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags, final List<TagToCategory> excludeTags)
	{
		return getMatchingTopics(convertToTagArrayFromTagToCategory(matchingTags), convertToTagArrayFromTagToCategory(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag, final List<TagToCategory> excludeTags)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopics(matchingTag.getTag(), convertToTagArrayFromTagToCategory(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag, final TagToCategory excludeTag)
	{
		if (matchingTag == null || excludeTag == null)
			return null;

		return getMatchingTopics(matchingTag.getTag(), excludeTag.getTag(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags, final TagToCategory excludeTag)
	{
		if (excludeTag == null)
			return null;

		return getMatchingTopics(convertToTagArrayFromTagToCategory(matchingTags), excludeTag.getTag(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopics(matchingTag.getTag(), new ArrayList<Tag>(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags)
	{
		return getMatchingTopics(convertToTagArrayFromTagToCategory(matchingTags), new ArrayList<Tag>(), false);
	}

	private List<Tag> convertToTagArrayFromTagToCategory(final List<TagToCategory> collection)
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToCategory topicToTag : collection)
			if (topicToTag != null)
				retValue.add(topicToTag.getTag());
		return retValue;
	}
}
