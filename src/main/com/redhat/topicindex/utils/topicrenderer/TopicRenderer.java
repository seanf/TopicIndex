package com.redhat.topicindex.utils.topicrenderer;

import java.util.Map;

import com.redhat.ecs.commonutils.XSLTUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.EntityUtilities;

public class TopicRenderer
{
	private static final Integer DOCBOOK_ZIP_ID = 6;
	private static final String DOCBOOK_XSL_URL = "http://docbook.sourceforge.net/release/xsl/current/";
	private static Map<String, byte[]> docbookFiles = null;
	private static byte[] xsl = null;
	
	private static void initialize()
	{
		final byte[] docbookZip = EntityUtilities.loadBlobConstant(DOCBOOK_ZIP_ID);
		docbookFiles = ZipUtilities.readZipFile(docbookZip, DOCBOOK_XSL_URL);
		
	}
	
	public static String transformDocbook(final String xml)
	{
		if (docbookFiles == null)
			initialize();
		
		if (xml != null && xsl != null && docbookFiles != null)
			return XSLTUtilities.transformXML(xml, xsl, docbookFiles);
		
		return null;
	}
}
