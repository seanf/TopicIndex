package com.redhat.topicindex.utils.docbookbuilding.toc;

import java.util.ArrayList;
import java.util.List;

import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;
import com.redhat.topicindex.utils.docbookbuilding.DocbookUtils;

/**
	This class represents a folder in the TOC
 */
public class TocFolderElement extends TocElement
{
	protected List<TocElement> children;

	public List<TocElement> getChildren() 
	{
		return children;
	}

	public void setChildren(final List<TocElement> children) 
	{
		this.children = children;
	}
	
	public TocFolderElement(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id, final List<TocElement> children)
	{
		super(docbookBuildingOptions, label, id);
		this.children = children;
	}
	
	public TocFolderElement(final DocbookBuildingOptions docbookBuildingOptions, final String label, final String id)
	{
		super(docbookBuildingOptions, label, id);
		this.children = new ArrayList<TocElement>();
	}
	
	public TocFolderElement(final DocbookBuildingOptions docbookBuildingOptions)
	{
		super(docbookBuildingOptions);
		this.children = new ArrayList<TocElement>();
	}
	
	public TocFolderElement()
	{
		super();
		this.children = new ArrayList<TocElement>();
	}

	@Override
	public void generateCode() 
	{
		// allow the children to generate their code
		for (final TocElement child : children)
			child.generateCode();
		
		generateDocbookXML();
		generateEclipseXML();		
	}
	
	protected void generateDocbookXML()
	{
		// generate the docbook that represents this folder
		final List<String> childrenDocbook = new ArrayList<String>();
				
		for (final TocElement child : children)
			childrenDocbook.add(child.getDocbook());
		
		this.docbook = DocbookUtils.wrapInListItem(
			DocbookUtils.wrapListItems(childrenDocbook, label)
		);
	}
	
	protected void generateEclipseXML()
	{
		// generate the eclipse xml that represents this folder
		this.eclipseXml = "<topic label=\"" + this.label + "\">";
		for (final TocElement child : children)
			this.eclipseXml += child.eclipseXml;
		this.eclipseXml += "</topic>";
	}
}
