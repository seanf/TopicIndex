package com.redhat.topicindex.utils.topicrenderer;

import java.util.HashMap;
import java.util.Map;

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
	 * This is the ID of the BlobConstant record that holds the zip file
	 * containing the xsl directory from /usr/share/publican
	 */
	private static final Integer PUBLICANXSL_ZIP_ID = 7;
	/** The StringConstant ID of the html.xsl file */
	private static final Integer HTML_XSL_ID = 29;
	/**
	 * This is the URL of the xsl files imported by the html.xsl file below. We
	 * use this as the system id when using a URIResolver to allow us to track
	 * the context in which files are imported.
	 */
	private static final String DOCBOOK_XSL_URL = "http://docbook.sourceforge.net/release/xsl/current/";
	/**
	 * This file is a copy of
	 * /usr/share/publican/Common_Content/JBoss_EAP6/xsl/html.xsl
	 */
	private static final String HTML_XSL_SYSTEMID = "/usr/share/publican/Common_Content/JBoss_EAP6/xsl/html.xsl";
	private static Map<String, byte[]> docbookFiles = null;
	private static String xsl = null;

	private static void initialize()
	{
		final byte[] docbookZip = EntityUtilities.loadBlobConstant(DOCBOOK_ZIP_ID);
		final byte[] publicanZip = EntityUtilities.loadBlobConstant(PUBLICANXSL_ZIP_ID);

		/* load the xsl files from the docbook xsl package */
		docbookFiles = ZipUtilities.mapZipFile(docbookZip, DOCBOOK_XSL_URL, "docbook-xsl-1.76.1/");
		/* load the xsl files from the publican xsl zip file package */
		ZipUtilities.mapZipFile(publicanZip, docbookFiles, "file:/usr/share/publican/");

		xsl = EntityUtilities.loadStringConstant(HTML_XSL_ID);
	}

	public static String transformDocbook(final String xml)
	{
		if (docbookFiles == null)
			initialize();

		if (xml != null && xsl != null && docbookFiles != null)
			return XSLTUtilities.transformXML(xml, xsl, HTML_XSL_SYSTEMID, docbookFiles);

		return null;
	}
}
