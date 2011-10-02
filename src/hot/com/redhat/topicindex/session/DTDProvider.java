package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

import com.redhat.ecs.commonutils.HTTPUtilities;
import com.redhat.ecs.commonutils.MIMEUtilities;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.DTDConstants;

/**
	A simple class to provide a download link for the RocBook DTD
 */
@Name("dtdProvider")
public class DTDProvider 
{
	/**
	 * Send the RockBook DTD to the user as a downloaded file.
	 */
	public void downloadRocBookDTD()
	{
		HTTPUtilities.writeOutContent(DTDConstants.DOCBOOK45_DTD, "rockbook.dtd", MIMEUtilities.DTD_MIME_TYPE);
	}
}
