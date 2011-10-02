package com.redhat.topicindex.utils.docbookbuilding.toc;

import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

/**
	This class is the base for all elements that appear in the toc
 */
public abstract class TocElement 
{
	protected String label;
	protected String docbook;
	protected String eclipseXml;
	protected DocbookBuildingOptions docbookBuildingOptions;

	public String getLabel() 
	{
		return label;
	}

	public void setLabel(final DocbookBuildingOptions docbookBuildingOptions, final String label) 
	{
		this.label = label;
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = "";
		this.eclipseXml = "";
	}
	
	public TocElement(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String docbook)
	{
		this.label = label;
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = docbook;
		this.eclipseXml = "";
	}
	
	public TocElement(final DocbookBuildingOptions docbookBuildingOptions, final String label)
	{
		this.label = label;
		this.docbookBuildingOptions = docbookBuildingOptions;
		this.docbook = "";
		this.eclipseXml = "";
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
}
