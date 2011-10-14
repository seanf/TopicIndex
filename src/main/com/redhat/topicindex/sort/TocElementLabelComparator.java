package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.utils.docbookbuilding.toc.TocElement;

public class TocElementLabelComparator implements Comparator<TocElement>
{
	@Override
	public int compare(final TocElement o1, final TocElement o2)
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1.getLabel() == null && o2.getLabel() == null)
			return 0;
		if (o1.getLabel() == null)
			return -1;
		if (o2.getLabel() == null)
			return 1;
		
		return o1.getLabel().compareTo(o2.getLabel());
	}

}
