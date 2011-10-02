package com.redhat.topicindex.utils.docbookbuilding;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.redhat.topicindex.utils.Constants;

public class DocbookUtils 
{
	/** A prefix for error xref ids */
	private static final String ERROR_XREF_ID_PREFIX = "TagErrorXRef";
	
	public static String buildChapter(final String contents, final String title)
	{
		return buildChapter(contents, title, null);
	}
	
	public static String buildChapter(final String contents, final String title, final String id)
	{
		final String titleContents = title==null||title.length()==0?"":title;
		final String chapterContents = contents==null||contents.length()==0?"":contents;
		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
		return "<chapter" + idAttribute + "><title>" + titleContents + "</title>" + chapterContents + "</chapter>";
	}
	
	public static String addXMLBoilerplate(final String xml)
	{
		return "<?xml version='1.0' encoding='utf-8' ?>\n" +
		"<!DOCTYPE chapter PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" [\n" +
		"<!ENTITY % BOOK_ENTITIES SYSTEM \"Book.ent\">\n" +
		"%BOOK_ENTITIES;\n" +
		"]>\n\n" +
		xml;
	}
	
	public static String buildXRefListItem(final String xref, final String role)
	{
		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
		return "<listitem><para><xref" + roleAttribute + " linkend=\"" + xref + "\"/></para></listitem>";
	}
	
	public static List<Element> buildXRef(final Document xmlDoc, final String xref)
	{
		final List<Element> retValue = new ArrayList<Element>();
		
		final Element xrefItem = xmlDoc.createElement("xref");
		xrefItem.setAttribute("linkend", xref);
		
		retValue.add(xrefItem);
		
		return retValue;
	}
	
	public static List<Element> buildEmphasisPrefixedXRef(final Document xmlDoc, final String prefix, final String xref)
	{
		final List<Element> retValue = new ArrayList<Element>();
		
		final Element emphasis = xmlDoc.createElement("emphasis");
		emphasis.setTextContent(prefix);
		retValue.add(emphasis);
		
		final Element xrefItem = xmlDoc.createElement("xref");
		xrefItem.setAttribute("linkend", xref);
		retValue.add(xrefItem);
		
		return retValue;
	}
	
	public static Node buildDOMXRefLinkListItem(final String xref, final String title, final Document xmlDoc)
	{
		final Element listItem = xmlDoc.createElement("listitem");
		
		final Element paraItem = xmlDoc.createElement("para");
		listItem.appendChild(paraItem);
		
		final Element linkItem = xmlDoc.createElement("link");
		linkItem.setAttribute("linkend", xref);
		linkItem.setTextContent(title);
		paraItem.appendChild(linkItem);
		
		return listItem;
	}
	
	public static Node buildDOMLinkListItem(final List<Node> children, final Document xmlDoc)
	{
		final Element listItem = xmlDoc.createElement("listitem");
		
		final Element paraItem = xmlDoc.createElement("para");
		listItem.appendChild(paraItem);
		
		for (final Node node : children)
			paraItem.appendChild(node);
		
		return listItem;
	}
	
	public static Node buildDOMXRef(final String xref, final String title, final Document xmlDoc)
	{
		final Element linkItem = xmlDoc.createElement("link");
		linkItem.setAttribute("linkend", xref);
		linkItem.setTextContent(title);
		return linkItem;
	}
	
	public static Node buildDOMText(final String title, final Document xmlDoc)
	{
		final Node textNode = xmlDoc.createTextNode(title);
		return textNode;
	}
	
	public static String buildErrorListItem(final String topicID, final String error)
	{
		return buildErrorListItem(topicID, error, true, null);
	}
	
	public static String buildErrorListItem(final String topicID, final String error, final boolean addXRefId)
	{
		return buildErrorListItem(topicID, error, addXRefId, null);
	}
	
	public static String buildErrorListItem(final String topicID, final String error, final boolean addXRefId, final String codeExample)
	{
		String revalue = "<listitem";
		if (addXRefId)
			revalue += " id=\"" + ERROR_XREF_ID_PREFIX + topicID + "\" xreflabel=\"Topic " + topicID + "\"";
		
		revalue += "><para>Topic <ulink url=\"" + Constants.SERVER_URL + "/TopicIndex/CustomSearchTopicList.seam?topicIds=" + topicID + "\">" + topicID + "</ulink> " + error; 
		
		if (codeExample != null)
			revalue += "<programlisting><![CDATA[" + codeExample + "]]></programlisting>";
			
		revalue	 += "</para></listitem>\n";
		
		return revalue;	
	}
	
	public static String buildErrorListItem(final String error)
	{
		return "<listitem><para>" + error + "</para></listitem>\n";
	}
	
	public static String buildSection(final String contents, final String title)
	{
		return buildSection(contents, title, null, null, null);
	}
	
	public static String buildSection(final String contents, final String title, final String id)
	{
		return buildSection(contents, title, id, null, null);
	}
	
	public static String buildSection(final String contents, final String title, final String id, final String titleRole)
	{
		return buildSection(contents, title, id, titleRole, null);
	}
	
	public static String buildSection(final String contents, final String title, final String id, final String titleRole, final String xreflabel)
	{
		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
		final String xreflabelAttribute = xreflabel==null||xreflabel.length()==0?"":" xreflabel=\"" + xreflabel + "\"";
		final String titleRoleAttribute = titleRole==null||titleRole.length()==0?"":" role=\"" + titleRole + "\"";
		
		return "<section" + idAttribute + xreflabelAttribute + ">\n" +
			"<title" + titleRoleAttribute + ">" + title + "</title>\n" +
			contents + "\n" +
			
			"	<simplesect>\n" +
			"		<title></title>\n" +
			"		<para>\n" + 
			"			<remark>Built with Skynet version " + Constants.BUILD + "</remark>\n" +
			"		</para>\n" +
			"	</simplesect>" +
			
			"</section>\n";
	}
	
	public static String wrapInListItem(final String content)
	{
		return "<listitem>" + content + "</listitem>";
	}
	
	public static String wrapListItems(final List<String> listItems, final String title)
	{
		final String titleElement = title == null || title.length() == 0 ? "" : "<title>" + title + "</title>";
		
		String retValue = "<itemizedlist>" + titleElement;
		for (final String listItem : listItems)
			retValue += listItem;
		retValue += "</itemizedlist>";
		return retValue;
	}
	
	public static String wrapListItemsInPara(final String listItems)
	{
		if (listItems.length() != 0)
		{
			return 
				"<para>" +
				"<itemizedlist>\n" + listItems + "</itemizedlist>" +
				"</para>";
		}
		
		return "";
	}

	public static String wrapInPara(final String contents)
	{
		return wrapInPara(contents, null, null);
	}
	
	public static String wrapInPara(final String contents, final String role)
	{
		return wrapInPara(contents, role, null);
	}
	
	public static String wrapInPara(final String contents, final String role, final String id)
	{
		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
		return 
			"<para" + idAttribute + roleAttribute + ">" +
			contents +
			"</para>";
	}
	
	public static List<Element> wrapItemizedListItemsInPara(final Document xmlDoc, final List<List<Element>> items)
	{
		final List<Element> retValue = new ArrayList<Element>();
		
		final Element para = xmlDoc.createElement("para");
		
		final Element itemizedlist = xmlDoc.createElement("itemizedlist");
		para.appendChild(itemizedlist);
				
		for (final List<Element> itemSequence : items)
		{
			final Element listitem = xmlDoc.createElement("listitem");
			itemizedlist.appendChild(listitem);
			
			final Element listItemPara = xmlDoc.createElement("para");
			listitem.appendChild(listItemPara);
			
			for (final Element item : itemSequence)
			{				
				listItemPara.appendChild(item);
			}
		}
		
		retValue.add(para);
		
		return retValue;
	}
	
	public static List<Element> wrapOrderedListItemsInPara(final Document xmlDoc, final List<List<Element>> items)
	{
		final List<Element> retValue = new ArrayList<Element>();
		
		final Element para = xmlDoc.createElement("para");
		
		final Element orderedlist = xmlDoc.createElement("orderedlist");
		para.appendChild(orderedlist);
				
		for (final List<Element> itemSequence : items)
		{
			final Element listitem = xmlDoc.createElement("listitem");
			orderedlist.appendChild(listitem);
			
			final Element listItemPara = xmlDoc.createElement("para");
			listitem.appendChild(listItemPara);
			
			for (final Element item : itemSequence)
			{
				listItemPara.appendChild(item);
			}
		}
		
		retValue.add(para);
		
		return retValue;
	}
	
	public static List<Element> wrapItemsInListItems(final Document xmlDoc, final List<List<Element>> items)
	{
		final List<Element> retValue = new ArrayList<Element>();
		
		for (final List<Element> itemSequence : items)
		{			
			final Element listitem = xmlDoc.createElement("listitem");
			final Element listItemPara = xmlDoc.createElement("para");
			listitem.appendChild(listItemPara);
			
			for (final Element item : itemSequence)
			{
				listItemPara.appendChild(item);
			}
			
			retValue.add(listitem);
		}

		return retValue;
	}

	public static String wrapInSimpleSect(final String contents)
	{
		return wrapInSimpleSect(contents, null, null);
	}
	
	public static String wrapInSimpleSect(final String contents, final String role)
	{
		return wrapInSimpleSect(contents, null, null);
	}
	
	public static String wrapInSimpleSect(final String contents, final String role, final String id)
	{
		final String roleAttribute = role==null||role.length()==0?"":" role=\"" + role + "\"";
		final String idAttribute = id==null||id.length()==0?"":" id=\"" + id + "\"";
		
		return 
			"<simplesect" + idAttribute + roleAttribute + ">\n"+
			"\t<title></title>\n" +
			contents + "\n" + 
			"</simplesect>";
	}
	
	public static Element wrapListItems(final List<Node> listItems, final String title, final Document xmlDoc)
	{
		final Element formalParaElement = xmlDoc.createElement("formalpara");
		
		final Element titleElement = xmlDoc.createElement("title");
		formalParaElement.appendChild(titleElement);
		titleElement.setTextContent(title);
		
		final Element paraElement = xmlDoc.createElement("para");
		formalParaElement.appendChild(paraElement);
		
		final Element itemizedlistElement = xmlDoc.createElement("itemizedlist");
		paraElement.appendChild(itemizedlistElement);
		
		for (final Node listItem : listItems)
			itemizedlistElement.appendChild(listItem);
		
		return formalParaElement;
	}
	
	public static void insertNodeAfter(final Node reference, final Node insert)
	{
		final Node parent = reference.getParentNode();
		final Node nextSibling = reference.getNextSibling();
		
		if (parent == null)
			return;
		
		if (nextSibling != null)
			parent.insertBefore(insert, nextSibling);
		else
			parent.appendChild(insert);
	}
	
	public static Node createRelatedTopicLink(final Document xmlDoc, final Node parent, final String xref)
	{
		final Element listItem = xmlDoc.createElement("listitem");
		parent.appendChild(listItem);
		
		final Element paraItem = xmlDoc.createElement("para");
		listItem.appendChild(paraItem);
		
		final Element xrefItem = xmlDoc.createElement("xref");
		xrefItem.setAttribute("linkend", xref);
		paraItem.appendChild(xrefItem);
		
		return listItem;
	}
	
	public static Node createRelatedTopicItemizedList(final Document xmlDoc, final String title)
	{
		final Node itemizedlist = xmlDoc.createElement("itemizedlist");
		final Node itemizedlistTitle = xmlDoc.createElement("title");
		itemizedlistTitle.setTextContent(title);
		itemizedlist.appendChild(itemizedlistTitle);
		
		return itemizedlist;
	}
}
