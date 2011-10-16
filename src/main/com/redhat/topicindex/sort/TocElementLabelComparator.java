package com.redhat.topicindex.sort;

import java.util.Comparator;

import com.redhat.topicindex.utils.docbookbuilding.toc.TocElement;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocFolderElement;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocLink;

public class TocElementLabelComparator implements Comparator<TocElement>
{
	private boolean linksAppearAboveFolders;
	
	public TocElementLabelComparator(final boolean linksAppearAboveFolders)
	{
		this.linksAppearAboveFolders = linksAppearAboveFolders;
	}
	
	@Override
	public int compare(final TocElement o1, final TocElement o2)
	{
		if (o1 == null && o2 == null)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		
		if (o1 instanceof TocLink && o2 instanceof TocFolderElement)
			return linksAppearAboveFolders ? -1 : 1;
		if (o1 instanceof TocFolderElement && o2 instanceof TocLink)
			return linksAppearAboveFolders ? 1 : -1;
		
		if (o1.getLabel() == null && o2.getLabel() == null)
			return 0;
		if (o1.getLabel() == null)
			return -1;
		if (o2.getLabel() == null)
			return 1;
		
		return o1.getLabel().compareTo(o2.getLabel());
	}

}
