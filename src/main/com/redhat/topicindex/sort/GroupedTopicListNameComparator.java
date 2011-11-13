package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.utils.structures.GroupedTopicList;

public class GroupedTopicListNameComparator implements Comparator<GroupedTopicList>
{
	public int compare(final GroupedTopicList o1, final GroupedTopicList o2)
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getDetachedTag() == null && o2.getDetachedTag() == null)
			return 0;
		if (o1.getDetachedTag() == null)
			return -1;
		if (o2.getDetachedTag() == null)
			return 1;
		
		if (o1.getDetachedTag().getTagName() == null && o2.getDetachedTag().getTagName() == null)
			return 0;
		if (o1.getDetachedTag().getTagName() == null)
			return -1;
		if (o2.getDetachedTag().getTagName() == null)
			return 1;
		
		return o1.getDetachedTag().getTagName().compareTo(o2.getDetachedTag().getTagName());
	}
}
