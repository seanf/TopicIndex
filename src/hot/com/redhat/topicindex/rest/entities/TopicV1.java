package com.redhat.topicindex.rest.entities;

import java.util.Date;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

/**
 * A representation of the Topic resource to be sent via the REST interface.
 */
@XmlRootElement(name = "topic")
public class TopicV1 extends BaseRESTEntityV1 {
	public static final String ID_NAME = "id";
	public static final String TITLE_NAME = "title";
	public static final String DESCRIPTION_NAME = "description";
	public static final String XML_NAME = "xml";
	public static final String HTML_NAME = "html";
	public static final String TAGS_NAME = "tags";
	public static final String OUTGOING_NAME = "outgoingRelationships";
	public static final String INCOMING_NAME = "incomingRelationships";
	public static final String TWO_WAY_NAME = "twoWayRelationships";

	private Integer id = null;
	private String title = null;
	private String description = null;
	private String xml = null;
	private String html = null;
	private Date lastModified = null;
	private Date created = null;
	private BaseRestCollectionV1<TagV1> tags = null;
	private BaseRestCollectionV1<TopicV1> outgoingRelationships = null;
	private BaseRestCollectionV1<TopicV1> incomingRelationships = null;
	private BaseRestCollectionV1<TopicV1> twoWayRelationships = null;

	public Integer getId() {
		return id;
	}

	public void setId(final Integer id) {
		this.id = id;
		setParamaterToConfigured(ID_NAME);
	}

	@XmlElement
	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
		setParamaterToConfigured(TITLE_NAME);
	}

	@XmlElement
	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
		setParamaterToConfigured(DESCRIPTION_NAME);
	}

	@XmlElement
	public String getXml() {
		return xml;
	}

	public void setXml(final String xml) {
		this.xml = xml;
		setParamaterToConfigured(XML_NAME);
	}

	@XmlElement
	public String getHtml() {
		return html;
	}

	public void setHtml(final String html) {
		this.html = html;
		setParamaterToConfigured(HTML_NAME);
	}

	@XmlElement
	public BaseRestCollectionV1<TagV1> getTags() {
		return tags;
	}

	public void setTags(final BaseRestCollectionV1<TagV1> tags) {
		this.tags = tags;
		setParamaterToConfigured(TAGS_NAME);
	}

	@XmlElement
	public BaseRestCollectionV1<TopicV1> getOutgoingRelationships() {
		return outgoingRelationships;
	}

	public void setOutgoingRelationships(
			final BaseRestCollectionV1<TopicV1> outgoingRelationships) {
		this.outgoingRelationships = outgoingRelationships;
		setParamaterToConfigured(OUTGOING_NAME);
	}

	@XmlElement
	public BaseRestCollectionV1<TopicV1> getIncomingRelationships() {
		return incomingRelationships;
	}

	public void setIncomingRelationships(
			final BaseRestCollectionV1<TopicV1> incomingRelationships) {
		this.incomingRelationships = incomingRelationships;
		setParamaterToConfigured(INCOMING_NAME);
	}

	@XmlElement
	public BaseRestCollectionV1<TopicV1> getTwoWayRelationships() {
		return twoWayRelationships;
	}

	public void setTwoWayRelationships(
			final BaseRestCollectionV1<TopicV1> twoWayRelationships) {
		this.twoWayRelationships = twoWayRelationships;
		setParamaterToConfigured(TWO_WAY_NAME);
	}

	/**
	 * @return the XML contained in a new element, or null if the XML is not
	 *         valid
	 */
	public String getXMLWithNewContainer(final String containerName) {
		assert containerName != null : "The containerName parameter can not be null";

		final Document document = XMLUtilities
				.convertStringToDocument(this.xml);

		if (document == null)
			return null;

		final Element newElement = document.createElement(containerName);
		final Element documentElement = document.getDocumentElement();

		document.removeChild(documentElement);
		document.appendChild(newElement);
		newElement.appendChild(documentElement);

		return XMLUtilities.convertDocumentToString(document);
	}

	public String getXMLWithNoContainer(final Boolean includeTitle) {
		final Document document = XMLUtilities
				.convertStringToDocument(this.xml);

		if (document == null)
			return null;

		String retValue = "";

		final NodeList nodes = document.getDocumentElement().getChildNodes();

		for (int i = 0; i < nodes.getLength(); ++i) {
			final Node node = nodes.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				if (includeTitle != null && !includeTitle) {
					if (node.getNodeName().equals("title")) {
						continue;
					}
				}
				
				retValue += XMLUtilities.convertNodeToString(node);
			}
		}

		return retValue;

	}

	public Date getLastModified()
	{
		return lastModified;
	}

	public void setLastModified(final Date lastModified)
	{
		this.lastModified = lastModified;
	}

	public Date getCreated()
	{
		return created;
	}

	public void setCreated(Date created)
	{
		this.created = created;
	}
}
