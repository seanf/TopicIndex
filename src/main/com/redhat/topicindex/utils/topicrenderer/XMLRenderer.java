package com.redhat.topicindex.utils.topicrenderer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import com.redhat.ecs.commonutils.XSLTUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class provides a way to convert docbook to HTML.
 */
public class XMLRenderer
{
	/**
	 * This is the ID of the BlobConstant record that holds the docbook XSL ZIP
	 * archive downloaded from
	 * http://sourceforge.net/projects/docbook/files/docbook-xsl/
	 */
	private static final Integer DOCBOOK_ZIP_ID = 6;
	/**
	 * This is the URL of the xsl files imported by the html.xsl file below. We
	 * use this as the system id when using a URIResolver to allow us to track
	 * the context in which files are imported.
	 */
	private static final String DOCBOOK_XSL_URL = "http://docbook.sourceforge.net/release/xsl/current/";
	
	private static final String DOCBOOK_XSL_SYSTEMID = "http://docbook.sourceforge.net/release/xsl/current/xhtml/docbook.xsl";
	/**
	 * This file is a copy of
	 * /usr/share/publican/Common_Content/JBoss_EAP6/xsl/html.xsl
	 */
	private static Map<String, byte[]> docbookFiles = null;
	private static Map<String, String> parameters = null;

	private synchronized static void initialize(final EntityManager entityManager)
	{
		if (docbookFiles == null)			
		{
			System.out.println("Initializing XMLRenderer");
			
			/* set the global paramaters required for building the docbook */
			parameters = Collections.unmodifiableMap(new HashMap<String, String>());
			
			final byte[] docbookZip = EntityUtilities.loadBlobConstant(entityManager, DOCBOOK_ZIP_ID);
			
			if (docbookZip != null)
			{
				/* load the xsl files from the docbook xsl package */
				docbookFiles = Collections.unmodifiableMap(ZipUtilities.mapZipFile(docbookZip, DOCBOOK_XSL_URL, "docbook-xsl-1.76.1/"));
			}
		}
	}

	public static String transformDocbook(final EntityManager entityManager, final String xml)
	{
		initialize(entityManager);
	
		if (xml != null && docbookFiles != null && docbookFiles.containsKey(DOCBOOK_XSL_SYSTEMID))
			return XSLTUtilities.transformXML(xml, new String(docbookFiles.get(DOCBOOK_XSL_SYSTEMID)), DOCBOOK_XSL_SYSTEMID, docbookFiles, parameters);

		return null;
	}
}
