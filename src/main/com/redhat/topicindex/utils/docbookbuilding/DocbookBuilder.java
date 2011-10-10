package com.redhat.topicindex.utils.docbookbuilding;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import ch.lambdaj.function.matcher.LambdaJMatcher;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.HTTPUtilities;
import com.redhat.ecs.commonutils.MIMEUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.entity.BlobConstants;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.entity.IntegerConstants;
import com.redhat.topicindex.entity.StringConstants;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToTag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTag;
import com.redhat.topicindex.entity.TopicToTopic;
import com.redhat.topicindex.sort.ExternalMapSort;
import com.redhat.topicindex.sort.TopicTitleSorter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.LineEndFormatter;
import com.redhat.topicindex.utils.XMLValidator;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocFolderElement;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocLink;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocTopLevel;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.TopicErrorData;
import com.redhat.topicindex.utils.structures.TopicImageData;

/**
 * This class is used to convert a collection of topics in a Publican Docbook
 * archive file that can be compiled into HTML
 */
public class DocbookBuilder
{
	/** The StringConstantsID that represents the Revision_History.xml file */
	protected static final Integer REVISION_HISTORY_XML_ID = 15;
	/** The StringConstantsID that represents the Book.xml file */
	protected static final Integer BOOK_XML_ID = 1;
	/** The StringConstantsID that represents the Book.ent file */
	protected static final Integer BOOK_ENT_ID = 2;
	/** The StringConstantsID that represents the Book_Info.xml file */
	protected static final Integer BOOK_INFO_XML_ID = 3;
	/** The StringConstantsID that represents the Author_Group.xml file */
	protected static final Integer AUTHOR_GROUP_XML_ID = 4;
	/** The StringConstantsID that represents the publican.cfg file */
	protected static final Integer PUBLICAN_CFG_ID = 5;
	/** The StringConstantsID that represents the icon.svg file */
	protected static final Integer ICON_SVG_ID = 6;
	/** The StringConstantsID that represents the error topic xml template */
	protected static final Integer ERROR_TOPIC_ID = 7;
	/** The StringConstantsID that represents the error topic xml template */
	protected static final Integer ERRORTAGS_TOPIC_ID = 8;
	/** The StringConstantsID that represents the error topic xml template */
	protected static final Integer MISSING_TOPIC_ID = 9;
	/** The StringConstantsID that represents the Makefile file */
	protected static final Integer MAKEFILE_ID = 16;
	/** The StringConstantsID that represents the spec.in file */
	protected static final Integer SPEC_IN_ID = 17;
	/** The StringConstantsID that represents the package.sh file */
	protected static final Integer PACKAGE_SH_ID = 18;
	/** The StringConstantsID that represents the StartPage.xml file */
	protected static final Integer START_PAGE_ID = 19;
	/** The StringConstantsID that represents the jboss.svg file */
	protected static final Integer JBOSS_SVG_ID = 20;
	/** The StringConstantsID that represents the yahoo_dom_event.js file */
	protected static final Integer YAHOO_DOM_EVENT_JS_ID = 21;
	/** The StringConstantsID that represents the treeview_min.js file */
	protected static final Integer TREEVIEW_MIN_JS_ID = 22;
	/** The StringConstantsID that represents the treeview.css file */
	protected static final Integer TREEVIEW_CSS_ID = 23;
	/** The StringConstantsID that represents the jquery_min.js file */
	protected static final Integer JQUERY_MIN_JS_ID = 24;

	protected static final Integer TREEVIEW_SPRITE_GIF_ID = 1;
	protected static final Integer TREEVIEW_LOADING_GIF_ID = 2;
	protected static final Integer CHECK1_GIF_ID = 3;
	protected static final Integer CHECK2_GIF_ID = 4;
	protected static final Integer FAILPENGUIN_PNG_ID = 5;

	protected static final Integer PLUGIN_XML_ID = 25;
	protected static final Integer ECLIPSE_PACKAGE_SH_ID = 26;
	protected static final Integer PUBLICAN_ECLIPSE_CFG_ID = 27;
	protected static final Integer NARRATIVE_PUBLICAN_CFG_ID = 28;

	/**
	 * The IntegerConstantsID that represents a tag that hides a topic from the
	 * navigation pages
	 */
	protected static final Integer INVISIBLE_TAG_ID = 1;

	/**
	 * The contents of the Revision_History.xml template, as pulled from the
	 * StringConstants table
	 */
	protected String revisionHistoryXml;
	/**
	 * The contents of the Book.xml template, as pulled from the StringConstants
	 * table
	 */
	protected String bookXml;
	/**
	 * The contents of the publican.cfg template, as pulled from the
	 * StringConstants table
	 */
	protected String publicanCfg;
	/**
	 * The contents of the Author_Group.xml template, as pulled from the
	 * StringConstants table
	 */
	protected String authorGroupXml;
	/**
	 * The contents of the Book_Info.xml template, as pulled from the
	 * StringConstants table
	 */
	protected String bookInfoXml;
	/**
	 * The contents of the Book.ent template, as pulled from the StringConstants
	 * table
	 */
	protected String bookEnt;
	/**
	 * The contents of the icon.svg template, as pulled from the StringConstants
	 * table
	 */
	protected String iconSvg;
	/**
	 * The contents of the error topic template, as pulled from the
	 * StringConstants table
	 */
	protected String errorTopic;
	/**
	 * The contents of the error topic (where some tags are missing) template,
	 * as pulled from the StringConstants table
	 */
	protected String errorTagsTopic;
	/**
	 * The contents of the missing topic (where the SVN URL is missing)
	 * template, as pulled from the StringConstants table
	 */
	protected String missingTopic;

	protected String makefile;
	protected String spec_in;
	protected String package_sh;
	protected String startPage;
	protected String jbossSvg;
	protected String yahooDomEventJs;
	protected String treeviewMinJs;
	protected String treeviewCss;
	protected String jqueryMinJs;
	protected byte[] treeviewSpriteGif;
	protected byte[] treeviewLoadingGif;
	protected byte[] check1Gif;
	protected byte[] check2Gif;
	protected byte[] failpenguinPng;

	protected String pluginXml;
	protected String eclisePackageSh;
	protected String publicanEclipseCfg;
	protected String narrativePublicanCfg;

	protected Integer invisibleTagID;

	/** A marker to replace with the date in a string */
	protected static final String DATE_YYMMDD_TEXT_MARKER = "#YYMMDD#";
	protected static final String BOOK_XML_XI_INCLUDES_MARKER = "#XI_INCLUDES#";

	/** This marker is replaced with the SkyNet build version */
	protected static final String BUILD_MARKER = "#SKYNETBUILD#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String ROLE_MARKER = "#ROLE#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String TAG_FILTER_URL_MARKER = "#TAGFILTERURL#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String TOPIC_ID_MARKER = "#TOPICID#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String TOPIC_ERROR_LINK_MARKER = "#TOPICERRORLINK#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String XREF_ID_MARKER = "#XREF#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String TOPIC_TITLE_MARKER = "#TOPICTITLE#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	protected static final String TOPIC_TEXT_MARKER = "#TOPICTEXT#";
	/**
	 * Identifies the beginning of the string that references an image in
	 * docbook
	 */
	protected static final String IMAGE_START = "<imagedata fileref=\"";
	/** Identifies the end of the string that references an image in docbook */
	protected static final String IMAGE_END = "\"";

	/*
	 * These constants define the docbook tags that are used to wrap up xrefs in
	 * custom injection points
	 */

	/**
	 * Used to identify that an <orderedlist> should be generated for the
	 * injection point
	 */
	protected static final int ORDEREDLIST_INJECTION_POINT = 1;
	/**
	 * Used to identify that an <itemizedlist> should be generated for the
	 * injection point
	 */
	protected static final int ITEMIZEDLIST_INJECTION_POINT = 2;
	/**
	 * Used to identify that an <xref> should be generated for the injection
	 * point
	 */
	protected static final int XREF_INJECTION_POINT = 3;
	/**
	 * Used to identify that an <xref> should be generated for the injection
	 * point
	 */
	protected static final int LIST_INJECTION_POINT = 4;

	/** Defines how many related tasks to show on the nav page */
	protected static final int MAX_RELATED_TASKS = 5;

	/*
	 * These regular expressions define the format of the custom injection
	 * points
	 */

	/** Identifies a named regular expression group */
	protected static final String TOPICIDS_RE_NAMED_GROUP = "TopicIDs";
	/** This text identifies an option task in a list */
	protected static final String OPTIONAL_MARKER = "OPT:";
	/** The text to be prefixed to a list item if a topic is optional */
	protected static final String OPTIONAL_LIST_PREFIX = "Optional: ";
	/** A regular expression that identifies a topic id */
	protected static final String TOPIC_ID_RE = "(" + OPTIONAL_MARKER + "\\s*)?\\d+";

	/**
	 * A regular expression that matches an InjectSequence custom injection
	 * point
	 */
	protected static final String CUSTOM_INJECTION_SEQUENCE_RE =
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
	protected static final String CUSTOM_INJECTION_LIST_RE =
	/* start xml comment and 'InjectList:' surrounded by optional white space */
	"\\s*InjectList:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	protected static final String CUSTOM_INJECTION_LISTITEMS_RE =
	/* start xml comment and 'InjectList:' surrounded by optional white space */
	"\\s*InjectListItems:\\s*" +
	/*
	 * an optional comma separated list of digit blocks, and at least one digit
	 * block with an optional comma
	 */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(\\s*" + TOPIC_ID_RE + "\\s*,)*(\\s*" + TOPIC_ID_RE + ",?))" +
	/* xml comment end */
	"\\s*";

	protected static final String CUSTOM_ALPHA_SORT_INJECTION_LIST_RE =
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
	protected static final String CUSTOM_INJECTION_SINGLE_RE =
	/* start xml comment and 'Inject:' surrounded by optional white space */
	"\\s*Inject:\\s*" +
	/* one digit block */
	"(?<" + TOPICIDS_RE_NAMED_GROUP + ">(" + TOPIC_ID_RE + "))" +
	/* xml comment end */
	"\\s*";

	/**
	 * The Docbook role (which becomes a CSS class) for the Skynet build version
	 * para
	 */
	protected static final String ROLE_BUILD_VERSION_PARA = "RoleBuildVersionPara";
	/**
	 * The Docbook role (which becomes a CSS class) for the Skynet topic link
	 * para
	 */
	protected static final String ROLE_VIEW_IN_SKYNET_PARA = "RoleViewInSkyNetPara";
	/** The Docbook role (which becomes a CSS class) for the bug link para */
	protected static final String ROLE_CREATE_BUG_PARA = "RoleCreateBugPara";
	/** The Docbook role (which becomes a CSS class) for the survey link para */
	protected static final String ROLE_SURVEY_PARA = "RoleCreateBugPara";

	/*
	 * The collections below take the topics that are to be included in the
	 * docbook build and provide an easy way to reference them by the tags that
	 * are applied to the topics, and by the topic id's. In this way we can look
	 * up topics by tag and by id.
	 */

	/** tag to topic map */
	protected HashMap<Integer, HashMap<Integer, Topic>> tagIdToTopicMap = new HashMap<Integer, HashMap<Integer, Topic>>();
	/** topic id to topic map */
	protected HashMap<Integer, Topic> topicIdToTopicMap = new HashMap<Integer, Topic>();
	/** tag id to final compiled docbook */
	protected HashMap<Integer, String> tagIdToDocbookMap = new HashMap<Integer, String>();

	/**
	 * Holds the compiler errors that form the Errors.xml file in the compiled
	 * docbook
	 */
	protected Map<Topic, TopicErrorData> errors = new HashMap<Topic, TopicErrorData>();

	/**
	 * Holds information on file url locations, which will be downloaded and
	 * included in the docbook zip file
	 */
	protected ArrayList<TopicImageData> imageLocations = new ArrayList<TopicImageData>();

	/**
	 * Takes a comma separated list of ints, and returns an array of Integers.
	 * This is used when processing custom injection points.
	 */
	protected List<InjectionTopicData> processTopicIdList(final String list)
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

	protected List<Node> getImages(final Node node)
	{
		final List<Node> images = new ArrayList<Node>();
		final NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i)
		{
			final Node child = children.item(i);

			if (child.getNodeName().equals("imagedata"))
			{
				images.add(child);
			}
			else
			{
				images.addAll(getImages(child));
			}
		}
		return images;
	}

	/**
	 * Searches the given XML for an injection point, as defined by the
	 * regularExpression parameter. Those topics listed in the injection point
	 * are processed, and the injection point marker (i.e. the xml comment) is
	 * replaced with a list of xrefs.
	 * 
	 * @return a list of topics that were injected but not related
	 */
	protected String processCustomInjectionPoints(final ArrayList<Integer> customInjectionIds, final HashMap<Node, InjectionListData> customInjections, final int injectionPointType, final Integer topidID, final Topic topicData, final String regularExpression,
			final ExternalMapSort<Integer, Topic, InjectionTopicData> sortComparator, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final Document xmlDocument = topicData.getTempTopicXMLDoc();

		String injectionErrors = "";

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

					/* sort the InjectionTopicData list of required */
					if (sortComparator != null)
						sortComparator.sort(topicIdToTopicMap, sequenceIDs);

					/* loop over all the topic ids in the injection point */
					for (final InjectionTopicData sequenceID : sequenceIDs)
					{
						/*
						 * right now it is possible to list a topic in an
						 * injection point without it being related in the
						 * database. if that is the case, we will report an
						 * error
						 */
						boolean foundSequenceID = false;

						/*
						 * look for the topic mentioned in the injection point
						 * in the list of related topics
						 */
						if (topicData.getRelatedTopicIDs().contains(sequenceID.topicId) && topicIdToTopicMap.containsKey(sequenceID.topicId))
						{
							/*
							 * this injected topic is also related, so we don't
							 * need to generate an error
							 */
							foundSequenceID = true;

							/*
							 * topics that are injected into custom injection
							 * points are excluded from the generic related
							 * topic lists at the beginning and end of a topic.
							 * adding the topic id here means that when it comes
							 * time to generate the generic related topic lists,
							 * we can skip this topic
							 */
							customInjectionIds.add(sequenceID.topicId);

							/*
							 * we have found the related topic, so build our
							 * list
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
							if (sequenceID.optional)
							{
								list.add(DocbookUtils.buildEmphasisPrefixedXRef(xmlDocument, OPTIONAL_LIST_PREFIX, topicIdToTopicMap.get(sequenceID.topicId).getXRefID()));
							}
							else
							{
								list.add(DocbookUtils.buildXRef(xmlDocument, topicIdToTopicMap.get(sequenceID.topicId).getXRefID()));
							}

							/*
							 * save the changes back into the customInjections
							 * collection
							 */
							customInjections.put(comment, new InjectionListData(list, injectionPointType));
						}

						if (!foundSequenceID && !docbookBuildingOptions.isIgnoreMissingCustomInjections())
						{
							/*
							 * the topic referenced in the custom injection
							 * point was not related in the database, so report
							 * an error
							 */
							injectionErrors += sequenceID.topicId + ", ";
						}
					}
				}
			}
		}

		return injectionErrors;

	}

	/**
	 * This function takes the list of topics and their related topics and
	 * injects xrefs
	 */
	protected void processInjections(final List<TagToCategory> topicTypeTagIDs, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		System.out.println("Processing injection points");

		/*
		 * this defines how many injection points are processed before a new
		 * status update is printed to the output
		 */
		final int progressPoints = 50;
		int currentProgressPoints = progressPoints;

		/* loop through each topic, and build up the list of related topics */
		int categoryProgress = 0;
		final int categoryTotal = topicTypeTagIDs.size();

		for (final TagToCategory topicTypeTag : topicTypeTagIDs)
		{
			if (tagIdToTopicMap.containsKey(topicTypeTag.getTag().getTagId()))
			{
				/*
				 * loop over the topics that have one of the type tags assigned
				 * to it
				 */
				final HashMap<Integer, Topic> topics = tagIdToTopicMap.get(topicTypeTag.getTag().getTagId());

				int topicProgress = 0;
				final int topicSize = topics.keySet().size();

				for (final Integer topicID : topics.keySet())
				{
					/* display a progress notification */
					--currentProgressPoints;
					if (currentProgressPoints <= 0)
					{
						currentProgressPoints = progressPoints;
						final float progress = (((float) categoryProgress / (float) categoryTotal) + ((float) topicProgress / (float) topicSize / (float) categoryTotal)) * 100.0f;
						System.out.println("Processing injection points " + Math.round(progress) + "% done");
					}

					final Topic topic = topics.get(topicID);

					/*
					 * don't bother processing injection points for invalid
					 * topics
					 */
					if (!topic.isTempInvalidTopic())
					{
						/***************** PROCESS CUSTOM INJECTION POINTS *****************/

						/*
						 * keep a track of the topics we inject into custom
						 * locations, so we don't then inject them again
						 */
						final ArrayList<Integer> customInjectionIds = new ArrayList<Integer>();

						/*
						 * this collection keeps a track of the injection point
						 * markers and the docbook lists that we will be
						 * replacing them with
						 */
						final HashMap<Node, InjectionListData> customInjections = new HashMap<Node, InjectionListData>();

						String injectionErrors = processCustomInjectionPoints(customInjectionIds, customInjections, ORDEREDLIST_INJECTION_POINT, topicID, topic, CUSTOM_INJECTION_SEQUENCE_RE, null, docbookBuildingOptions);
						injectionErrors += processCustomInjectionPoints(customInjectionIds, customInjections, XREF_INJECTION_POINT, topicID, topic, CUSTOM_INJECTION_SINGLE_RE, null, docbookBuildingOptions);
						injectionErrors += processCustomInjectionPoints(customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, topicID, topic, CUSTOM_INJECTION_LIST_RE, null, docbookBuildingOptions);
						injectionErrors += processCustomInjectionPoints(customInjectionIds, customInjections, ITEMIZEDLIST_INJECTION_POINT, topicID, topic, CUSTOM_ALPHA_SORT_INJECTION_LIST_RE, new TopicTitleSorter(), docbookBuildingOptions);
						injectionErrors += processCustomInjectionPoints(customInjectionIds, customInjections, LIST_INJECTION_POINT, topicID, topic, CUSTOM_INJECTION_LISTITEMS_RE, null, docbookBuildingOptions);

						if (injectionErrors.length() != 0)
						{
							populateIdXMLDataFromDB(errorTopic, topic, false, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

							/* remove the last ", " from the error string */
							injectionErrors = injectionErrors.substring(0, injectionErrors.length() - 2);
							addErrorToTopic(
									topic,
									"Topic references Topic(s) "
											+ injectionErrors
											+ " in a custom injection point, but this topic has either not been related in the database, or was not matched by the filter. The later might occur if you are building a narrative and the injected topic was not listed in the Topic ID field, or you have not selected the 'Include all related topics' option.");
						}
						else
						{
							/* now make the custom injection point substitutions */
							for (final Node customInjectionCommentNode : customInjections.keySet())
							{
								final InjectionListData injectionListData = customInjections.get(customInjectionCommentNode);
								List<Element> list = null;

								/*
								 * this may not be true if we are not building
								 * all related topics
								 */
								if (injectionListData.listItems.size() != 0)
								{
									if (injectionListData.listType == ORDEREDLIST_INJECTION_POINT)
									{
										list = DocbookUtils.wrapOrderedListItemsInPara(topic.getTempTopicXMLDoc(), injectionListData.listItems);
									}
									else
										if (injectionListData.listType == XREF_INJECTION_POINT)
										{
											list = injectionListData.listItems.get(0);
										}
										else
											if (injectionListData.listType == ITEMIZEDLIST_INJECTION_POINT)
											{
												list = DocbookUtils.wrapItemizedListItemsInPara(topic.getTempTopicXMLDoc(), injectionListData.listItems);
											}
											else
												if (injectionListData.listType == LIST_INJECTION_POINT)
												{
													list = DocbookUtils.wrapItemsInListItems(topic.getTempTopicXMLDoc(), injectionListData.listItems);
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

							/***************** PROCESS GENERIC INJECTION POINTS *****************/

							/*
							 * don't add generic injection points when building
							 * a narrative style
							 */
							if (!docbookBuildingOptions.isBuildNarrative())
							{
								/*
								 * this collection will hold the lists of
								 * related topics
								 */
								final HashMap<Tag, ArrayList<Topic>> relatedLists = new HashMap<Tag, ArrayList<Topic>>();

								/* wrap each related topic in a listitem tag */
								for (final Integer relatedTopic : topic.getRelatedTopicIDs())
								{
									/*
									 * don't process those topics that were /
									 * injected into custom injection points
									 */
									if (!customInjectionIds.contains(relatedTopic))
									{
										// loop through the topic type tags
										for (final TagToCategory primaryTopicTypeTag : topicTypeTagIDs)
										{
											final Integer primaryTopicTypeTagId = primaryTopicTypeTag.getTag().getTagId();

											/*
											 * see if we have processed a
											 * related topic with one of the
											 * topic type tags this may never be
											 * true if not processing all
											 * related topics
											 */
											if (tagIdToTopicMap.containsKey(primaryTopicTypeTagId) && tagIdToTopicMap.get(primaryTopicTypeTagId).containsKey(relatedTopic))
											{
												/*
												 * at this point we have found a
												 * topic that is related, has
												 * not been included in any
												 * custom injection points, and
												 * has been processed
												 */

												if (!relatedLists.containsKey(primaryTopicTypeTag.getTag()))
													relatedLists.put(primaryTopicTypeTag.getTag(), new ArrayList<Topic>());

												/*
												 * add the related topic to the
												 * relatedLists collection
												 * against the topic type tag
												 * that has been assigned to the
												 * related topic
												 */
												relatedLists.get(primaryTopicTypeTag.getTag()).add(topicIdToTopicMap.get(relatedTopic));

												break;
											}
										}
									}
								}

								insertGenericInjectionLinks(topic.getTempTopicXMLDoc(), relatedLists);
							}

							/*
							 * make sure the xml is valid after all of our
							 * modifications
							 */
							postValidateTopicDocbook(topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
						}
					}

					++topicProgress;
				}
			}

			++categoryProgress;
		}

		System.out.println("Processing injection points 100% done");
	}

	/**
	 * The generic injection points are placed in well defined locations within
	 * a topics xml structure. This function takes the list of related topics
	 * and the topic type tags that are associated with them and injects them
	 * into the xml document.
	 */
	@SuppressWarnings("serial")
	private void insertGenericInjectionLinks(final Document xmlDoc, final Map<Tag, ArrayList<Topic>> relatedLists)
	{
		/* related overviews are placed after the section title */
		Node titleNode = null;
		final NodeList nodes = xmlDoc.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodes.getLength(); ++i)
		{
			final Node node = nodes.item(i);
			if (node.getNodeType() == 1 && node.getNodeName().equals("title"))
			{
				titleNode = node;
				break;
			}
		}

		/* related overviews are placed before the first simplesect */
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
		 * place the overviews and concepts at the top of the topic. these will
		 * end up in the reverse order that they are added to the ArrayList
		 */
		for (final Integer topTag : new ArrayList<Integer>()
		{
			{
				add(Constants.CONCEPT_TAG_ID);
				add(Constants.CONCEPTUALOVERVIEW_TAG_ID);
			}
		})
		{
			/* check for related concepts */
			for (final Tag tag : relatedLists.keySet())
			{
				if (tag.getTagId() == topTag)
				{
					final Node itemizedlist = DocbookUtils.createRelatedTopicItemizedList(xmlDoc, "Related " + tag.getTagName() + "s");

					for (final Topic relatedTopic : relatedLists.get(tag))
						DocbookUtils.createRelatedTopicLink(xmlDoc, itemizedlist, relatedTopic.getXRefID());

					if (titleNode != null)
					{
						/*
						 * insert the new node after the title, if a title
						 * exists
						 */
						DocbookUtils.insertNodeAfter(titleNode, itemizedlist);
					}
					else
					{
						/*
						 * of the title does not exist, see if any children
						 * exist
						 */
						final Node firstChild = xmlDoc.getDocumentElement().getFirstChild();

						if (firstChild != null)
						{
							/* if so, insert the new node before the first child */
							xmlDoc.getDocumentElement().insertBefore(itemizedlist, firstChild);
						}
						else
						{
							/* if not, just add the new node */
							xmlDoc.getDocumentElement().appendChild(itemizedlist);
						}
					}
				}
			}
		}

		/*
		 * place the related task and reference topics and place them at the end
		 * of the topic
		 */
		for (final Integer topTag : new ArrayList<Integer>()
		{
			{
				add(Constants.REFERENCE_TAG_ID);
				add(Constants.TASK_TAG_ID);
			}
		})
		{
			for (final Tag tag : relatedLists.keySet())
			{
				if (tag.getTagId() == topTag)
				{
					final Node itemizedlist = DocbookUtils.createRelatedTopicItemizedList(xmlDoc, "Related " + tag.getTagName() + "s");

					for (final Topic relatedTopic : relatedLists.get(tag))
						DocbookUtils.createRelatedTopicLink(xmlDoc, itemizedlist, relatedTopic.getXRefID());

					if (simplesectNode != null)
						xmlDoc.getDocumentElement().insertBefore(itemizedlist, simplesectNode);
					else
						xmlDoc.getDocumentElement().appendChild(itemizedlist);
				}
			}
		}
	}

	protected byte[] getStringBytes(final String input)
	{
		return input == null ? new byte[]
		{} : input.getBytes();
	}

	protected void addImagesToBook(final HashMap<String, byte[]> files)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		/* download the image files that were identified in the processing stage */
		int imageProgress = 0;
		final int imageTotal = this.imageLocations.size();

		for (final TopicImageData imageLocation : this.imageLocations)
		{
			boolean success = false;

			final int extensionIndex = imageLocation.getImageName().lastIndexOf(".");
			final int pathIndex = imageLocation.getImageName().lastIndexOf("/");

			if (/* characters were found */
			extensionIndex != -1 && pathIndex != -1 &&
			/* the path character was found before the extension */
			extensionIndex > pathIndex)
			{
				try
				{
					/*
					 * The file name minus the extension should be an integer
					 * that references an ImageFile record ID.
					 */
					final String topicID = imageLocation.getImageName().substring(pathIndex + 1, extensionIndex);
					final ImageFile imageFile = entityManager.find(ImageFile.class, Integer.parseInt(topicID));

					if (imageFile != null)
					{
						success = true;
						files.put("Book/en-US/" + imageLocation.getImageName(), imageFile.getImageData());
					}
					else
					{
						addErrorToTopic(imageLocation.getTopic(), "ImageFile ID " + topicID + " from image location " + imageLocation + " was not found!");
						System.out.println("ImageFile ID " + topicID + " from image location " + imageLocation + " was not found!");
					}
				}
				catch (final Exception ex)
				{
					success = false;
					addErrorToTopic(imageLocation.getTopic(), imageLocation + " is not a valid image. Must be in the format [ImageFileID].extension e.g. 123.png, or images/321.jpg");
					ExceptionUtilities.handleException(ex);
				}
			}

			/* put in a place holder */
			if (!success)
			{
				files.put("Book/en-US/" + imageLocation.getImageName(), failpenguinPng);
			}

			final float progress = (float) imageProgress / (float) imageTotal * 100;
			System.out.println("Downloading Images " + Math.round(progress) + "% done");

			++imageProgress;
		}
	}

	private String buildErrorChapter()
	{
		String errorItemizedLists = "";

		for (final Topic topic : errors.keySet())
		{
			final List<String> topicErrorItems = new ArrayList<String>();

			final String tags = topic.getCommaSeparatedTagList();
			final String url = getTopicSkynetURL(topic);

			topicErrorItems.add(DocbookUtils.buildListItem("INFO: " + tags));
			topicErrorItems.add(DocbookUtils.buildListItem("INFO: <ulink url=\"" + url + "\">Topic URL</ulink>"));

			for (final String error : errors.get(topic).getErrors())
				topicErrorItems.add(DocbookUtils.buildListItem("ERROR: " + error));

			for (final String warning : errors.get(topic).getWarnings())
				topicErrorItems.add(DocbookUtils.buildListItem("WARNING: " + warning));

			/*
			 * this should never be false, because a topic will only be listed
			 * in the errors collection once a error or warning has been added.
			 * The count of 2 comes from the standard list items we added above
			 * for the tags and url.
			 */
			if (topicErrorItems.size() > 2)
			{
				final String title = "Topic ID " + topic.getTopicId();
				final String id = DocbookUtils.ERROR_XREF_ID_PREFIX + topic.getTopicId();

				errorItemizedLists += DocbookUtils.wrapListItems(topicErrorItems, title, id);
			}

		}

		return DocbookUtils.buildChapter(errorItemizedLists, "Compiler Output");
	}

	/**
	 * This function takes the XML files that have been generated by the Docbook
	 * compilation process and packages them up with some static Docbook Strings
	 * to produce a ZIP file that is sent to the user
	 */
	protected void buildZipFile(final List<TagToCategory> topicTypeTagIDs, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/* build up the files that will make up the zip file */
		final HashMap<String, byte[]> files = new HashMap<String, byte[]>();

		/* add the images to the zip file */
		addImagesToBook(files);

		/* build the chapter containing the compiler errors */
		final String compilerOutput = buildErrorChapter();

		/*
		 * the narrative style will not include a TOC or Eclipse plugin, and
		 * includes a different publican.cfg file
		 */
		String publicnCfgFixed = "";
		if (!docbookBuildingOptions.isBuildNarrative())
		{
			// build the table of contents
			final TocTopLevel tocTopLevel = buildTOCCommonNameOnly(docbookBuildingOptions);

			// get the HTML TOC
			final String toc = tocTopLevel.getDocbook();

			// get the Eclipse TOC
			final String eclipseToc = tocTopLevel.getEclipseXml();

			// add the files that make up the Eclipse help package
			files.put("Book/eclipse/com.redhat.eap6.doc_1.0.0/toc.xml", getStringBytes(eclipseToc));
			files.put("Book/eclipse/com.redhat.eap6.doc_1.0.0/plugin.xml", getStringBytes(pluginXml));
			files.put("Book/eclipse_package.sh", getStringBytes(LineEndFormatter.convertToLinuxLineEndings(eclisePackageSh)));
			files.put("Book/publican_eclipse.cfg", getStringBytes(publicanEclipseCfg));
			files.put("Book/en-US/Toc.xml", getStringBytes(toc));
			files.put("Book/en-US/StartPage.xml", getStringBytes(startPage == null ? "" : startPage));

			for (final TagToCategory topicTag : topicTypeTagIDs)
			{
				if (tagIdToDocbookMap.containsKey(topicTag.getTag().getTagId()) && tagIdToDocbookMap.get(topicTag.getTag().getTagId()).length() != 0)
				{
					files.put("Book/en-US/" + topicTag.getTag().getTagName().replaceAll(" ", "") + "s.xml", getStringBytes(tagIdToDocbookMap.get(topicTag.getTag().getTagId())));
				}
			}

			// now add the publican files
			publicnCfgFixed = publicanCfg;
		}
		else
		{
			if (tagIdToDocbookMap.containsKey(Constants.TAG_ID_NARRATIVE))
			{
				files.put("Book/en-US/Narrative.xml", getStringBytes(tagIdToDocbookMap.get(Constants.TAG_ID_NARRATIVE)));
			}

			// now add the Publican files
			publicnCfgFixed = narrativePublicanCfg;
		}

		// fix the Publican CFG file
		if (docbookBuildingOptions.isPublicanShowRemarks() && publicnCfgFixed != null)
			publicnCfgFixed += "\nshow_remarks: 1";
		files.put("Book/publican.cfg", getStringBytes(publicnCfgFixed));

		// add the files that are used to package up the RPM file
		files.put("Book/package.sh", getStringBytes(LineEndFormatter.convertToLinuxLineEndings(package_sh)));
		files.put("Book/packager/en-US/Makefile", getStringBytes(makefile));
		files.put("Book/packager/en-US/spec.in", getStringBytes(spec_in));

		// replace the date marker in the Book.XML file
		files.put("Book/en-US/Book_Info.xml", bookInfoXml.replace(DATE_YYMMDD_TEXT_MARKER, new StringBuilder(new SimpleDateFormat("yyMMdd").format(new Date()))).getBytes());

		files.put("Book/en-US/Author_Group.xml", getStringBytes(authorGroupXml));
		files.put("Book/en-US/Book.ent", getStringBytes(bookEnt));

		// these files are used by the YUI treeview
		files.put("Book/en-US/files/yahoo-dom-event.js", getStringBytes(yahooDomEventJs));
		files.put("Book/en-US/files/treeview-min.js", getStringBytes(treeviewMinJs));
		files.put("Book/en-US/files/treeview.css", getStringBytes(treeviewCss));
		files.put("Book/en-US/files/jquery.min.js", getStringBytes(jqueryMinJs));

		// these are the images that are referenced in the treeview.css file
		files.put("Book/en-US/files/treeview-sprite.gif", treeviewSpriteGif);
		files.put("Book/en-US/files/treeview-loading.gif", treeviewLoadingGif);
		files.put("Book/en-US/files/check1.gif", check1Gif);
		files.put("Book/en-US/files/check2.gif", check2Gif);

		files.put("Book/en-US/images/icon.svg", getStringBytes(iconSvg));
		files.put("Book/en-US/images/jboss.svg", getStringBytes(jbossSvg));

		if (tagIdToDocbookMap.containsKey(Constants.TAG_ID_ERROR))
		{
			files.put("Book/en-US/ErrorTopics.xml", getStringBytes(tagIdToDocbookMap.get(Constants.TAG_ID_ERROR)));
		}

		/*
		 * build the middle of the Book.xml file, where we include references to
		 * the topic type pages that were built above
		 */
		String bookXmlXiIncludes = "";

		if (!docbookBuildingOptions.isSuppressErrorsPage())
		{
			files.put("Book/en-US/Errors.xml", getStringBytes(compilerOutput == null ? "" : compilerOutput));
			bookXmlXiIncludes += "	<xi:include href=\"Errors.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
		}

		if (tagIdToDocbookMap.containsKey(Constants.TAG_ID_ERROR))
		{
			bookXmlXiIncludes += "	<xi:include href=\"ErrorTopics.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
		}

		/*
		 * only reference the Toc.xml file if we are not building a narrative
		 * book
		 */
		if (!docbookBuildingOptions.isBuildNarrative())
		{
			bookXmlXiIncludes += "	<xi:include href=\"Toc.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />";
			bookXmlXiIncludes += "	<xi:include href=\"StartPage.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />";

			for (final TagToCategory topicTag : topicTypeTagIDs)
			{
				if (tagIdToDocbookMap.containsKey(topicTag.getTag().getTagId()) && tagIdToDocbookMap.get(topicTag.getTag().getTagId()).length() != 0)
				{
					bookXmlXiIncludes += "	<xi:include href=\"" + topicTag.getTag().getTagName().replaceAll(" ", "") + "s.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
				}
			}
		}
		else
		{
			if (tagIdToDocbookMap.containsKey(Constants.TAG_ID_NARRATIVE))
			{
				bookXmlXiIncludes += "	<xi:include href=\"Narrative.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
			}
		}

		// replace the marker in the book.xml template
		final String thisBookXml = bookXml.replace(BOOK_XML_XI_INCLUDES_MARKER, bookXmlXiIncludes);

		files.put("Book/en-US/Book.xml", getStringBytes(thisBookXml));
		files.put("Book/en-US/Revision_History.xml", getStringBytes(revisionHistoryXml));

		// now create the zip file
		byte[] zipFile = null;
		try
		{
			zipFile = ZipUtilities.createZip(files);
		}
		catch (final Exception ex)
		{
			zipFile = null;
			ExceptionUtilities.handleException(ex);
		}

		HTTPUtilities.writeOutContent(zipFile, "Book.zip", MIMEUtilities.ZIP_MIME_TYPE);
	}

	/**
	 * This function takes the topics that match the filter tags selected by the
	 * user and processes them to validate the xml in the topic SVN repo,
	 * process the related topics and fix up image paths
	 */
	protected void processTopics(final WorkingMemory businessRulesWorkingMemory, final Filter filter, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final int roleCategoryID, final String searchTagsUrl,
			final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final List<Topic> topicList = EntityUtilities.getTopicsFromFilter(filter);

		/*
		 * this collection holds all the id attributes used by the topics we use
		 * this list to look for duplicates which would cause docbook
		 * compilation errors
		 */
		final List<String> usedIds = new ArrayList<String>();

		/*
		 * loop through each topic, and save the relevant information like type,
		 * related topics etc
		 */
		for (final Topic topic : topicList)
			processTopic(businessRulesWorkingMemory, topic, mandatoryExclusiveTagCollections, mandatoryTagCollections, roleCategoryID, searchTagsUrl, tagToCategories, usedIds, docbookBuildingOptions);

	}

	/**
	 * Uses those topics that belong to a particular tag to build a docbook
	 * chapter
	 * 
	 * @param tagId
	 *            The TagID that the topics need to belong to in order to be
	 *            included in the chapter
	 * @param tagName
	 *            The TagName that belongs to the TagID
	 */
	protected void buildChapterFromCollection(final Integer tagId, final List<Integer> buildOrder)
	{
		if (tagIdToTopicMap.containsKey(tagId))
		{
			// loop over the topics that have one of the type tags assigned to
			// it
			final HashMap<Integer, Topic> topics = tagIdToTopicMap.get(tagId);

			String sections = "";

			for (final Integer orderedTopicID : buildOrder)
			{
				if (topics.containsKey(orderedTopicID))
					sections += topics.get(orderedTopicID).getTempTopicXMLDocString();
			}

			for (final Integer topicID : topics.keySet())
			{
				/*
				 * those with a specific order are added first, and should now
				 * be skipped
				 */
				if (buildOrder.contains(topicID))
					continue;

				/*
				 * take the XML Document we have been modifying, convert it to
				 * text, and add it to the section
				 */
				if (topics.containsKey(topicID))
					sections += topics.get(topicID).getTempTopicXMLDocString();
			}

			// build up the sections into a chapter
			if (sections.length() != 0)
			{
				tagIdToDocbookMap.put(tagId, DocbookUtils.addXMLBoilerplate(DocbookUtils.buildChapter(sections, "")));
			}
		}
	}

	/**
	 * Builds chapters for all the tags in the topicTypeTagIDs collection
	 * 
	 * @param topicTypeTagIDs
	 *            This holds a collection of Tags that are used to build the
	 *            chapters (usually the Topic Type tags, like Concept, Task ,
	 *            Reference etc).
	 */
	protected void buildChapters(final List<TagToCategory> topicTypeTagIDs, final List<Integer> buildOrder)
	{
		for (final TagToCategory topicTypeTag : topicTypeTagIDs)
			buildChapterFromCollection(topicTypeTag.getTag().getTagId(), buildOrder);

		/*
		 * we know, in addition to the core tag types that will be included as
		 * chapters, that we also need to include those topics that couldn't be
		 * mapped to a tag because of validation errors
		 */
		buildChapterFromCollection(Constants.TAG_ID_ERROR, buildOrder);

		/*
		 * when building a narrative, all topics are in a single chapter in a
		 * specific order
		 */
		buildChapterFromCollection(Constants.TAG_ID_NARRATIVE, buildOrder);
	}

	/**
	 * Loads the strings for the file templates from the database
	 */
	protected void loadConstantsFromDB()
	{
		revisionHistoryXml = loadStringConstant(REVISION_HISTORY_XML_ID);
		bookXml = loadStringConstant(BOOK_XML_ID);
		publicanCfg = loadStringConstant(PUBLICAN_CFG_ID);
		authorGroupXml = loadStringConstant(AUTHOR_GROUP_XML_ID);
		bookInfoXml = loadStringConstant(BOOK_INFO_XML_ID);
		bookEnt = loadStringConstant(BOOK_ENT_ID);
		iconSvg = loadStringConstant(ICON_SVG_ID);

		errorTopic = loadStringConstant(ERROR_TOPIC_ID);
		errorTagsTopic = loadStringConstant(ERRORTAGS_TOPIC_ID);
		missingTopic = loadStringConstant(MISSING_TOPIC_ID);

		makefile = loadStringConstant(MAKEFILE_ID);
		spec_in = loadStringConstant(SPEC_IN_ID);
		package_sh = loadStringConstant(PACKAGE_SH_ID);

		startPage = loadStringConstant(START_PAGE_ID);
		jbossSvg = loadStringConstant(JBOSS_SVG_ID);

		yahooDomEventJs = loadStringConstant(YAHOO_DOM_EVENT_JS_ID);
		treeviewMinJs = loadStringConstant(TREEVIEW_MIN_JS_ID);
		treeviewCss = loadStringConstant(TREEVIEW_CSS_ID);
		jqueryMinJs = loadStringConstant(JQUERY_MIN_JS_ID);

		treeviewSpriteGif = loadBlobConstant(TREEVIEW_SPRITE_GIF_ID);
		treeviewLoadingGif = loadBlobConstant(TREEVIEW_LOADING_GIF_ID);
		check1Gif = loadBlobConstant(CHECK1_GIF_ID);
		check2Gif = loadBlobConstant(CHECK2_GIF_ID);
		failpenguinPng = loadBlobConstant(FAILPENGUIN_PNG_ID);

		invisibleTagID = loadIntegerConstant(INVISIBLE_TAG_ID);

		pluginXml = loadStringConstant(PLUGIN_XML_ID);
		eclisePackageSh = loadStringConstant(ECLIPSE_PACKAGE_SH_ID);
		publicanEclipseCfg = loadStringConstant(PUBLICAN_ECLIPSE_CFG_ID);
		narrativePublicanCfg = loadStringConstant(NARRATIVE_PUBLICAN_CFG_ID);
	}

	protected byte[] loadBlobConstant(final Integer id)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final BlobConstants constant = entityManager.find(BlobConstants.class, id);
		return constant == null ? null : constant.getConstantValue();
	}

	protected Integer loadIntegerConstant(final Integer id)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final IntegerConstants constant = entityManager.find(IntegerConstants.class, id);
		return constant == null ? null : constant.getConstantValue();
	}

	protected String loadStringConstant(final Integer id)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final StringConstants constant = entityManager.find(StringConstants.class, id);
		return constant == null ? null : constant.getConstantValue();
	}

	@SuppressWarnings("unchecked")
	protected List<TagToCategory> getTagToCategories()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<TagToCategory> tagToCategories = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();
		return tagToCategories;
	}

	@SuppressWarnings("serial")
	public void buildDocbookZipFile(final Filter filter, final WorkingMemory businessRulesWorkingMemory, final DocbookBuildingOptions docbookBuildingOptions)
	{
		reloadSystemPreferences(businessRulesWorkingMemory);
		loadConstantsFromDB();

		final List<TagToCategory> tagToCategories = getTagToCategories();

		/*
		 * get a list of the tag to category mappings. we will pass this
		 * collection through various function, which can then use the
		 * getTagToCatgeories function to filter it for the tags that are mapped
		 * to a specific category
		 */

		final List<TagToCategory> topicTypeTagIDs = getTagToCatgeories(Constants.TYPE_CATEGORY_ID, tagToCategories);
		final List<TagToCategory> technologyTagIDs = getTagToCatgeories(Constants.TECHNOLOGY_CATEGORY_ID, tagToCategories);
		final List<TagToCategory> commonNameTagIDs = getTagToCatgeories(Constants.COMMON_NAME_CATEGORY_ID, tagToCategories);

		/*
		 * All topics require a topic type, and only one can be assigned
		 */
		final ArrayList<List<TagToCategory>> mandatoryExclusiveTags = new ArrayList<List<TagToCategory>>();
		mandatoryExclusiveTags.add(topicTypeTagIDs);

		/*
		 * All topics need at least a technology or common name
		 */
		final ArrayList<List<TagToCategory>> mandatoryTags = new ArrayList<List<TagToCategory>>();
		mandatoryTags.add(new ArrayList<TagToCategory>()
		{
			{
				addAll(technologyTagIDs);
				addAll(commonNameTagIDs);
			}
		});

		/*
		 * build the URL that will return to this screen with the appropriate
		 * selection
		 */
		String searchTagsUrl = "";
		final HashMap<String, String> urlVars = filter.getUrlVariables();
		for (final Entry<String, String> var : urlVars.entrySet())
			searchTagsUrl = EntityUtilities.addParameter(searchTagsUrl, var.getKey(), var.getValue());
		searchTagsUrl = Constants.SERVER_URL + "/TopicIndex/CustomSearchTopics.seam" + searchTagsUrl;

		/*
		 * build an initial list of "root" topics from the search page to be
		 * processed
		 */
		processTopics(businessRulesWorkingMemory, filter, mandatoryExclusiveTags, mandatoryTags, Constants.LIFECYCLE_CATEGORY_ID, searchTagsUrl, tagToCategories, docbookBuildingOptions);

		/*
		 * take the information gathered by the processTopic() function, and use
		 * it to dynamically inject xrefs
		 */
		processInjections(topicTypeTagIDs, searchTagsUrl, Constants.LIFECYCLE_CATEGORY_ID, tagToCategories, docbookBuildingOptions);

		/*
		 * go through and sort the topicList collection into the same order as
		 * the topic id filter field
		 */
		final List<Integer> buildOrder = new ArrayList<Integer>();
		if (docbookBuildingOptions.isBuildNarrative())
		{
			final FilterField topicIdField = selectFirst(filter.getFilterFields(), having(on(FilterField.class).getField(), equalTo(Constants.TOPIC_IDS_FILTER_VAR)));

			if (topicIdField != null)
			{
				final String[] topicIds = topicIdField.getValue().split(",");

				for (final String topicId : topicIds)
				{
					try
					{
						final Integer topicIdInt = Integer.parseInt(topicId.trim());
						buildOrder.add(topicIdInt);
					}
					catch (final Exception ex)
					{
						/*
						 * a non-integer was probably entered into the field, so
						 * ignore
						 */
						ExceptionUtilities.handleException(ex);
					}
				}
			}
		}

		// build up the xml from the process topics into chapters
		buildChapters(topicTypeTagIDs, buildOrder);

		// now build the publican zip file that will be sent to the user
		buildZipFile(topicTypeTagIDs, tagToCategories, docbookBuildingOptions);
	}

	protected Document buildErrorTopic(final String template, final Topic xmlData, final String xref, final String filterUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		String retValue = template;

		// include a link to the error page if it has not been suppressed
		if (docbookBuildingOptions.isSuppressErrorsPage())
			retValue = retValue.replace(TOPIC_ERROR_LINK_MARKER, "");
		else
			retValue = retValue.replace(TOPIC_ERROR_LINK_MARKER, "<para>Please review the compiler error for <xref linkend=\"" + DocbookUtils.ERROR_XREF_ID_PREFIX + "#TOPICID#\"/> for more detailed information.</para>");

		// replace the various markers
		retValue = retValue.replace(TOPIC_ID_MARKER, xmlData.getTopicId().toString()).replace(TOPIC_TITLE_MARKER, EntityUtilities.cleanTextForXML(xmlData.getTopicTitle())).replace(TOPIC_TEXT_MARKER, EntityUtilities.cleanTextForXML(xmlData.getTopicText())).replace(XREF_ID_MARKER, xref)
				.replace(TAG_FILTER_URL_MARKER, filterUrl.replace("&", "&amp;")).replace(BUILD_MARKER, Constants.BUILD);

		return XMLUtilities.convertStringToDocument(retValue);
	}

	/**
	 * Adds the role attribute to the section element (or technically where ever
	 * the ROLE_MARKER has been defined in the template, and then calls the 2nd
	 * populateIdXMLDataFromDB function to populate the rest of the template.
	 */
	protected void populateIdXMLDataFromDB(final String errorTemplate, final Topic xmlData, final boolean assignToErrorTag, final String filterUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final String fixedTemplate = errorTemplate.replaceAll(ROLE_MARKER, xmlData.getTempTopicRole());
		populateIdXMLDataFromDB(fixedTemplate, xmlData, assignToErrorTag, filterUrl, docbookBuildingOptions);
	}

	/**
	 * Populates the template with the topics information, and maps the topic to
	 * the error tag (which is just a negative tag id to indate that it doesn't
	 * exist in the database) if required.
	 * 
	 * @param errorTemplate
	 *            The xml template, with markers used to indicate those parts of
	 *            the template that should be customized
	 * @param xmlData
	 *            The data representing the topic being processed (the
	 *            amalgamation of database and SVN data)
	 * @param topic
	 *            The Topic entity that represents the topic in the database
	 * @param assignToErrorTag
	 *            true if this topic needs to be included in the final docbook
	 *            build
	 * @param filterUrl
	 *            The URL that is used to get back to the tag search screen
	 */
	protected void populateIdXMLDataFromDB(final String errorTemplate, final Topic xmlData, final boolean assignToErrorTag, final String filterUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		xmlData.setTempTopicXMLDoc(buildErrorTopic(errorTemplate, xmlData, xmlData.getXRefID(), filterUrl, docbookBuildingOptions));
		xmlData.setTempInvalidTopic(true);

		/*
		 * the topic needs to be included in the final build, but does not have
		 * valid tags that we can associate with it. so we associate it with the
		 * "error tag". all topics with the error tag will be included in the
		 * docbook build, which is important if they are related to other
		 * topics.
		 */
		if (assignToErrorTag)
		{
			if (!tagIdToTopicMap.containsKey(Constants.TAG_ID_ERROR))
				tagIdToTopicMap.put(Constants.TAG_ID_ERROR, new HashMap<Integer, Topic>());
			tagIdToTopicMap.get(Constants.TAG_ID_ERROR).put(xmlData.getTopicId(), xmlData);
		}
	}

	/**
	 * Test to make sure the XML is valid. This is done by getting Xerces to
	 * parse the xml, and if no exceptions and thrown we assume the xml is ok.
	 * The XML is also validated against a Rocbook DTD (which is a subset of
	 * Docbook)
	 * 
	 * In an ideal world, we should be able to assume that the xml coming from
	 * the repo is valid, and that our injections don't break docbook
	 * validations. But until the topic tool implements some kind of validation,
	 * this step is required.
	 * 
	 * This is done after the injections are generated.
	 */
	protected boolean postValidateTopicDocbook(final Topic topic, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final XMLValidator validator = new XMLValidator();

		// get the xml document, or null if it was not valid
		if (validator.validateTopicXML(topic.getTempTopicXMLDoc(), true) == null)
		{
			/*
			 * This XML is now allows to have the "]]>" closing tag, which can
			 * happen if it has a CDATA section
			 */
			final String originalXML = XMLUtilities.cleanXMLForInclusionInCDATA(topic.getTempTopicXMLDocString());

			populateIdXMLDataFromDB(errorTopic, topic, false, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
			System.out.println(topic.getTopicId().toString() + " failed docbook validation after the injection points were processed.");

			addErrorToTopic(topic, "Topic failed docbook validation after the injection points were processed. The error was <emphasis>" + validator.getErrorText() + "</emphasis> The XML after the injections points were processed is: <programlisting><![CDATA[" + originalXML + "]]></programlisting>");
			return false;
		}

		return true;
	}

	/**
	 * Test to make sure the XML is valid. This is done by getting Xerces to
	 * parse the xml, and if no exceptions and thrown we assume the xml is ok.
	 * In an ideal world, we should be able to assume that the xml coming from
	 * the repo is valid. But until the topic tool implements some kind of
	 * validation, this step is required.
	 */
	protected boolean validateTopicXML(final Topic topic, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		// see if we have some xml to work with
		if (topic.getTopicXML() == null || topic.getTopicXML().trim().length() == 0)
		{
			populateIdXMLDataFromDB(errorTopic, topic, false, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
			addErrorToTopic(topic, "Topic has a blank Topic XML field");
			return false;
		}

		/*
		 * if the xml is not blank, but the tempTopicXMLDoc variable is null, we
		 * must have had some invalid xml
		 */
		if (topic.getTempTopicXMLDoc() == null)
		{
			populateIdXMLDataFromDB(errorTopic, topic, false, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

			addErrorToTopic(topic, "Topic failed xml validation.");
			return false;
		}

		return true;
	}

	protected boolean validateIdAttributesUnique(final Topic topic, final String searchTagsUrl, final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final NodeList elements = topic.getTempTopicXMLDoc().getDocumentElement().getChildNodes();
		for (int i = 0; i < elements.getLength(); ++i)
		{
			final String retValue = validateIdAttributesUnique(topic, searchTagsUrl, elements.item(i), usedIds);
			if (retValue != null)
			{
				populateIdXMLDataFromDB(errorTopic, topic, false, searchTagsUrl, docbookBuildingOptions);
				addErrorToTopic(topic, "Topic uses an id attribute called \"" + retValue + "\" that is already used in another topic.");
				return false;
			}
		}

		return true;
	}

	protected String validateIdAttributesUnique(final Topic xmlData, final String searchTagsUrl, final Node node, final List<String> usedIds)
	{
		final NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
		{
			final Node idAttribute = attributes.getNamedItem("id");
			if (idAttribute != null)
			{
				final String idAttibuteValue = idAttribute.getNodeValue();

				if (usedIds.contains(idAttibuteValue))
				{
					return idAttibuteValue;
				}
				else
				{
					usedIds.add(idAttibuteValue);
				}
			}
		}

		final NodeList elements = node.getChildNodes();
		for (int i = 0; i < elements.getLength(); ++i)
		{
			final String retValue = validateIdAttributesUnique(xmlData, searchTagsUrl, elements.item(i), usedIds);
			if (retValue != null)
				return retValue;
		}

		return null;
	}

	/**
	 * Find the highest priority topic lifecycle tag assigned to a topic and
	 * returns it. This is used when assigning a role to section element.
	 */
	protected String getTopicLifecycleRole(final Integer topicID, final int roleCategoryID, final List<TagToCategory> tagToCategories)
	{
		final List<TagToCategory> tags = getTagToCatgeories(roleCategoryID, tagToCategories);

		Collections.sort(tags);
		Collections.reverse(tags);

		for (final TagToCategory tagToCat : tags)
		{
			final Integer tagID = tagToCat.getTag().getTagId();

			if (tagIdToTopicMap.containsKey(tagID) && tagIdToTopicMap.get(tagID).containsKey(topicID))
				return tagToCat.getTag().getTagName().replaceAll(" ", "");
		}

		return "";
	}

	/**
	 * Assign a role attribute to the section element of the docbook tag based
	 * on the highest priority topic lifecycle tag assigned to the topic. This
	 * role can then be used to highlight topics when they are compiled to HTML
	 */
	protected void processTopicLifecycleRole(final Topic xmlData, final Topic topic, final int roleCategoryID, final List<TagToCategory> tagToCategories)
	{
		final String topicTagRole = getTopicLifecycleRole(topic.getTopicId(), roleCategoryID, tagToCategories);
		if (topicTagRole.length() != 0)
			xmlData.getTempTopicXMLDoc().getDocumentElement().setAttribute("role", topicTagRole);
	}

	/**
	 * Add a note to those topics that do not have the written tag assigned to
	 * them
	 */
	protected void processTopicDraftWarning(final Topic xmlData, final Topic topic)
	{
		// if this topic has not been marked as final, add a note
		if (!(tagIdToTopicMap.containsKey(Constants.TOPIC_FINAL_LIFECYCLE) && tagIdToTopicMap.get(Constants.TOPIC_FINAL_LIFECYCLE).containsKey(topic.getTopicId())))
		{
			final Element note = xmlData.getTempTopicXMLDoc().createElement("note");
			final Element title = xmlData.getTempTopicXMLDoc().createElement("title");
			title.setTextContent("Topic is still in a draft state");
			note.appendChild(title);
			final Element para = xmlData.getTempTopicXMLDoc().createElement("para");
			para.setTextContent("This topic is still in a draft state. The contents below are subject to change");
			note.appendChild(para);

			final NodeList nodes = xmlData.getTempTopicXMLDoc().getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); ++i)
			{
				final Node node = nodes.item(i);
				if (node.getNodeType() == 1 && !node.getNodeName().equals("title"))
				{
					xmlData.getTempTopicXMLDoc().getDocumentElement().insertBefore(note, node);
					break;
				}
			}
		}
	}

	/**
	 * Sets the topic xref id to the topic database id
	 */
	protected void processTopicID(final Topic topic)
	{
		topic.getTempTopicXMLDoc().getDocumentElement().setAttribute("id", topic.getXRefID());
	}

	/**
	 * Adds some debug information and links to the end of the topic
	 */
	protected void processTopicAdditionalInfo(final Topic xmlData, final Topic topic, final String searchTagsUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		// SIMPLESECT TO HOLD OTHER LINKS
		final Element bugzillaSection = xmlData.getTempTopicXMLDoc().createElement("simplesect");
		xmlData.getTempTopicXMLDoc().getDocumentElement().appendChild(bugzillaSection);

		final Element bugzillaSectionTitle = xmlData.getTempTopicXMLDoc().createElement("title");
		bugzillaSectionTitle.setTextContent("");
		bugzillaSection.appendChild(bugzillaSectionTitle);

		// BUGZILLA LINK

		try
		{
			final Element bugzillaPara = xmlData.getTempTopicXMLDoc().createElement("para");
			bugzillaPara.setAttribute("role", ROLE_CREATE_BUG_PARA);

			final Element bugzillaULink = xmlData.getTempTopicXMLDoc().createElement("ulink");

			bugzillaULink.setTextContent("Report a bug");

			final SimpleDateFormat formatter = new SimpleDateFormat(Constants.FILTER_DISPLAY_DATE_FORMAT);
			final String whiteboard = URLEncoder.encode("Topic ID:" + topic.getTopicId() + " Skynet Build: " + Constants.BUILD + " Topic Title: " + topic.getTopicTitle() + " Topic Revision: " + topic.getLatestRevision() + " Topic Revision Date: " + formatter.format(topic.getLatestRevisionDate()) + " Topic Tags: "
					+ topic.getCommaSeparatedTagList(), "UTF-8");

			bugzillaULink.setAttribute("url", "https://bugzilla.redhat.com/enter_bug.cgi?product=Topic+Tool&component=topics-EAP6&keywords=Documentation&status_whiteboard=" + whiteboard);

			/*
			 * only add the elements to the XML DOM if there was no excpetion
			 * (not that there should be one
			 */
			bugzillaSection.appendChild(bugzillaPara);
			bugzillaPara.appendChild(bugzillaULink);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}

		// SURVEY LINK

		final Element surveyPara = xmlData.getTempTopicXMLDoc().createElement("para");
		surveyPara.setAttribute("role", ROLE_CREATE_BUG_PARA);
		bugzillaSection.appendChild(surveyPara);

		final Text startSurveyText = xmlData.getTempTopicXMLDoc().createTextNode("Thank you for evaluating the new documentation format for JBoss Enterprise Application Platform. Let us know what you think by taking a short ");
		surveyPara.appendChild(startSurveyText);

		final Element surveyULink = xmlData.getTempTopicXMLDoc().createElement("ulink");
		surveyPara.appendChild(surveyULink);
		surveyULink.setTextContent("survey");
		surveyULink.setAttribute("url", "https://www.keysurvey.com/survey/380730/106f/");

		final Text endSurveyText = xmlData.getTempTopicXMLDoc().createTextNode(".");
		surveyPara.appendChild(endSurveyText);

		// VIEW IN SKYNET

		final Element skynetElement = xmlData.getTempTopicXMLDoc().createElement("remark");
		skynetElement.setAttribute("role", ROLE_VIEW_IN_SKYNET_PARA);
		bugzillaSection.appendChild(skynetElement);

		final Element skynetLinkULink = xmlData.getTempTopicXMLDoc().createElement("ulink");
		skynetElement.appendChild(skynetLinkULink);
		skynetLinkULink.setTextContent("View in Skynet");
		skynetLinkULink.setAttribute("url", getTopicSkynetURL(topic));

		// SKYNET VERSION

		final Element buildVersionElement = xmlData.getTempTopicXMLDoc().createElement("remark");
		buildVersionElement.setAttribute("role", ROLE_BUILD_VERSION_PARA);
		bugzillaSection.appendChild(buildVersionElement);

		final Element skynetVersionElementULink = xmlData.getTempTopicXMLDoc().createElement("ulink");
		buildVersionElement.appendChild(skynetVersionElementULink);
		skynetVersionElementULink.setTextContent("Built with Skynet version " + Constants.BUILD);
		skynetVersionElementULink.setAttribute("url", searchTagsUrl);
	}

	/**
	 * Call the BRMS rule to calculate the priority of this topic
	 */
	protected void processTopicCalculatePriority(final Topic xmlData, final Topic topic, final WorkingMemory businessRulesWorkingMemory)
	{
		// generate the relative priority from the BRMS rule
		businessRulesWorkingMemory.setGlobal("topic", topic);
		businessRulesWorkingMemory.setGlobal("idXmlMap", xmlData);
		businessRulesWorkingMemory.insert(new DroolsEvent("CalculateRelativePriority"));
		businessRulesWorkingMemory.fireAllRules();
	}

	/**
	 * Fix image location references so publican will compile them
	 */
	protected void processTopicFixImages(final Topic xmlData, final Topic topic)
	{
		/*
		 * Images have to be in the image folder in publican. Here we loop
		 * through all the imagedata elements and fix up any reference to an
		 * image that is not in the images folder.
		 */

		final List<Node> images = getImages(topic.getTempTopicXMLDoc());

		for (final Node imageNode : images)
		{
			final NamedNodeMap attributes = imageNode.getAttributes();
			if (attributes != null)
			{
				final Node fileRefAttribute = attributes.getNamedItem("fileref");

				if (fileRefAttribute != null && !fileRefAttribute.getNodeValue().startsWith("images/"))
				{
					fileRefAttribute.setNodeValue("images/" + fileRefAttribute.getNodeValue());
				}

				imageLocations.add(new TopicImageData(topic, fileRefAttribute.getNodeValue()));
			}
		}
	}

	/**
	 * Make sure the topic has one and only one tag in each mandatory mutually
	 * exclusive category, and at least on tag in the mandatory categories.
	 * 
	 * This should be enforced when topics are created, but currently this is
	 * not the case.
	 */
	protected boolean validateTopicTags(final Topic xmlData, final Topic topic, final String searchTagsUrl, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/*
		 * make sure topics have the required tags assigned to them. this should
		 * not be necessary, but rules like this have yet to be implemented in
		 * the gui.
		 */

		/*
		 * Get the tag ids instead of the tags themselves, because for some
		 * reason some of the tags are not initialized, which means that we
		 * can't match tags from topc.getTagsArray with the tags supplied to
		 * this function. Accessing their ids forces them to be loaded.
		 */
		final List<Integer> topicToTags = topic.getTagIDs();

		for (final List<TagToCategory> mandatoryExclusiveCollection : mandatoryExclusiveTagCollections)
		{
			final ArrayList<Tag> foundTypeTags = new ArrayList<Tag>();
			for (final TagToCategory exclusiveTag : mandatoryExclusiveCollection)
			{
				if (topicToTags.contains(exclusiveTag.getTag().getTagId()))
					foundTypeTags.add(exclusiveTag.getTag());
			}

			if (foundTypeTags.size() > 1)
			{
				populateIdXMLDataFromDB(errorTagsTopic, xmlData, true, searchTagsUrl, docbookBuildingOptions);

				String error = "Topic " + topic.getTopicId() + " has tags";
				for (final Tag foundTag : foundTypeTags)
					error += " " + foundTag.getTagName() + " [" + foundTag.getTagId() + "]";
				error += " assigned to it from a mutually exclusive catgeory group";

				addErrorToTopic(topic, error);

				return false;
			}
			else
				if (foundTypeTags.size() == 0)
				{
					populateIdXMLDataFromDB(errorTagsTopic, xmlData, true, searchTagsUrl, docbookBuildingOptions);

					addErrorToTopic(topic, "Topic is missing tags in an exclusive mandatory category");

					return false;
				}
		}

		for (final List<TagToCategory> mandatoryCollection : mandatoryTagCollections)
		{
			int foundTypeTags = 0;
			for (final TagToCategory mandatoryTagToCategory : mandatoryCollection)
			{
				final Tag mandatoryTag = mandatoryTagToCategory.getTag();

				if (topicToTags.contains(mandatoryTag.getTagId()))
					++foundTypeTags;
			}

			if (foundTypeTags == 0)
			{
				populateIdXMLDataFromDB(errorTagsTopic, xmlData, true, searchTagsUrl, docbookBuildingOptions);

				addErrorToTopic(topic, "Topic is missing tags in a mandatory category");

				return false;
			}
		}

		return true;
	}

	/**
	 * This is the first step in building the docbook zip file. Each topic from
	 * the search list is processed, its xml retrieved and validated, related
	 * topics noted and recursively processed, and image locations fixed up
	 */
	protected void processTopic(final WorkingMemory businessRulesWorkingMemory, final Topic topic, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final int roleCategoryID, final String searchTagsUrl,
			final List<TagToCategory> tagToCategories, final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/* we have already processed this topic, don't do it again */
		if (topicIdToTopicMap.containsKey(topic.getTopicId()))
			return;

		System.out.println("Processing Topic " + topic.getTopicTitle());

		/*
		 * Convert the XML text into an XML Document. It is this Document that
		 * the remainder of this function will modify
		 */
		topic.initializeTempTopicXMLDoc();

		// check to make sure this topic has not already been processed
		if (!topicIdToTopicMap.containsKey(topic.getTopicId()))
		{
			topic.setTempNavLinkDocbook(DocbookUtils.buildXRefListItem(topic.getXRefID(),
			/* the role is defined as the topic type and topic lifecycle */
			AttributeBuilder.GENERIC_NAV_LINK_ROLE + " " + getTopicLifecycleRole(topic.getTopicId(), roleCategoryID, tagToCategories)));

			/* assign the topic data to the topic id map */
			topicIdToTopicMap.put(topic.getTopicId(), topic);

			/********** VALIDATE THE TAGS **********/

			if (!validateTopicTags(topic, topic, searchTagsUrl, mandatoryExclusiveTagCollections, mandatoryTagCollections, docbookBuildingOptions))
				return;

			/********** ASSIGN THE TOPIC TO THE TAG MAP **********/

			/*
			 * we know the topic has the required tags, so we can now add the
			 * topic to the tag map, which will allow us to easily reference
			 * this topic by its tags
			 */
			for (final TopicToTag topicToTag : topic.getTopicToTags())
			{
				final Integer topicTagId = topicToTag.getTag().getTagId();

				if (!tagIdToTopicMap.containsKey(topicTagId))
					tagIdToTopicMap.put(topicTagId, new HashMap<Integer, Topic>());

				tagIdToTopicMap.get(topicTagId).put(topic.getTopicId(), topic);
			}

			/*
			 * if we are building a narrative, all topics get placed in a
			 * special category
			 */
			if (docbookBuildingOptions.isBuildNarrative())
			{
				if (!tagIdToTopicMap.containsKey(Constants.TAG_ID_NARRATIVE))
					tagIdToTopicMap.put(Constants.TAG_ID_NARRATIVE, new HashMap<Integer, Topic>());

				tagIdToTopicMap.get(Constants.TAG_ID_NARRATIVE).put(topic.getTopicId(), topic);
			}

			// define the role, which uses the topic lifecycle tag
			topic.setTempTopicRole(getTopicLifecycleRole(topic.getTopicId(), roleCategoryID, tagToCategories));

			/********** ADDITIONAL VALIDATION **********/

			/*
			 * make sure that we have valid XML. this won't check the validity
			 * of the docbook, but will ensure that we are working with valid
			 * xml.
			 */
			if (!validateTopicXML(topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions))
				return;

			/*
			 * make sure this topic is not trying to use an id attribute that
			 * has been used previously
			 */
			if (!validateIdAttributesUnique(topic, searchTagsUrl, usedIds, docbookBuildingOptions))
				return;

			/********** TOPIC IS VALID **********/

			/*
			 * At this point we have a valid topic, so we can apply the various
			 * additions and fixes to it
			 */

			processTopicID(topic);
			processTopicCalculatePriority(topic, topic, businessRulesWorkingMemory);
			processTopicLifecycleRole(topic, topic, roleCategoryID, tagToCategories);
			processTopicDraftWarning(topic, topic);
			// processTopicViewRotationLinks(xmlData, topic, tagToCategories);
			processTopicAdditionalInfo(topic, topic, searchTagsUrl, docbookBuildingOptions);
			processTopicFixImages(topic, topic);

			/********** PROCESS RELATED TOPICS **********/

			for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
			{
				/*
				 * don't process related topics when creating a narrative, or
				 * when specifically instructed not to
				 */
				if (docbookBuildingOptions.isProcessRelatedTopics() && !docbookBuildingOptions.isBuildNarrative())
				{
					processTopic(businessRulesWorkingMemory, topicToTopic.getRelatedTopic(), mandatoryExclusiveTagCollections, mandatoryTagCollections, roleCategoryID, searchTagsUrl, tagToCategories, usedIds, docbookBuildingOptions);
				}
			}
		}
	}

	/**
	 * This function is used to add a topic to a nav page collection, assuming
	 * the topic shares a tag that is included in the nav page collection keyset
	 */
	protected boolean populateNavTopicCollection(final HashMap<Tag, TopicData> collection, final Topic topic, final Topic xmlData)
	{
		/*
		 * we need to check that the topic has a tag that can be found in the
		 * collections key set (the keyset essentially represnts the tags in a
		 * particular category)
		 */
		boolean foundTechnology = false;
		for (final Tag topicTag : collection.keySet())
		{
			// check to see that the supplied topic has the tag
			if (filter(having(on(TopicToTag.class).getTag(), equalTo(topicTag)), topic.getTopicToTags()).size() != 0)
			{
				/*
				 * assicate the topic (or at least the represnattion of the
				 * topic in the xmlData parameter) against the tag
				 */
				collection.get(topicTag).map.put(topic.getTopicId(), xmlData);
				// we expect to find at least one matching tag for this topic
				foundTechnology = true;
			}
		}

		// if no matching tags were found, create a warning
		if (!foundTechnology)
		{
			addErrorToTopic(topic, "Topic is not tagged with a technology type", false);
		}

		return foundTechnology;
	}

	/**
	 * @param categoryID
	 *            The category that the TagToCategory entities must belong to in
	 *            order to be returned
	 * @param tagToCategories
	 *            A collection of all TagToCategory entities
	 * @return A collection of TagToCategory entities that belong to the
	 *         categoryID
	 */
	protected List<TagToCategory> getTagToCatgeories(final Integer categoryID, final List<TagToCategory> tagToCategories)
	{
		final List<TagToCategory> retValue = filter(having(on(TagToCategory.class).getCategory().getCategoryId(), equalTo(categoryID)), tagToCategories);
		Collections.sort(retValue);
		return retValue;
	}

	protected void reloadSystemPreferences(final WorkingMemory businessRulesWorkingMemory)
	{
		// generate the relative priority from the BRMS rule
		businessRulesWorkingMemory.insert(new DroolsEvent("SetSystemPreferences"));
		businessRulesWorkingMemory.fireAllRules();
	}

	/**
	 * Uses Hamcrest filters to pull out topics that match the supplied tags
	 */
	protected List<Topic> findTopicsForToc(final List<Integer> tagIds, final List<Integer> excludeTagIDs, final boolean limitToTags)
	{
		List<Topic> topicList = null;
		for (final Integer topicType : new Integer[]
		{ Constants.CONCEPTUALOVERVIEW_TAG_ID, Constants.TASK_TAG_ID })
		{
			LambdaJMatcher<Object> matcher = having(on(Topic.class).getTagIDs(), hasItem(topicType));

			/*
			 * add a limiter if we want the collection to include only the tags
			 * we have included
			 */
			if (limitToTags)
				matcher = matcher.and(having(on(Topic.class).getTagIDs().size(), equalTo(tagIds.size() + 1)));

			// build a list of inclusions
			for (final Integer includeTagId : tagIds)
				matcher = matcher.and(having(on(Topic.class).getTagIDs(), hasItem(includeTagId)));

			// build up a list of exclusions
			if (excludeTagIDs != null)
			{
				for (final Object excludeTagId : excludeTagIDs)
					matcher = matcher.and(not(having(on(Topic.class).getTagIDs(), hasItem(excludeTagId))));
			}

			final List<Topic> thisTopicList = filter(matcher, topicIdToTopicMap.values());

			if (topicList == null)
				topicList = thisTopicList;
			else
				topicList.addAll(thisTopicList);

		}

		return topicList;
	}

	/**
	 * This function builds a TOC that shows children tags in the TOC next to
	 * their parent names.
	 */
	@SuppressWarnings("serial")
	protected TocTopLevel buildTOCCommonNameTechnologyHybrid(final DocbookBuildingOptions docbookBuildingOptions)
	{
		/*
		 * todo: if this toc style of navigation ends up being the standard, a
		 * lot of the code here could be done smarter with recursive calls to
		 * functions that generate the listitems
		 */

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// these categories make up the folders in the tree view
		final Category firstLevelCategoryOne = entityManager.find(Category.class, Constants.TECHNOLOGY_CATEGORY_ID);
		final Category firstLevelCategoryTwo = entityManager.find(Category.class, Constants.COMMON_NAME_CATEGORY_ID);

		final Category secondLevelCategory = entityManager.find(Category.class, Constants.CONCERN_CATEGORY_ID);

		final List<TagToCategory> combinedTechnologyTagToCategories = new ArrayList<TagToCategory>(firstLevelCategoryOne.getTagToCategories());
		combinedTechnologyTagToCategories.addAll(firstLevelCategoryTwo.getTagToCategories());
		Collections.sort(combinedTechnologyTagToCategories);

		/*
		 * this container will hold those list items that appear in the top
		 * level of the treeview
		 */
		final TocTopLevel tocTopLevel = new TocTopLevel(docbookBuildingOptions, "EAP6 Documentation");

		// add the home link
		tocTopLevel.getChildren().add(new TocLink(docbookBuildingOptions, "HOME", "index"));

		// get a list of tags that are equivalent to another parent tag
		final List<Integer> childrenTags = new ArrayList<Integer>();
		for (final TagToCategory firstLevelCategoryTag : combinedTechnologyTagToCategories)
		{
			if (firstLevelCategoryTag.getTag().getParentTagToTags().size() != 0)
				childrenTags.add(firstLevelCategoryTag.getTag().getTagId());
		}

		// get a list of concern tags
		final List<Integer> concernTags = new ArrayList<Integer>();
		for (final TagToCategory concernTag : secondLevelCategory.getTagToCategories())
			concernTags.add(concernTag.getTag().getTagId());

		/*
		 * loop over the first level category. the tags in this category will
		 * appear as the top level items in the tree
		 */
		for (final TagToCategory firstLevelCategoryTag : combinedTechnologyTagToCategories)
		{
			// a convenience variable
			final Tag firstLevelTag = firstLevelCategoryTag.getTag();
			final Integer firstLevelTagId = firstLevelTag.getTagId();

			/*
			 * get a list of tags that are equivalent to this parent tag. we
			 * will use this to determine which topics get placed under the
			 * parent tag, and which get placed under the child tag (because a
			 * topic can be tagged with a child and parent tag, where the child
			 * tag is a child of another parent)
			 */
			final List<Integer> thisChildrenTags = new ArrayList<Integer>();
			for (final TagToTag childTag : firstLevelTag.getChildrenTagToTags())
				thisChildrenTags.add(childTag.getSecondaryTag().getTagId());

			// don't process child tags directly
			if (firstLevelTag.getParentTagToTags().size() == 0)
			{
				final TocFolderElement parentOnlyFolder = new TocFolderElement(docbookBuildingOptions, firstLevelTag.getTagName());

				// find those topics that *only* have a parent tag
				final List<Topic> tocTopicList = findTopicsForToc(new ArrayList<Integer>()
				{
					{
						add(firstLevelTagId);
					}
				}, new ArrayList<Integer>()
				{
					{
						addAll(thisChildrenTags);
						addAll(concernTags);
					}
				}, false);

				for (final Topic xmlMap : tocTopicList)
				{
					parentOnlyFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
				}

				// now loop over the concern tags for the parent tag only
				final List<TagToCategory> sortedCategories = new ArrayList<TagToCategory>(secondLevelCategory.getTagToCategories());
				Collections.sort(sortedCategories);

				for (final TagToCategory secondLevelCategoryTag : sortedCategories)
				{
					final Tag concernTag = secondLevelCategoryTag.getTag();
					final Integer concernTagId = concernTag.getTagId();

					final List<Topic> concernTopicList = findTopicsForToc(new ArrayList<Integer>()
					{
						{
							add(firstLevelTagId);
							add(concernTagId);
						}
					}, thisChildrenTags, false);

					if (concernTopicList.size() != 0)
					{
						final TocFolderElement concernLevelFolder = new TocFolderElement(docbookBuildingOptions, concernTag.getTagName());
						parentOnlyFolder.getChildren().add(concernLevelFolder);

						for (final Topic xmlMap : concernTopicList)
						{
							concernLevelFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
						}
					}
				}

				// add the folder to the toc if it holds any items
				if (parentOnlyFolder.getChildren().size() != 0)
					tocTopLevel.getChildren().add(parentOnlyFolder);

				/*****/

				// loop over the children tags
				for (final TagToTag childTagToTag : firstLevelTag.getChildrenTagToTags())
				{
					final Tag secondaryTag = childTagToTag.getSecondaryTag();

					/*
					 * this collection will hold the those list items that
					 * appear under a first level folder
					 */
					final TocFolderElement equivalentFolder = new TocFolderElement(docbookBuildingOptions, firstLevelTag.getTagName() + " / " + secondaryTag.getTagName());

					// add those topics that aren't tagged with a concern.

					/*
					 * here we don't actually use the equivalent tag when
					 * grabbing these topics. having the child tag implies
					 * having the main tag
					 */
					final List<Topic> topicList = findTopicsForToc(new ArrayList<Integer>()
					{
						{
							add(secondaryTag.getTagId());
						}
					}, concernTags, false);

					for (final Topic xmlMap : topicList)
					{
						equivalentFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
					}

					// now loop over the concern tags
					for (final TagToCategory secondLevelCategoryTag : secondLevelCategory.getTagToCategories())
					{
						final Tag concernTag = secondLevelCategoryTag.getTag();

						final List<Topic> concernTopicList = findTopicsForToc(new ArrayList<Integer>()
						{
							{
								add(secondaryTag.getTagId());
								add(concernTag.getTagId());
							}
						}, null, false);

						if (concernTopicList.size() != 0)
						{
							final TocFolderElement concernLevelFolder = new TocFolderElement(docbookBuildingOptions, concernTag.getTagName());
							equivalentFolder.getChildren().add(concernLevelFolder);

							for (final Topic xmlMap : concernTopicList)
							{
								concernLevelFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
							}
						}

					}

					// add the folder to the toc if it holds any items
					if (equivalentFolder.getChildren().size() != 0)
						tocTopLevel.getChildren().add(equivalentFolder);
				}
			}
		}

		tocTopLevel.generateCode();
		return tocTopLevel;
	}

	/**
	 * This function will return a TOC object where tags that are encompassed by
	 * a parent tag do not show up in the toc directly. Instead they are listed
	 * under their parent folders.
	 */
	@SuppressWarnings("serial")
	protected TocTopLevel buildTOCCommonNameOnly(final DocbookBuildingOptions docbookBuildingOptions)
	{
		/*
		 * todo: if this toc style of navigation ends up being the standard, a
		 * lot of the code here could be done smarter with recursive calls to
		 * functions that generate the listitems
		 */

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// these categories make up the folders in the tree view
		final Category firstLevelCategoryOne = entityManager.find(Category.class, Constants.TECHNOLOGY_CATEGORY_ID);
		final Category firstLevelCategoryTwo = entityManager.find(Category.class, Constants.COMMON_NAME_CATEGORY_ID);

		final Category secondLevelCategory = entityManager.find(Category.class, Constants.CONCERN_CATEGORY_ID);

		final List<TagToCategory> combinedTechnologyTagToCategories = new ArrayList<TagToCategory>(firstLevelCategoryOne.getTagToCategories());
		combinedTechnologyTagToCategories.addAll(firstLevelCategoryTwo.getTagToCategories());
		Collections.sort(combinedTechnologyTagToCategories);

		/*
		 * this container will hold those list items that appear in the top
		 * level of the treeview
		 */
		final TocTopLevel tocTopLevel = new TocTopLevel(docbookBuildingOptions, "EAP6 Documentation");

		// add the home link
		tocTopLevel.getChildren().add(new TocLink(docbookBuildingOptions, "HOME", "index"));

		// get a list of tags that are equivalent to another parent tag
		final List<Integer> childrenTags = new ArrayList<Integer>();
		for (final TagToCategory firstLevelCategoryTag : combinedTechnologyTagToCategories)
		{
			if (firstLevelCategoryTag.getTag().getParentTagToTags().size() != 0)
				childrenTags.add(firstLevelCategoryTag.getTag().getTagId());
		}

		// get a list of concern tags
		final List<Integer> concernTags = new ArrayList<Integer>();
		for (final TagToCategory concernTag : secondLevelCategory.getTagToCategories())
			concernTags.add(concernTag.getTag().getTagId());

		/*
		 * loop over the first level category. the tags in this category will
		 * appear as the top level items in the tree
		 */
		for (final TagToCategory firstLevelCategoryTag : combinedTechnologyTagToCategories)
		{
			// a convenience variable
			final Tag firstLevelTag = firstLevelCategoryTag.getTag();
			final Integer firstLevelTagId = firstLevelTag.getTagId();

			/*
			 * get a list of tags that are equivalent to this parent tag. we
			 * will use this to determine which topics get placed under the
			 * parent tag, and which get placed under the child tag (because a
			 * topic can be tagged with a child and parent tag, where the child
			 * tag is a child of another parent)
			 */
			final List<Integer> thisChildrenTags = new ArrayList<Integer>();
			for (final TagToTag childTag : firstLevelTag.getChildrenTagToTags())
				thisChildrenTags.add(childTag.getSecondaryTag().getTagId());

			// don't process child tags directly
			if (firstLevelTag.getParentTagToTags().size() == 0)
			{
				final TocFolderElement parentOnlyFolder = new TocFolderElement(docbookBuildingOptions, firstLevelTag.getTagName());

				// list of topic ids to match
				final List<Integer> matchTagIds = new ArrayList<Integer>();
				matchTagIds.add(firstLevelTagId);

				// list of topic ids to exclude
				final List<Integer> excludeTagIds = new ArrayList<Integer>();
				excludeTagIds.addAll(thisChildrenTags);
				excludeTagIds.addAll(concernTags);

				/*
				 * find those topics that *only* have a parent tag, and no
				 * concern tags. these will be placed at the top of the tree
				 */
				final List<Topic> parentOnlyTopicList = findTopicsForToc(matchTagIds, excludeTagIds, false);

				/*
				 * find those topics that have a child tag. these will be placed
				 * below the parent only topics
				 */
				final List<Topic> chidlrenTopicList = new ArrayList<Topic>();
				for (final Integer childTagID : thisChildrenTags)
				{
					final List<Integer> matchChildTagIds = new ArrayList<Integer>();
					matchChildTagIds.add(childTagID);

					final List<Integer> excludeChildTagIds = new ArrayList<Integer>();
					excludeChildTagIds.addAll(concernTags);

					final List<Topic> matchingTopics = findTopicsForToc(matchChildTagIds, excludeChildTagIds, false);

					CollectionUtilities.addAllThatDontExist(matchingTopics, chidlrenTopicList);
				}

				/* create links to those topics that have no concern */
				for (final List<Topic> tocTopicList : new ArrayList<List<Topic>>()
				{
					{
						add(parentOnlyTopicList);
						add(chidlrenTopicList);
					}
				})
				{
					for (final Topic xmlMap : tocTopicList)
					{
						parentOnlyFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
					}
				}

				// now loop over the concern tags
				final List<TagToCategory> sortedCategories = new ArrayList<TagToCategory>(secondLevelCategory.getTagToCategories());
				Collections.sort(sortedCategories);

				for (final TagToCategory secondLevelCategoryTag : sortedCategories)
				{
					final Tag concernTag = secondLevelCategoryTag.getTag();
					final Integer concernTagId = concernTag.getTagId();

					final List<Integer> matchConcernTagIds = new ArrayList<Integer>();
					matchConcernTagIds.add(firstLevelTagId);
					matchConcernTagIds.add(concernTagId);

					/*
					 * find those topics that have only the parent tag, and the
					 * concern tag
					 */
					final List<Topic> parentOnlyConcernTopicList = findTopicsForToc(matchConcernTagIds, thisChildrenTags, false);

					/*
					 * find those topics that have a child tag, and the concern
					 * tag
					 */
					final List<Topic> childrenConcernTopicList = new ArrayList<Topic>();
					for (final Integer childTagID : thisChildrenTags)
					{
						final List<Integer> matchChildTagIds = new ArrayList<Integer>();
						matchChildTagIds.add(childTagID);
						matchChildTagIds.add(concernTagId);

						final List<Topic> matchingTopics = findTopicsForToc(matchChildTagIds, null, false);

						CollectionUtilities.addAllThatDontExist(matchingTopics, childrenConcernTopicList);
					}

					if (parentOnlyConcernTopicList.size() != 0 || childrenConcernTopicList.size() != 0)
					{
						final TocFolderElement concernLevelFolder = new TocFolderElement(docbookBuildingOptions, concernTag.getTagName());
						parentOnlyFolder.getChildren().add(concernLevelFolder);

						final ArrayList<List<Topic>> topicCollections = new ArrayList<List<Topic>>();
						topicCollections.add(parentOnlyConcernTopicList);
						topicCollections.add(childrenConcernTopicList);

						for (List<Topic> topicList : topicCollections)
						{
							for (final Topic xmlMap : topicList)
							{
								concernLevelFolder.getChildren().add(new TocLink(docbookBuildingOptions, xmlMap.getTopicTitle(), xmlMap.getTopicId(), xmlMap.getTempNavLinkDocbook()));
							}
						}
					}
				}

				// add the folder to the toc if it holds any items
				if (parentOnlyFolder.getChildren().size() != 0)
					tocTopLevel.getChildren().add(parentOnlyFolder);
			}
		}

		tocTopLevel.generateCode();
		return tocTopLevel;
	}

	private void addErrorToTopic(final Topic topic, final String message)
	{
		addErrorToTopic(topic, message, true);
	}

	private void addErrorToTopic(final Topic topic, final String message, boolean error)
	{
		if (!this.errors.containsKey(topic))
			this.errors.put(topic, new TopicErrorData());

		final TopicErrorData topicErrorData = this.errors.get(topic);

		if (error)
			topicErrorData.getErrors().add(message);
		else
			topicErrorData.getWarnings().add(message);
	}

	private String getTopicSkynetURL(final Topic topic)
	{
		return Constants.SERVER_URL + "/TopicIndex/CustomSearchTopicList.seam?topicIds=" + topic.getTopicId();
	}
}