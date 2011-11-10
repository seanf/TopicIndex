package com.redhat.topicindex.utils.docbookbuilding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.sort.ExternalListSort;
import com.redhat.topicindex.sort.TopicTitleComparator;
import com.redhat.topicindex.sort.TopicTitleSorter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.docbookbuilding.tocdatabase.TocTopicDatabase;
import com.redhat.topicindex.utils.structures.GenericInjectionPoint;
import com.redhat.topicindex.utils.structures.GenericInjectionPointDatabase;

/**
 * This class takes the XML from a topic and modifies it to include and injected
 * content.
 */
public class XMLPreProcessor
{
	/**
	 * Used to identify that an <orderedlist> should be generated for the
	 * injection point
	 */
	private static final int ORDEREDLIST_INJECTION_POINT = 1;
	/**
	 * Used to identify that an <itemizedlist> should be generated for the
	 * injection point
	 */
	private static final int ITEMIZEDLIST_INJECTION_POINT = 2;
	/**
	 * Used to identify that an <xref> should be generated for the injection
	 * point
	 */
	private static final int XREF_INJECTION_POINT = 3;
	/**
	 * Used to identify that an <xref> should be generated for the injection
	 * point
	 */
	private static final int LIST_INJECTION_POINT = 4;
	/** Identifies a named regular expression group */
	private static final String TOPICIDS_RE_NAMED_GROUP = "TopicIDs";
	/** This text identifies an option task in a list */
	private static final String OPTIONAL_MARKER = "OPT:";
	/** The text to be prefixed to a list item if a topic is optional */
	private static final String OPTIONAL_LIST_PREFIX = "Optional: ";
	/** A regular expression that identifies a topic id */
	private static final String OPTIONAL_TOPIC_ID_RE = "(" + OPTIONAL_MARKER + "\\s*)?\\d+";
	/** A regular expression that identifies a topic id */
	private static final String TOPIC_ID_RE = "\\d+";

	/**
	 * A regular expression that matches an InjectSequence custom injection
	 * point
	 */
	private static final String CUSTOM_INJECTION_SEQUENCE_RE =
	/*
	 * start xml comment and 'InjectSequence:' surrounded by optional white
	 * space
	 */
	"\\s*InjectSequence:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	/** A regular expression that matches an InjectList custom injection point */
	private static final String CUSTOM_INJECTION_LIST_RE =
	/* start xml comment and 'InjectList:' surrounded by optional white space */
	"\\s*InjectList:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	private static final String CUSTOM_INJECTION_LISTITEMS_RE =
	/* start xml comment and 'InjectList:' surrounded by optional white space */
	"\\s*InjectListItems:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	private static final String CUSTOM_ALPHA_SORT_INJECTION_LIST_RE =
	/*
	 * start xml comment and 'InjectListAlphaSort:' surrounded by optional white
	 * space
	 */
	"\\s*InjectListAlphaSort:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + OPTIONAL_TOPIC_ID_RE + "\\s*,)*(\\s*" + OPTIONAL_TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	/** A regular expression that matches an Inject custom injection point */
	private static final String CUSTOM_INJECTION_SINGLE_RE =
	/* start xml comment and 'Inject:' surrounded by optional white space */
	"\\s*Inject:\\s*" +
	/* one digit block */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + OPTIONAL_TOPIC_ID_RE + "))" +
	/* xml comment end */
	"\\s*";

	/** A regular expression that matches an Inject Content Fragment */
	private static final String INJECT_CONTENT_FRAGMENT_RE =
	/* start xml comment and 'Inject:' surrounded by optional white space */
	"\\s*InjectText:\\s*" +
	/* one digit block */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
	/* xml comment end */
	"\\s*";

	/** A regular expression that matches an Inject Content Fragment */
	private static final String INJECT_TITLE_FRAGMENT_RE =
	/* start xml comment and 'Inject:' surrounded by optional white space */
	"\\s*InjectTitle:\\s*" +
	/* one digit block */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
	/* xml comment end */
	"\\s*";

	/**
	 * The noinject value for the role attribute indicates that an element
	 * should not be included in the Topic Fragment
	 */
	private static final String NO_INJECT_ROLE = "noinject";

	/**
	 * Takes a comma separated list of ints, and returns an array of Integers.
	 * This is used when processing custom injection points.
	 */
	private static List<InjectionTopicData> processTopicIdList(final String list)
	{
		/* find the individual topic ids */
		final String[] topicIDs = list.split(",");

		List<InjectionTopicData> retValue = new ArrayList<InjectionTopicData>(topicIDs.length);

		/* clean the topic ids */
		for (int i = 0; i < topicIDs.length; ++i)
		{
			final String topicId = topicIDs[i].replaceAll(OPTIONAL_MARKER, "").trim();
			final boolean optional = topicIDs[i].indexOf(OPTIONAL_MARKER) != -1;

			try
			{
				final InjectionTopicData topicData = new InjectionTopicData(Integer.parseInt(topicId), optional);
				retValue.add(topicData);
			}
			catch (final Exception ex)
			{
				/*
				 * these lists are discovered by a regular expression so we
				 * shouldn't have any trouble here with Integer.parse
				 */
				SkynetExceptionUtilities.handleException(ex);
				retValue.add(new InjectionTopicData(-1, false));
			}
		}

		return retValue;
	}

	public static String processInjections(final boolean internal, final Topic topic, final ArrayList<Integer> customInjectionIds, final Document xmlDocument, final TocTopicDatabase database, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/*
		 * this collection keeps a track of the injection point markers and the
		 * docbook lists that we will be replacing them with
		 */
		final HashMap<Node, InjectionListData> customInjections = new HashMap<Node, InjectionListData>();

		final List<Integer> errorTopics = new ArrayList<Integer>();

		errorTopics.addAll(processInjections(internal, topic, customInjectionIds, customInjections, ORDEREDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SEQUENCE_RE, null, database, docbookBuildingOptions));
		errorTopics.addAll(processInjections(internal, topic, customInjectionIds, customInjections, XREF_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SINGLE_RE, null, database, docbookBuildingOptions));
		errorTopics.addAll(processInjections(internal, topic, customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LIST_RE, null, database, docbookBuildingOptions));
		errorTopics.addAll(processInjections(internal, topic, customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_ALPHA_SORT_INJECTION_LIST_RE, new TopicTitleSorter(), database, docbookBuildingOptions));
		errorTopics.addAll(processInjections(internal, topic, customInjectionIds, customInjections, LIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LISTITEMS_RE, null, database, docbookBuildingOptions));

		if (errorTopics.size() != 0)
		{
			String errorList = "";
			for (final Integer topicId : errorTopics)
			{
				if (errorList.length() != 0)
					errorList += ", ";
				errorList += topicId.toString();
			}
			return errorList;
		}

		/* now make the custom injection point substitutions */
		for (final Node customInjectionCommentNode : customInjections.keySet())
		{
			final InjectionListData injectionListData = customInjections.get(customInjectionCommentNode);
			List<Element> list = null;

			/*
			 * this may not be true if we are not building all related topics
			 */
			if (injectionListData.listItems.size() != 0)
			{
				if (injectionListData.listType == ORDEREDLIST_INJECTION_POINT)
				{
					list = DocbookUtils.wrapOrderedListItemsInPara(xmlDocument, injectionListData.listItems);
				}
				else if (injectionListData.listType == XREF_INJECTION_POINT)
				{
					list = injectionListData.listItems.get(0);
				}
				else if (injectionListData.listType == ITEMIZEDLIST_INJECTION_POINT)
				{
					list = DocbookUtils.wrapItemizedListItemsInPara(xmlDocument, injectionListData.listItems);
				}
				else if (injectionListData.listType == LIST_INJECTION_POINT)
				{
					list = DocbookUtils.wrapItemsInListItems(xmlDocument, injectionListData.listItems);
				}
			}

			if (list != null)
			{
				for (final Element element : list)
				{
					customInjectionCommentNode.getParentNode().insertBefore(element, customInjectionCommentNode);
				}

				customInjectionCommentNode.getParentNode().removeChild(customInjectionCommentNode);
			}
		}

		return "";
	}

	public static List<Integer> processInjections(final boolean internal, final Topic topic, final ArrayList<Integer> customInjectionIds, final HashMap<Node, InjectionListData> customInjections, final int injectionPointType, final Document xmlDocument, final String regularExpression,
			final ExternalListSort<Integer, Topic, InjectionTopicData> sortComparator, final TocTopicDatabase database, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final List<Integer> retValue = new ArrayList<Integer>();

		if (xmlDocument == null)
			return retValue;

		/* loop over all of the comments in the document */
		for (final Node comment : XMLUtilities.getComments(xmlDocument))
		{
			final String commentContent = comment.getNodeValue();

			/* compile the regular expression */
			final NamedPattern injectionSequencePattern = NamedPattern.compile(regularExpression);
			/* find any matches */
			final NamedMatcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);

			/* loop over the regular expression matches */
			while (injectionSequencematcher.find())
			{
				/*
				 * get the list of topics from the named group in the regular
				 * expression match
				 */
				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);

				/* make sure we actually found a matching named group */
				if (reMatch != null)
				{
					/* get the sequence of ids */
					final List<InjectionTopicData> sequenceIDs = processTopicIdList(reMatch);

					/*
					 * add the related topic into a TocTopicDatabase for
					 * convenience
					 */
					final List<Topic> relatedTopics = topic.getOutgoingTopicsArray();
					TocTopicDatabase referencedTopicsDatabase = database;
					if (referencedTopicsDatabase == null)
					{
						referencedTopicsDatabase = new TocTopicDatabase();
						referencedTopicsDatabase.setTopics(relatedTopics);
					}

					/* sort the InjectionTopicData list of required */
					if (sortComparator != null)
					{
						sortComparator.sort(relatedTopics, sequenceIDs);
					}

					/* loop over all the topic ids in the injection point */
					for (final InjectionTopicData sequenceID : sequenceIDs)
					{
						/*
						 * topics that are injected into custom injection points
						 * are excluded from the generic related topic lists at
						 * the beginning and end of a topic. adding the topic id
						 * here means that when it comes time to generate the
						 * generic related topic lists, we can skip this topic
						 */
						customInjectionIds.add(sequenceID.topicId);

						/*
						 * Pull the topic out of either the main or the
						 * reference database
						 */
						final Topic relatedTopic = referencedTopicsDatabase.getTopic(sequenceID.topicId);

						/*
						 * it is possible that the topic id referenced in the
						 * injection point h as not been related, and therefore
						 * is not in the referencedTopicsDatabase
						 */
						if (relatedTopic != null)
						{
							/*
							 * build our list
							 */
							List<List<Element>> list = new ArrayList<List<Element>>();

							/*
							 * each related topic is added to a string, which is
							 * stored in the customInjections collection. the
							 * customInjections key is the custom injection text
							 * from the source xml. this allows us to match the
							 * xrefs we are generating for the related topic
							 * with the text in the xml file that these xrefs
							 * will eventually replace
							 */
							if (customInjections.containsKey(comment))
								list = customInjections.get(comment).listItems;

							/* wrap the xref up in a listitem */
							final String url = getURLToInternalTopic(relatedTopic.getTopicId());
							if (sequenceID.optional)
							{
								if (internal)
									list.add(DocbookUtils.buildEmphasisPrefixedULink(xmlDocument, OPTIONAL_LIST_PREFIX, url, relatedTopic.getTopicTitle()));
								else
									list.add(DocbookUtils.buildEmphasisPrefixedXRef(xmlDocument, OPTIONAL_LIST_PREFIX, relatedTopic.getXRefID()));
							}
							else
							{
								if (internal)
									list.add(DocbookUtils.buildULink(xmlDocument, url, relatedTopic.getTopicTitle()));
								else
									list.add(DocbookUtils.buildXRef(xmlDocument, relatedTopic.getXRefID()));
							}

							/*
							 * save the changes back into the customInjections
							 * collection
							 */
							customInjections.put(comment, new InjectionListData(list, injectionPointType));
						}
						else if (docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
						{
							retValue.add(sequenceID.topicId);
						}
					}
				}
			}
		}

		return retValue;
	}

	public static String processGenericInjections(final boolean internal, final Topic topic, final Document xmlDocument, final ArrayList<Integer> customInjectionIds, final List<Pair<Integer, String>> topicTypeTagIDs, final TocTopicDatabase database, final DocbookBuildingOptions docbookBuildingOptions)
	{

		if (xmlDocument == null)
			return "";

		final List<Integer> errors = new ArrayList<Integer>();

		/*
		 * this collection will hold the lists of related topics
		 */
		final GenericInjectionPointDatabase relatedLists = new GenericInjectionPointDatabase();

		/* wrap each related topic in a listitem tag */
		for (final Topic relatedTopic : topic.getOutgoingTopicsArray())
		{
			/* make sure the topic is available to be linked to */
			if (database != null && database.getTopic(relatedTopic.getTopicId()) == null)
			{
				if ((docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections()))
					errors.add(relatedTopic.getTopicId());
			}
			else
			{
				/*
				 * don't process those topics that were injected into custom
				 * injection points
				 */
				if (!customInjectionIds.contains(relatedTopic.getTopicId()))
				{
					// loop through the topic type tags
					for (final Pair<Integer, String> primaryTopicTypeTag : topicTypeTagIDs)
					{
						/*
						 * see if we have processed a related topic with one of
						 * the topic type tags this may never be true if not
						 * processing all related topics
						 */
						if (relatedTopic.isTaggedWith(primaryTopicTypeTag.getFirst()))
						{
							relatedLists.addInjectionTopic(primaryTopicTypeTag, relatedTopic);

							break;
						}
					}
				}
			}
		}

		insertGenericInjectionLinks(internal, xmlDocument, relatedLists, database, docbookBuildingOptions);

		if (errors.size() != 0)
		{
			String retValue = "";
			for (final Integer error : errors)
			{
				if (retValue.length() != 0)
					retValue += ", ";
				retValue += error.toString();
			}
			return retValue;
		}

		return "";
	}

	/**
	 * The generic injection points are placed in well defined locations within
	 * a topics xml structure. This function takes the list of related topics
	 * and the topic type tags that are associated with them and injects them
	 * into the xml document.
	 */
	private static void insertGenericInjectionLinks(final boolean internal, final Document xmlDoc, final GenericInjectionPointDatabase relatedLists, final TocTopicDatabase database, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/* all related topics are placed before the first simplesect */
		final NodeList nodes = xmlDoc.getDocumentElement().getChildNodes();
		Node simplesectNode = null;
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			final Node node = nodes.item(i);
			if (node.getNodeType() == 1 && node.getNodeName().equals("simplesect"))
			{
				simplesectNode = node;
				break;
			}
		}

		/*
		 * place the topics at the end of the topic. They will appear in the
		 * reverse order as the call to toArrayList()
		 */
		for (final Integer topTag : CollectionUtilities.toArrayList(Constants.REFERENCE_TAG_ID, Constants.TASK_TAG_ID, Constants.CONCEPT_TAG_ID, Constants.CONCEPTUALOVERVIEW_TAG_ID))
		{
			for (final GenericInjectionPoint genericInjectionPoint : relatedLists.getInjectionPoints())
			{
				if (genericInjectionPoint.getCategoryIDAndName().getFirst() == topTag)
				{
					final List<Topic> relatedTopics = genericInjectionPoint.getTopics();

					/* don't add an empty list */
					if (relatedTopics.size() != 0)
					{
						final Node itemizedlist = DocbookUtils.createRelatedTopicItemizedList(xmlDoc, "Related " + genericInjectionPoint.getCategoryIDAndName().getSecond() + "s");

						Collections.sort(relatedTopics, new TopicTitleComparator());

						for (final Topic relatedTopic : relatedTopics)
						{
							if (internal)
								DocbookUtils.createRelatedTopicULink(xmlDoc, getURLToInternalTopic(relatedTopic.getTopicId()), relatedTopic.getTopicTitle(), itemizedlist);
							else
								DocbookUtils.createRelatedTopicXRef(xmlDoc, relatedTopic.getXRefID(), itemizedlist);

						}

						if (simplesectNode != null)
							xmlDoc.getDocumentElement().insertBefore(itemizedlist, simplesectNode);
						else
							xmlDoc.getDocumentElement().appendChild(itemizedlist);
					}
				}
			}
		}
	}

	private static String getURLToInternalTopic(final Integer topicId)
	{
		return "Topic.seam?topicTopicId=" + topicId + "&selectedTab=Rendered+View";
	}

	public static void processInternalImageFiles(final Document xmlDoc)
	{
		if (xmlDoc == null)
			return;

		final List<Node> imageDataNodes = XMLUtilities.getNodes(xmlDoc.getDocumentElement(), "imagedata");
		for (final Node imageDataNode : imageDataNodes)
		{
			final NamedNodeMap attributes = imageDataNode.getAttributes();
			final Node filerefAttribute = attributes.getNamedItem("fileref");
			if (filerefAttribute != null)
			{
				String imageId = filerefAttribute.getTextContent();
				imageId = imageId.replace("images/", "");
				final int periodIndex = imageId.lastIndexOf(".");
				if (periodIndex != -1)
					imageId = imageId.substring(0, periodIndex);

				/*
				 * at this point imageId should be an integer that is the id of
				 * the image uploaded in skynet. We will leave the validation of
				 * imageId to the ImageFileDisplay class.
				 */

				filerefAttribute.setTextContent("ImageFileDisplay.seam?imageFileId=" + imageId);
			}
		}
	}

	public static String processTopicContentFragments(final Topic topic, final Document xmlDocument, final DocbookBuildingOptions docbookBuildingOptions)
	{
		if (xmlDocument == null)
			return "";

		String injectionErrors = "";

		final Map<Node, ArrayList<Node>> replacements = new HashMap<Node, ArrayList<Node>>();

		/* loop over all of the comments in the document */
		for (final Node comment : XMLUtilities.getComments(xmlDocument))
		{
			final String commentContent = comment.getNodeValue();

			/* compile the regular expression */
			final NamedPattern injectionSequencePattern = NamedPattern.compile(INJECT_CONTENT_FRAGMENT_RE);
			/* find any matches */
			final NamedMatcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);

			/* loop over the regular expression matches */
			while (injectionSequencematcher.find())
			{
				/*
				 * get the list of topics from the named group in the regular
				 * expression match
				 */
				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);

				/* make sure we actually found a matching named group */
				if (reMatch != null)
				{
					try
					{
						if (!replacements.containsKey(comment))
							replacements.put(comment, new ArrayList<Node>());

						final Integer topicID = Integer.parseInt(reMatch);

						/*
						 * make sure the topic we are trying to inject has been
						 * related
						 */
						if (topic.isRelatedTo(topicID))
						{
							final Topic relatedTopic = topic.getRelatedTopicByID(topicID);
							final Document relatedTopicXML = XMLUtilities.convertStringToDocument(relatedTopic.getTopicXML());
							if (relatedTopicXML != null)
							{
								final Node relatedTopicDocumentElement = relatedTopicXML.getDocumentElement();
								final Node importedXML = xmlDocument.importNode(relatedTopicDocumentElement, true);

								/* ignore the section title */
								final NodeList sectionChildren = importedXML.getChildNodes();
								for (int i = 0; i < sectionChildren.getLength(); ++i)
								{
									final Node node = sectionChildren.item(i);
									if (node.getNodeName().equals("title"))
									{
										importedXML.removeChild(node);
										break;
									}
								}

								/* remove all with a role="noinject" attribute */
								removeNoInjectElements(importedXML);

								/*
								 * importedXML is a now section with no title,
								 * and no child elements with the noinject value
								 * on the role attribute. We now add its
								 * children to the Array in the replacements
								 * Map.
								 */

								final NodeList remainingChildren = importedXML.getChildNodes();
								for (int i = 0; i < remainingChildren.getLength(); ++i)
								{
									final Node child = remainingChildren.item(i);
									replacements.get(comment).add(child);
								}
							}
						}
						else if (docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
						{
							injectionErrors += reMatch + ", ";
						}
					}
					catch (final Exception ex)
					{
						SkynetExceptionUtilities.handleException(ex);
					}
				}
			}
		}

		if (injectionErrors.length() != 0)
			return injectionErrors;

		/*
		 * The replacements map now has a keyset of the comments mapped to a
		 * collection of nodes that the comment will be replaced with
		 */

		for (final Node comment : replacements.keySet())
		{
			final ArrayList<Node> replacementNodes = replacements.get(comment);
			for (final Node replacementNode : replacementNodes)
				comment.getParentNode().insertBefore(replacementNode, comment);
			comment.getParentNode().removeChild(comment);
		}

		return "";
	}

	private static void removeNoInjectElements(final Node parent)
	{
		final NodeList childrenNodes = parent.getChildNodes();
		final ArrayList<Node> removeNodes = new ArrayList<Node>();

		for (int i = 0; i < childrenNodes.getLength(); ++i)
		{
			final Node node = childrenNodes.item(i);
			final NamedNodeMap attributes = node.getAttributes();
			if (attributes != null)
			{
				final Node roleAttribute = attributes.getNamedItem("role");
				if (roleAttribute != null)
				{
					final String[] roles = roleAttribute.getTextContent().split(",");
					for (final String role : roles)
					{
						if (role.equals(NO_INJECT_ROLE))
						{
							removeNodes.add(node);
							break;
						}
					}
				}
			}
		}

		for (final Node removeNode : removeNodes)
			parent.removeChild(removeNode);

		final NodeList remainingChildrenNodes = parent.getChildNodes();

		for (int i = 0; i < remainingChildrenNodes.getLength(); ++i)
		{
			final Node child = remainingChildrenNodes.item(i);
			removeNoInjectElements(child);
		}
	}

	public static String processTopicTitleFragments(final Topic topic, final Document xmlDocument, final DocbookBuildingOptions docbookBuildingOptions)
	{
		if (xmlDocument == null)
			return "";

		String injectionErrors = "";

		final Map<Node, Node> replacements = new HashMap<Node, Node>();

		/* loop over all of the comments in the document */
		for (final Node comment : XMLUtilities.getComments(xmlDocument))
		{
			final String commentContent = comment.getNodeValue();

			/* compile the regular expression */
			final NamedPattern injectionSequencePattern = NamedPattern.compile(INJECT_TITLE_FRAGMENT_RE);
			/* find any matches */
			final NamedMatcher injectionSequencematcher = injectionSequencePattern.matcher(commentContent);

			/* loop over the regular expression matches */
			while (injectionSequencematcher.find())
			{
				/*
				 * get the list of topics from the named group in the regular
				 * expression match
				 */
				final String reMatch = injectionSequencematcher.group(TOPICIDS_RE_NAMED_GROUP);

				/* make sure we actually found a matching named group */
				if (reMatch != null)
				{
					try
					{
						if (!replacements.containsKey(comment))
							replacements.put(comment, null);

						final Integer topicID = Integer.parseInt(reMatch);

						/*
						 * make sure the topic we are trying to inject has been
						 * related
						 */
						if (topic.isRelatedTo(topicID))
						{
							final Topic relatedTopic = topic.getRelatedTopicByID(topicID);
							final Element titleNode = xmlDocument.createElement("title");
							titleNode.setTextContent(relatedTopic.getTopicTitle());
							replacements.put(comment, titleNode);
						}
						else if (docbookBuildingOptions != null && !docbookBuildingOptions.getIgnoreMissingCustomInjections())
						{
							injectionErrors += reMatch + ", ";
						}
					}
					catch (final Exception ex)
					{
						SkynetExceptionUtilities.handleException(ex);
					}
				}
			}
		}

		if (injectionErrors.length() != 0)
			return injectionErrors;

		/* swap the comment nodes with the new title nodes */
		for (final Node comment : replacements.keySet())
		{
			final Node title = replacements.get(comment);
			comment.getParentNode().insertBefore(title, comment);
			comment.getParentNode().removeChild(comment);
		}

		return "";
	}

	public static String processDocumentType(final String xml)
	{
		assert xml != null : "The xml parameter can not be null";

		if (XMLUtilities.findDocumentType(xml) == null)
		{
			final String preamble = XMLUtilities.findPreamble(xml);
			final String fixedPreamble = preamble == null ? "" : preamble + "\n";
			final String fixedXML = preamble == null ? xml : xml.replace(preamble, "");

			return fixedPreamble + "<!DOCTYPE section PUBLIC \"-//OASIS//DTD DocBook XML V4.5//EN\" \"http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd\" []>\n" + fixedXML;
		}

		return xml;
	}

	
}
