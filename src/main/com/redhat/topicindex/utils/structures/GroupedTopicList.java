package com.redhat.topicindex.utils.structures;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.session.ExtendedTopicList;

public class GroupedTopicList
{
	private Tag detachedTag;
	private ExtendedTopicList topicList;

	public ExtendedTopicList getTopicList()
	{
		return topicList;
	}

	public void setTopicList(final ExtendedTopicList topicList)
	{
		this.topicList = topicList;
	}

	public Tag getDetachedTag()
	{
		return detachedTag;
	}

	public void setDetachedTag(Tag detachedTag)
	{
		this.detachedTag = detachedTag;
	}
}
