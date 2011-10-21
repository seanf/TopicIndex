package com.redhat.topicindex.utils.docbookbuilding.toc;

import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;
import com.redhat.topicindex.utils.docbookbuilding.DocbookUtils;

/**
	This class represents a link to a topic in the toc
*/
public class TocLink extends TocElement 
{
	private String pageName;

	protected String getPageName() 
	{
		return pageName;
	}

	protected void setPageName(final String pageName) 
	{
		this.pageName = pageName;
	}
	
	protected void setTopicId(final Integer topicId)
	{
		this.pageName = Constants.TOPIC_XREF_PREFIX + topicId;
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id)
	{
		super(docbookBuildingOptions, label, id);
		this.pageName = Constants.TOPIC_XREF_PREFIX + id;
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id, final String docbook)
	{
		super(docbookBuildingOptions, label, id, docbook);
		this.pageName = Constants.TOPIC_XREF_PREFIX + id;
		this.docbook = docbook;		
	}
	
	public TocLink()
	{
		super();
		this.pageName = "";
	}

	@Override
	public void generateCode() 
	{
		this.eclipseXml = "<topic label=\"" + label + "\" href=\"" + pageName + ".html\"/>";
		if (this.docbook == null || this.docbook.length() == 0)
			this.docbook = DocbookUtils.buildULinkListItem(pageName + ".html", label);
	}
}
