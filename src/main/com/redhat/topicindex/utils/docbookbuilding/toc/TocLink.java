package com.redhat.topicindex.utils.docbookbuilding.toc;

import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

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
		this.pageName = "TopicID" + topicId;
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String pageName)
	{
		super(docbookBuildingOptions, label);
		this.pageName = pageName;
		this.docbook = "<listitem><para><ulink url=\"" + pageName + ".html\">" + label + "</ulink></para></listitem>";
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label, final Integer topicId)
	{
		super(docbookBuildingOptions, label);
		this.pageName = "TopicID" + topicId;
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label, final Integer topicId, final String docbook)
	{
		super(docbookBuildingOptions, label, docbook);
		this.pageName = "TopicID" + topicId;
		
	}
	
	public TocLink(final DocbookBuildingOptions docbookBuildingOptions, final String label)
	{
		super(docbookBuildingOptions, label);
		this.pageName = "";
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
	}
}
