package com.redhat.topicindex.utils.docbookbuilding.toc;

import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

/**
	This class is the base for all elements that appear in the toc
 */
public abstract class TocElement 
{
	/** The name of this element */
	protected String label;
	/** The DocBook XML that is used to render this element */
	protected String docbook;
	/** The XML that is used to render this element in an Eclipse tree */
	protected String eclipseXml;
	/** The options specified by the user when building the DocBook */
	protected DocbookBuildingOptions docbookBuildingOptions;
	/** The id of this element */
	protected String id;

	public String getLabel() 
	{
		return label;
	}

	public void setLabel(final String label) 
	{
		this.label = label;
	}
	
	public TocElement(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id, final String docbook)
	{
		this.label = label;
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = docbook;
		this.eclipseXml = "";
		this.id = id;
	}
	
	public TocElement(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id)
	{
		this.label = label;
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = "";
		this.eclipseXml = "";
		this.id = id;
	}
	
	public TocElement(final DocbookBuildingOptions docbookBuildingOptions)
	{
		this.label = "";
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = "";
		this.eclipseXml = "";
	}
	
	public TocElement()
	{
		this.label = "";
		this.docbookBuildingOptions = null;
		this.docbook = "";
		this.eclipseXml = "";
	}

	public String getDocbook() 
	{
		return docbook;
	}

	public void setDocbook(final String docbook) 
	{
		this.docbook = docbook;
	}

	public String getEclipseXml() 
	{
		return eclipseXml;
	}

	public void setEclipseXml(final String eclipseXml) 
	{
		this.eclipseXml = eclipseXml;
	}
	
	public abstract void generateCode();

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}
}
