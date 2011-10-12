package com.redhat.topicindex.utils.docbookbuilding.tocdatabase;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonutils.CollectionUtilities;
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

	public void addTopic(final Topic topic)
	{
		if (!containsTopic(topic))
			topics.add(topic);
	}
	
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
		return getMatchingTopicsFromInteger(tag.getTagId()).size() != 0;
	}
	
	public boolean containsTopicsWithTag(final Integer tag)
	{
		return getMatchingTopicsFromInteger(tag).size() != 0;
	}
	
	public List<Tag> getTagsFromCategories(final List<Integer> categoryIds)
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		
		for (final Topic topic : topics)
		{
			CollectionUtilities.addAllThatDontExist(topic.getTagsInCategoriesByID(categoryIds), retValue);
		}
		
		return retValue;
	}

	public List<Topic> getMatchingTopicsFromInteger(final List<Integer> matchingTags, final List<Integer> excludeTags, final boolean haveOnlyMatchingTags)
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
			for (final Integer matchingTag : matchingTags)
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
			for (final Integer excludeTag : excludeTags)
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

	public List<Topic> getMatchingTopicsFromInteger(final Integer matchingTag, final List<Integer> excludeTags, final boolean haveOnlyMatchingTags)
	{
		return getMatchingTopicsFromInteger(CollectionUtilities.toArrayList(matchingTag), excludeTags, haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopicsFromInteger(final Integer matchingTag, final Integer excludeTag, final boolean haveOnlyMatchingTags)
	{
		return getMatchingTopicsFromInteger(matchingTag, CollectionUtilities.toArrayList(excludeTag), haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopicsFromInteger(final List<Integer> matchingTags, final Integer excludeTag, final boolean haveOnlyMatchingTags)
	{
		return getMatchingTopicsFromInteger(matchingTags, CollectionUtilities.toArrayList(excludeTag), haveOnlyMatchingTags);
	}

	public List<Topic> getMatchingTopicsFromInteger(final List<Integer> matchingTags, final List<Integer> excludeTags)
	{
		return getMatchingTopicsFromInteger(matchingTags, excludeTags, false);
	}

	public List<Topic> getMatchingTopicsFromInteger(final Integer matchingTag, final List<Integer> excludeTags)
	{
		return getMatchingTopicsFromInteger(matchingTag, excludeTags, false);
	}

	public List<Topic> getMatchingTopics(final Integer matchingTag, final Integer excludeTag)
	{
		return getMatchingTopicsFromInteger(matchingTag, excludeTag, false);
	}

	public List<Topic> getMatchingTopicsFromInteger(final List<Integer> matchingTags, final Integer excludeTag)
	{
		return getMatchingTopicsFromInteger(matchingTags, excludeTag, false);
	}

	public List<Topic> getMatchingTopicsFromInteger(final Integer matchingTag)
	{
		return getMatchingTopicsFromInteger(matchingTag, new ArrayList<Integer>(), false);
	}

	public List<Topic> getMatchingTopicsFromInteger(final List<Integer> matchingTags)
	{
		return getMatchingTopicsFromInteger(matchingTags, new ArrayList<Integer>(), false);
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
		return getMatchingTopicsFromInteger(convertToIntegerArray(matchingTags), convertToIntegerArray(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final TopicToTag matchingTag, final List<TopicToTag> excludeTags)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTag().getTagId(), convertToIntegerArray(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final TopicToTag matchingTag, final TopicToTag excludeTag)
	{
		if (matchingTag == null || excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTag().getTagId(), excludeTag.getTag().getTagId(), false);
	}

	public List<Topic> getMatchingTopicsFromTopicToTag(final List<TopicToTag> matchingTags, final TopicToTag excludeTag)
	{
		if (excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(convertToIntegerArray(matchingTags), excludeTag.getTag().getTagId(), false);
	}

	private List<Integer> convertToIntegerArray(final List<TopicToTag> topicToTags)
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTag topicToTag : topicToTags)
			if (topicToTag != null)
				retValue.add(topicToTag.getTag().getTagId());
		return retValue;
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags, final List<TagToCategory> excludeTags)
	{
		return getMatchingTopicsFromInteger(convertToTagArrayFromTagToCategory(matchingTags), convertToTagArrayFromTagToCategory(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag, final List<TagToCategory> excludeTags)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTag().getTagId(), convertToTagArrayFromTagToCategory(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag, final TagToCategory excludeTag)
	{
		if (matchingTag == null || excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTag().getTagId(), excludeTag.getTag().getTagId(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags, final TagToCategory excludeTag)
	{
		if (excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(convertToTagArrayFromTagToCategory(matchingTags), excludeTag.getTag().getTagId(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final TagToCategory matchingTag)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTag().getTagId(), new ArrayList<Integer>(), false);
	}

	public List<Topic> getMatchingTopicsFromTagToCategory(final List<TagToCategory> matchingTags)
	{
		return getMatchingTopicsFromInteger(convertToTagArrayFromTagToCategory(matchingTags), new ArrayList<Integer>(), false);
	}

	private List<Integer> convertToTagArrayFromTagToCategory(final List<TagToCategory> collection)
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TagToCategory topicToTag : collection)
		{
			if (topicToTag != null)
			{
				retValue.add(topicToTag.getTag().getTagId());
			}
		}
		return retValue;
	}
	
	public List<Topic> getMatchingTopicsFromTag(final List<Tag> matchingTags, final List<Tag> excludeTags)
	{
		return getMatchingTopicsFromInteger(convertTagArrayToIntegerArray(matchingTags), convertTagArrayToIntegerArray(excludeTags), false);
	}

	public List<Topic> getMatchingTopicsFromTag(final Tag matchingTag, final List<Tag> excludeTags)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTagId(), convertTagArrayToIntegerArray(excludeTags), false);
	}
	
	public List<Topic> getMatchingTopicsFromTag(final Tag matchingTag, final Tag excludeTag)
	{
		if (matchingTag == null || excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTagId(), excludeTag.getTagId(), false);
	}

	public List<Topic> getMatchingTopicsFromTag(final List<Tag> matchingTags, final Tag excludeTag)
	{
		if (excludeTag == null)
			return null;

		return getMatchingTopicsFromInteger(convertTagArrayToIntegerArray(matchingTags), excludeTag.getTagId(), false);
	}
	
	public List<Topic> getMatchingTopicsFromTag(final Tag matchingTag)
	{
		if (matchingTag == null)
			return null;

		return getMatchingTopicsFromInteger(matchingTag.getTagId(), new ArrayList<Integer>(), false);
	}
	
	public List<Topic> getMatchingTopicsFromTag(final List<Tag> matchingTags)
	{
		return getMatchingTopicsFromInteger(convertTagArrayToIntegerArray(matchingTags), new ArrayList<Integer>(), false);
	}

	private List<Integer> convertTagArrayToIntegerArray(final List<Tag> tags)
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final Tag tag : tags)
			if (tag != null)
				retValue.add(tag.getTagId());
		return retValue;
	}
}