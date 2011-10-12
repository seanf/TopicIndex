package com.redhat.topicindex.utils.docbookbuilding.toc;

import java.util.List;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.utils.docbookbuilding.tocdatabase.TocTopicDatabase;

public class TocLandingPage
{
	private TocTopicDatabase tocTopicDatabase;
	private List<Tag> matchingTags;
	private String docbook;

	public String getDocbook()
	{
		return docbook;
	}

	public void setDocbook(String docbook)
	{
		this.docbook = docbook;
	}
	
	public TocLandingPage(final TocTopicDatabase tocTopicDatabase, final List<Tag> matchingTags)
	{
		this.tocTopicDatabase = tocTopicDatabase;
		this.matchingTags = matchingTags;
		
		generateDocbook();
	}
	
	private void generateDocbook()
	{
		
	}
}
