package com.redhat.topicindex.utils.topicrenderer;

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
	private static final Integer HTML_XSL_ID = 29;
	private static final String DOCBOOK_XSL_URL = "http://docbook.sourceforge.net/release/xsl/current/";
	private static Map<String, byte[]> docbookFiles = null;
	private static String xsl = null;

	private static void initialize()
	{
		final byte[] docbookZip = EntityUtilities.loadBlobConstant(DOCBOOK_ZIP_ID);
		final byte[] publicanZip = EntityUtilities.loadBlobConstant(PUBLICANXSL_ZIP_ID);

		/* load the xsl files from the docbook xsl package */
		docbookFiles = ZipUtilities.mapZipFile(docbookZip, DOCBOOK_XSL_URL, "docbook-xsl-1.76.1/");
		/* load the xsl files from the publican xsl zip file package */
		ZipUtilities.mapZipFile(publicanZip, docbookFiles, "../../../");

		xsl = EntityUtilities.loadStringConstant(HTML_XSL_ID);
	}

	public static String transformDocbook(final String xml)
	{
		//if (docbookFiles == null)
			initialize();

		if (xml != null && xsl != null && docbookFiles != null)
			return XSLTUtilities.transformXML(xml, xsl, docbookFiles);

		return null;
	}
}
