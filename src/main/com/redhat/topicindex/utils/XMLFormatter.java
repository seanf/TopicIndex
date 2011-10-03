package com.redhat.topicindex.utils;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSResourceResolver;
import org.w3c.dom.ls.LSSerializer;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;

/**
	This class is used to format the XML output into something a little more readable.
 */
public class XMLFormatter implements LSResourceResolver, DOMErrorHandler
{
	public String formatXML(final String xml)
	{
		if (xml == null || xml.length() == 0)
			return "";
		
		try
		{
			final int encodingIndexStart = xml.indexOf(XMLUtilities.ENCODING_START);			
			final int firstLineBreak = xml.indexOf("\n");
			
			// assume the xml is encoded in UTF-8
			String encoding = "UTF-8";
			
			// make sure we found the encoding attribute
			if (encodingIndexStart != -1)
			{
				final int encodingIndexEnd = xml.indexOf("\"", encodingIndexStart + XMLUtilities.ENCODING_START.length());
				
				// make sure the encoding attribute was found before the first line break
				if (firstLineBreak == -1 || encodingIndexStart < firstLineBreak)
				{
					// make sure we found the end of the attribute
					if (encodingIndexEnd != -1)
					{
						encoding = xml.substring(encodingIndexStart + XMLUtilities.ENCODING_START.length(), encodingIndexEnd);
					}
				}
			}
			
			final DOMImplementationLS domImplementationLS = (DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
			
			final LSInput xmlFile = domImplementationLS.createLSInput();
			xmlFile.setByteStream(new ByteArrayInputStream(xml.getBytes(encoding)));
			
			final LSParser builder = domImplementationLS.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
			builder.getDomConfig().setParameter("resource-resolver", this);
			builder.getDomConfig().setParameter("error-handler", this);
			
			final Document doc = builder.parse(xmlFile);
			
			final LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
			final DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
			if (domConfiguration.canSetParameter("format-pretty-print", Boolean.TRUE)) 
			{
				lsSerializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);				
				final LSOutput lsOutput = domImplementationLS.createLSOutput();
				lsOutput.setEncoding("UTF-8");
		    	final StringWriter stringWriter = new StringWriter();
		    	lsOutput.setCharacterStream(stringWriter);
		    	lsSerializer.write(doc, lsOutput);
		    	return stringWriter.toString();
			}			
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return xml;
	}

	public LSInput resolveResource(final String type, final String namespaceURI, final String publicId, final String systemId, final String baseURI) 
	{
		try 
		{
			final DOMImplementationLS impl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
			final LSInput resource = impl.createLSInput();
			
			if (systemId.equals("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd"))
				resource.setByteStream(new ByteArrayInputStream(DTDConstants.DOCBOOK45_DTD));
			
			// you have to return something for an entity (probably so the XML processor can
			// determine if it is a text or binary file)
			else if (systemId.equals("Book.ent"))			
				resource.setStringData(" ");
			
			return resource;
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return null;
	}

	public boolean handleError(final DOMError error) 
	{
		System.out.println("XMLFormatter.handleError() " + error.getMessage());
		return true;
	}
}
