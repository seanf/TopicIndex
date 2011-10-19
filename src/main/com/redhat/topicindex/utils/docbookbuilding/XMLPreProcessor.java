package com.redhat.topicindex.utils.docbookbuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.sort.ExternalListSort;
import com.redhat.topicindex.sort.TopicTitleSorter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.docbookbuilding.tocdatabase.TocTopicDatabase;

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
	private static final String TOPIC_ID_RE = "(" + OPTIONAL_MARKER + "\\s*)?\\d+";
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
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
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
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	private static final String CUSTOM_INJECTION_LISTITEMS_RE =
	/* start xml comment and 'InjectList:' surrounded by optional white space */
	"\\s*InjectListItems:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
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
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	/** A regular expression that matches an Inject custom injection point */
	private static final String CUSTOM_INJECTION_SINGLE_RE =
	/* start xml comment and 'Inject:' surrounded by optional white space */
	"\\s*Inject:\\s*" +
	/* one digit block */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
	/* xml comment end */
	"\\s*";

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
				ExceptionUtilities.handleException(ex);
				retValue.add(new InjectionTopicData(-1, false));
			}
		}

		return retValue;
	}

	public static void processInternalInjections(final ArrayList<Integer> customInjectionIds, final Document xmlDocument)
	{
		/*
		 * this collection keeps a track of the injection point markers
		 * and the docbook lists that we will be replacing them with
		 */
		final HashMap<Node, InjectionListData> customInjections = new HashMap<Node, InjectionListData>();
		
		processInternalInjections(customInjectionIds, customInjections, ORDEREDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SEQUENCE_RE, null);
		processInternalInjections(customInjectionIds, customInjections, XREF_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_SINGLE_RE, null);
		processInternalInjections(customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LIST_RE, null);
		processInternalInjections(customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, xmlDocument, CUSTOM_ALPHA_SORT_INJECTION_LIST_RE, new TopicTitleSorter());
		processInternalInjections(customInjectionIds, customInjections, LIST_INJECTION_POINT, xmlDocument, CUSTOM_INJECTION_LISTITEMS_RE, null);
		
		/* now make the custom injection point substitutions */
		for (final Node customInjectionCommentNode : customInjections.keySet())
		{
			final InjectionListData injectionListData = customInjections.get(customInjectionCommentNode);
			List<Element> list = null;

			/*
			 * this may not be true if we are not building all
			 * related topics
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
	}

	public static void processInternalInjections(final ArrayList<Integer> customInjectionIds, final HashMap<Node, InjectionListData> customInjections, final int injectionPointType, final Document xmlDocument, final String regularExpression, final ExternalListSort<Integer, Topic, InjectionTopicData> sortComparator)
	{
		if (xmlDocument == null)
			return;

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

					/* get a comma separated list of topic ids */
					String referencedTopicIds = "";
					for (final InjectionTopicData injectionTopicData : sequenceIDs)
					{
						if (referencedTopicIds.length() != 0)
							referencedTopicIds += referencedTopicIds + ",";
						referencedTopicIds += injectionTopicData.topicId.toString();
					}

					/* build a query that will return the referenced topics */
					final Filter filter = new Filter();
					final FilterField field = new FilterField();
					field.setField(Constants.TOPIC_IDS_FILTER_VAR);
					field.setValue(referencedTopicIds);
					filter.getFilterFields().add(field);
					final String query = filter.buildQuery();

					/*
					 * create a collection of the topics referenced by this
					 * injection point
					 */
					final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
					final List<Topic> referencedTopics = entityManager.createQuery(Topic.SELECT_ALL_QUERY + query).getResultList();
					final TocTopicDatabase referencedTopicsDatabase = new TocTopicDatabase();
					referencedTopicsDatabase.setTopics(referencedTopics);

					/* sort the InjectionTopicData list of required */
					if (sortComparator != null)
					{
						sortComparator.sort(referencedTopics, sequenceIDs);
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
						 * build our list
						 */
						List<List<Element>> list = new ArrayList<List<Element>>();

						/*
						 * each related topic is added to a string, which is
						 * stored in the customInjections collection. the
						 * customInjections key is the custom injection text
						 * from the source xml. this allows us to match the
						 * xrefs we are generating for the related topic with
						 * the text in the xml file that these xrefs will
						 * eventually replace
						 */
						if (customInjections.containsKey(comment))
							list = customInjections.get(comment).listItems;

						/*
						 * Pull the topic out of either the main or the
						 * reference database
						 */
						final Topic relatedTopic = referencedTopicsDatabase.getTopic(sequenceID.topicId);

						/* wrap the xref up in a listitem */
						final String url = "Topic.seam?topicTopicId=" + relatedTopic.getTopicId() + "&selectedTab=Rendered+View";
						if (sequenceID.optional)
						{
							list.add(DocbookUtils.buildEmphasisPrefixedULink(xmlDocument, OPTIONAL_LIST_PREFIX, url, relatedTopic.getTopicTitle()));
						}
						else
						{
							list.add(DocbookUtils.buildULink(xmlDocument, url, relatedTopic.getTopicTitle()));
						}

						/*
						 * save the changes back into the customInjections
						 * collection
						 */
						customInjections.put(comment, new InjectionListData(list, injectionPointType));
					}
				}
			}
		}
	}
}
