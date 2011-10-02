package com.redhat.topicindex.utils;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.redhat.ecs.commonutils.DocBookUtilities;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;

/**
	This class is used to validate XML, optionally also validating the XML against a DDT schema
 */
public class XMLValidator implements DOMErrorHandler, LSResourceResolver
{
	protected boolean errorsDetected;
	private String errorText;
	
	
	public Document validateTopicXML(final String xml, final boolean validateAgainstDTD)
	{
		try
		{
			return validateTopicXML(XMLUtilities.convertStringToDocument(xml), validateAgainstDTD);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return null;
	}
	
	public Document validateTopicXML(final Document core, final boolean validateAgainstDTD)
	{
		if (core == null)
			return null;

		try
		{
			errorsDetected = false;
			
			if (validateAgainstDTD)
			{
				final DOMConfiguration config = core.getDomConfig();
				config.setParameter("schema-type", XMLConstants.XML_DTD_NS_URI);
	            config.setParameter("schema-location", Constants.ROCBOOK_DTD);
	            config.setParameter("resource-resolver", this);
	            config.setParameter("error-handler", this);
	            config.setParameter("validate", Boolean.TRUE);
	            config.setParameter("namespaces", Boolean.FALSE);
	            core.normalizeDocument();
			}
			
            if (this.errorsDetected)
            	return null;
            
            // all topics have to be sections
            final Node rootNode = core.getDocumentElement();
            final String documentNodeName = rootNode.getNodeName();
            if (!documentNodeName.equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME))
            	return null;
            
            return core;

		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			
			// something happened, which we will assume means the
			// xml is invalid
			return null;					
		}
	}

	public boolean handleError(final DOMError error) 
	{
		errorsDetected = true;
		errorText = error.getMessage();
		System.out.println("XMLValidator.handleError() " + errorText);
		return true;
	}
	
	public LSInput resolveResource(final String type, final String namespace, final String publicId, final String systemId, final String baseURI)
	{
		try 
		{
			final DOMImplementationLS impl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS");
			final LSInput dtdsource = impl.createLSInput();			
			if (systemId.equals(Constants.ROCBOOK_DTD))
			{
				dtdsource.setByteStream(new ByteArrayInputStream(DTDConstants.ROCBOOK45_DTD));
				return dtdsource;
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return null;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}
}
