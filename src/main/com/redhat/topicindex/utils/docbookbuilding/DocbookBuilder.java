package com.redhat.topicindex.utils.docbookbuilding;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.selectFirst;
import static org.hamcrest.Matchers.equalTo;


import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToTag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTopic;
import com.redhat.topicindex.sort.TagToCategorySortingComparator;
import com.redhat.topicindex.sort.TocElementLabelComparator;
import com.redhat.topicindex.sort.TopicTitleComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.LineEndFormatter;
import com.redhat.topicindex.utils.XMLValidator;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocFolderElement;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocLink;
import com.redhat.topicindex.utils.docbookbuilding.toc.TocTopLevel;
import com.redhat.topicindex.utils.docbookbuilding.tocdatabase.TocTopicDatabase;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.TopicErrorData;
import com.redhat.topicindex.utils.structures.TopicErrorDatabase;
import com.redhat.topicindex.utils.structures.TopicImageData;

/**
 * This class is used to convert a collection of topics in a Publican Docbook
 * archive file that can be compiled into HTML
 */
public class DocbookBuilder
{
	/** The StringConstantsID that represents the Revision_History.xml file */
	private static final Integer REVISION_HISTORY_XML_ID = 15;
	/** The StringConstantsID that represents the Book.xml file */
	private static final Integer BOOK_XML_ID = 1;
	/** The StringConstantsID that represents the Book.ent file */
	private static final Integer BOOK_ENT_ID = 2;
	/** The StringConstantsID that represents the Book_Info.xml file */
	private static final Integer BOOK_INFO_XML_ID = 3;
	/** The StringConstantsID that represents the Author_Group.xml file */
	private static final Integer AUTHOR_GROUP_XML_ID = 4;
	/** The StringConstantsID that represents the publican.cfg file */
	private static final Integer PUBLICAN_CFG_ID = 5;
	/** The StringConstantsID that represents the icon.svg file */
	private static final Integer ICON_SVG_ID = 6;
	/** The StringConstantsID that represents the error topic xml template */
	private static final Integer ERROR_TOPIC_ID = 7;
	/** The StringConstantsID that represents the error topic xml template */
	private static final Integer ERRORTAGS_TOPIC_ID = 8;
	/** The StringConstantsID that represents the Makefile file */
	private static final Integer MAKEFILE_ID = 16;
	/** The StringConstantsID that represents the spec.in file */
	private static final Integer SPEC_IN_ID = 17;
	/** The StringConstantsID that represents the package.sh file */
	private static final Integer PACKAGE_SH_ID = 18;
	/** The StringConstantsID that represents the StartPage.xml file */
	private static final Integer START_PAGE_ID = 19;
	/** The StringConstantsID that represents the jboss.svg file */
	private static final Integer JBOSS_SVG_ID = 20;
	/** The StringConstantsID that represents the yahoo_dom_event.js file */
	private static final Integer YAHOO_DOM_EVENT_JS_ID = 21;
	/** The StringConstantsID that represents the treeview_min.js file */
	private static final Integer TREEVIEW_MIN_JS_ID = 22;
	/** The StringConstantsID that represents the treeview.css file */
	private static final Integer TREEVIEW_CSS_ID = 23;
	/** The StringConstantsID that represents the jquery_min.js file */
	private static final Integer JQUERY_MIN_JS_ID = 24;

	private static final Integer TREEVIEW_SPRITE_GIF_ID = 1;
	private static final Integer TREEVIEW_LOADING_GIF_ID = 2;
	private static final Integer CHECK1_GIF_ID = 3;
	private static final Integer CHECK2_GIF_ID = 4;
	private static final Integer FAILPENGUIN_PNG_ID = 5;

	private static final Integer PLUGIN_XML_ID = 25;
	private static final Integer ECLIPSE_PACKAGE_SH_ID = 26;
	private static final Integer PUBLICAN_ECLIPSE_CFG_ID = 27;

	/**
	 * The contents of the Revision_History.xml template, as pulled from the
	 * StringConstants table
	 */
	private String revisionHistoryXml;
	/**
	 * The contents of the Book.xml template, as pulled from the StringConstants
	 * table
	 */
	private String bookXml;
	/**
	 * The contents of the publican.cfg template, as pulled from the
	 * StringConstants table
	 */
	private String publicanCfg;
	/**
	 * The contents of the Author_Group.xml template, as pulled from the
	 * StringConstants table
	 */
	private String authorGroupXml;
	/**
	 * The contents of the Book_Info.xml template, as pulled from the
	 * StringConstants table
	 */
	private String bookInfoXml;
	/**
	 * The contents of the Book.ent template, as pulled from the StringConstants
	 * table
	 */
	private String bookEnt;
	/**
	 * The contents of the icon.svg template, as pulled from the StringConstants
	 * table
	 */
	private String iconSvg;
	/**
	 * The contents of the error topic template, as pulled from the
	 * StringConstants table
	 */
	private String errorTopic;
	/**
	 * The contents of the error topic (where some tags are missing) template,
	 * as pulled from the StringConstants table
	 */
	private String errorTagsTopic;

	private String makefile;
	private String spec_in;
	private String package_sh;
	private String startPage;
	private String jbossSvg;
	private String yahooDomEventJs;
	private String treeviewMinJs;
	private String treeviewCss;
	private String jqueryMinJs;
	private byte[] treeviewSpriteGif;
	private byte[] treeviewLoadingGif;
	private byte[] check1Gif;
	private byte[] check2Gif;
	private byte[] failpenguinPng;

	private String pluginXml;
	private String eclisePackageSh;
	private String publicanEclipseCfg;

	/** A marker to replace with the date in a string */
	private static final String DATE_YYMMDD_TEXT_MARKER = "#YYMMDD#";
	private static final String BOOK_XML_XI_INCLUDES_MARKER = "#XI_INCLUDES#";

	/** This marker is replaced with the SkyNet build version */
	private static final String BUILD_MARKER = "#SKYNETBUILD#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String ROLE_MARKER = "#ROLE#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String TAG_FILTER_URL_MARKER = "#TAGFILTERURL#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String TOPIC_ID_MARKER = "#TOPICID#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String TOPIC_ERROR_LINK_MARKER = "#TOPICERRORLINK#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String XREF_ID_MARKER = "#XREF#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String TOPIC_TITLE_MARKER = "#TOPICTITLE#";
	/**
	 * A marker that can be included in generic sections and replaced with
	 * specific data
	 */
	private static final String TOPIC_TEXT_MARKER = "#TOPICTEXT#";

	/**
	 * The Docbook role (which becomes a CSS class) for the Skynet build version
	 * para
	 */
	private static final String ROLE_BUILD_VERSION_PARA = "RoleBuildVersionPara";
	/**
	 * The Docbook role (which becomes a CSS class) for the Skynet topic link
	 * para
	 */
	private static final String ROLE_VIEW_IN_SKYNET_PARA = "RoleViewInSkyNetPara";
	/** The Docbook role (which becomes a CSS class) for the bug link para */
	private static final String ROLE_CREATE_BUG_PARA = "RoleCreateBugPara";

	/** A collection of topics that will go into the docbook build */
	private final TocTopicDatabase topicDatabase = new TocTopicDatabase();

	/**
	 * Holds the compiler errors that form the Errors.xml file in the compiled
	 * docbook
	 */
	private TopicErrorDatabase errorDatabase = new TopicErrorDatabase();

	/**
	 * Holds information on file url locations, which will be downloaded and
	 * included in the docbook zip file
	 */
	private ArrayList<TopicImageData> imageLocations = new ArrayList<TopicImageData>();

	private List<Node> getImages(final Node node)
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
	 * This function takes the list of topics and their related topics and
	 * injects xrefs
	 */
	private void processInjections(final List<Pair<Integer, String>> topicTypeTagDetails, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		System.out.println("Processing injection points");

		for (final Topic topic : topicDatabase.getTopics())
		{
			/*
			 * don't bother processing injection points for invalid topics
			 */
			if (!topic.isTempInvalidTopic())
			{
				/***************** PROCESS CUSTOM INJECTION POINTS *****************/

				/*
				 * keep a track of the topics we inject into custom locations,
				 * so we don't then inject them again
				 */
				final ArrayList<Integer> customInjectionIds = new ArrayList<Integer>();

				final String customInjectionErrors = XMLPreProcessor.processInjections(false, topic, customInjectionIds, topic.getTempTopicXMLDoc(), topicDatabase, docbookBuildingOptions);

				if (customInjectionErrors.length() != 0)
				{
					populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

					errorDatabase
							.addError(
									topic,
									"Topic references Topic(s) "
											+ customInjectionErrors
											+ " in a custom injection point, but these topic(s) have either not been related in the database, or was not matched by the filter. The later might occur if you are building a narrative and the injected topic was not listed in the Topic ID field, or you have not selected the 'Include all related topics' option.");
				}
				else
				{
					final String genericInjectionErrors = XMLPreProcessor.processGenericInjections(false, topic, topic.getTempTopicXMLDoc(), customInjectionIds, topicTypeTagDetails, topicDatabase, docbookBuildingOptions);

					if (genericInjectionErrors.length() != 0)
					{
						populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

						errorDatabase.addError(topic, "Topic relates to Topic(s) " + genericInjectionErrors
								+ ", but these topic(s) were not matched by the filter. The later might occur if you are building a narrative and the injected topic was not listed in the Topic ID field, or you have not selected the 'Include all related topics' option.");
					}
					else
					{
						/*
						 * make sure the xml is valid after all of our
						 * modifications
						 */
						postValidateTopicDocbook(topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
					}
				}
			}
		}

		System.out.println("Processing injection points 100% done");
	}

	/**
	 * This function takes the list of topics and their related topics and
	 * injects xrefs
	 */
	private void processFragmentInjections(final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		System.out.println("Processing topic fragment injections");

		for (final Topic topic : topicDatabase.getTopics())
		{
			/*
			 * don't bother processing injection points for invalid topics
			 */
			if (!topic.isTempInvalidTopic())
			{
				final String contentFragmentInjectionErrors = XMLPreProcessor.processTopicContentFragments(topic, topic.getTempTopicXMLDoc(), docbookBuildingOptions);
				final String titleFramgmentInjectionErrors = XMLPreProcessor.processTopicTitleFragments(topic, topic.getTempTopicXMLDoc(), docbookBuildingOptions);

				if (contentFragmentInjectionErrors.length() != 0 || titleFramgmentInjectionErrors.length() != 0)
				{
					populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

					/* remove the last ", " from the error string */
					final String injectionErrors = contentFragmentInjectionErrors + (contentFragmentInjectionErrors.length() != 0 ? ", " : "") + titleFramgmentInjectionErrors;
					errorDatabase
							.addError(
									topic,
									"Topic references Topic(s) "
											+ injectionErrors
											+ " in a topic fragment injection point, but this topic has either not been related in the database, or was not matched by the filter. The later might occur if you are building a narrative and the injected topic was not listed in the Topic ID field, or you have not selected the 'Include all related topics' option.");
				}
			}
		}

		System.out.println("Processing topic fragment injections 100% done");
	}

	private byte[] getStringBytes(final String input)
	{
		return input == null ? new byte[]
		{} : input.getBytes();
	}

	private void addImagesToBook(final HashMap<String, byte[]> files)
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
						errorDatabase.addError(imageLocation.getTopic(), "ImageFile ID " + topicID + " from image location " + imageLocation + " was not found!");
						System.out.println("ImageFile ID " + topicID + " from image location " + imageLocation + " was not found!");
					}
				}
				catch (final Exception ex)
				{
					success = false;
					errorDatabase.addError(imageLocation.getTopic(), imageLocation + " is not a valid image. Must be in the format [ImageFileID].extension e.g. 123.png, or images/321.jpg");
					SkynetExceptionUtilities.handleException(ex, true, "Probably some invalid XML defining the image");
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

		if (errorDatabase.hasItems())
		{
			for (final TopicErrorData topicErrorData : errorDatabase.getErrors())
			{
				final Topic topic = topicErrorData.getTopic();

				final List<String> topicErrorItems = new ArrayList<String>();

				final String tags = topic.getCommaSeparatedTagList();
				final String url = getTopicSkynetURL(topic);

				topicErrorItems.add(DocbookUtils.buildListItem("INFO: " + tags));
				topicErrorItems.add(DocbookUtils.buildListItem("INFO: <ulink url=\"" + url + "\">Topic URL</ulink>"));

				for (final String error : topicErrorData.getItemsOfType(TopicErrorDatabase.ERROR))
					topicErrorItems.add(DocbookUtils.buildListItem("ERROR: " + error));

				for (final String warning : topicErrorData.getItemsOfType(TopicErrorDatabase.WARNING))
					topicErrorItems.add(DocbookUtils.buildListItem("WARNING: " + warning));

				/*
				 * this should never be false, because a topic will only be
				 * listed in the errors collection once a error or warning has
				 * been added. The count of 2 comes from the standard list items
				 * we added above for the tags and url.
				 */
				if (topicErrorItems.size() > 2)
				{
					final String title = "Topic ID " + topic.getTopicId();
					final String id = DocbookUtils.ERROR_XREF_ID_PREFIX + topic.getTopicId();

					errorItemizedLists += DocbookUtils.wrapListItems(topicErrorItems, title, id);
				}
			}
		}
		else
		{
			errorItemizedLists = "<para>No Errors Found</para>";
		}

		return DocbookUtils.buildChapter(errorItemizedLists, "Compiler Output");
	}

	/**
	 * This function takes the XML files that have been generated by the Docbook
	 * compilation process and packages them up with some static Docbook Strings
	 * to produce a ZIP file that is sent to the user
	 */
	private byte[] buildZipFile(final List<TagToCategory> topicTypeTagIDs, final List<TagToCategory> tagToCategories, final List<String> usedIds, final TocTopLevel tocTopLevel, final DocbookBuildingOptions docbookBuildingOptions)
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
		String publicnCfgFixed = publicanCfg;

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

		// fix the Publican CFG file
		if (publicnCfgFixed != null)
		{
			if (docbookBuildingOptions.getPublicanShowRemarks())
			{
				publicnCfgFixed += "\nshow_remarks: 1";
			}

			publicnCfgFixed += "\ncvs_pkg: " + docbookBuildingOptions.getCvsPkgOption();
		}

		// add the publican.cfg file
		files.put("Book/publican.cfg", getStringBytes(publicnCfgFixed));

		// add the files that are used to package up the RPM file
		files.put("Book/package.sh", getStringBytes(LineEndFormatter.convertToLinuxLineEndings(package_sh)));

		// the make file is built up from options supplied from the user
		String makefileFixed = makefile;
		makefileFixed = "RELEASE = " + docbookBuildingOptions.getMakefileReleaseOption() + "\n" + makefileFixed;
		makefileFixed = "VERSION = " + docbookBuildingOptions.getMakefileVersionOption() + "\n" + makefileFixed;
		makefileFixed = "BOOKS = " + docbookBuildingOptions.getMakefileBooksOption() + "\n" + makefileFixed;
		makefileFixed = "LANG = " + docbookBuildingOptions.getMakefileLangOption() + "\n" + makefileFixed;
		makefileFixed = "PROD_VERSION = " + docbookBuildingOptions.getMakefileProdVersionOption() + "\n" + makefileFixed;
		makefileFixed = "PRODUCT = " + docbookBuildingOptions.getMakefileProductOption() + "\n" + makefileFixed;
		files.put("Book/packager/en-US/Makefile", getStringBytes(makefileFixed));

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

		/*
		 * build the middle of the Book.xml file, where we include references to
		 * the topic type pages that were built above
		 */
		String bookXmlXiIncludes = "";

		if (!docbookBuildingOptions.getSuppressErrorsPage())
		{
			files.put("Book/en-US/Errors.xml", getStringBytes(compilerOutput == null ? "" : compilerOutput));
			bookXmlXiIncludes += "	<xi:include href=\"Errors.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
		}

		/*
		 * only reference the Toc.xml file if we are not building a narrative
		 * book
		 */
		if (!docbookBuildingOptions.getBuildNarrative())
		{
			bookXmlXiIncludes += "	<xi:include href=\"Toc.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />";
			bookXmlXiIncludes += "	<xi:include href=\"StartPage.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />";
		}

		/* add an xml file for each topic TODO: ordering for narrative style */
		String topics = "";
		for (final Topic topic : topicDatabase.getTopics())
		{
			final String fileName = "Topic" + topic.getTopicId() + ".xml";
			files.put("Book/en-US/" + fileName, DocbookUtils.addXMLBoilerplate(topic.getTempTopicXMLDocString()).getBytes());
			topics += "	<xi:include href=\"" + fileName + "\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";
		}

		topics = DocbookUtils.addXMLBoilerplate(DocbookUtils.buildChapter(topics, ""));
		files.put("Book/en-US/Topics.xml", topics.getBytes());

		bookXmlXiIncludes += "	<xi:include href=\"Topics.xml\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" />\n";

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
			SkynetExceptionUtilities.handleException(ex, false, "Probably a stream error");
		}

		return zipFile;
	}

	/**
	 * This function takes the topics that match the filter tags selected by the
	 * user and processes them to validate the xml in the topic SVN repo,
	 * process the related topics and fix up image paths
	 */
	private void processTopics(final Filter filter, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final int roleCategoryID, final String searchTagsUrl,
			final List<TagToCategory> tagToCategories, final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final List<Topic> topicList = EntityUtilities.getTopicsFromFilter(filter);

		/*
		 * loop through each topic, and save the relevant information like type,
		 * related topics etc
		 */
		for (final Topic topic : topicList)
			processTopic(topic, mandatoryExclusiveTagCollections, mandatoryTagCollections, roleCategoryID, searchTagsUrl, tagToCategories, usedIds, docbookBuildingOptions);

	}

	/**
	 * Loads the strings for the file templates from the database
	 */
	private void loadConstantsFromDB()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		revisionHistoryXml = EntityUtilities.loadStringConstant(entityManager, REVISION_HISTORY_XML_ID);
		bookXml = EntityUtilities.loadStringConstant(entityManager, BOOK_XML_ID);
		publicanCfg = EntityUtilities.loadStringConstant(entityManager, PUBLICAN_CFG_ID);
		authorGroupXml = EntityUtilities.loadStringConstant(entityManager, AUTHOR_GROUP_XML_ID);
		bookInfoXml = EntityUtilities.loadStringConstant(entityManager, BOOK_INFO_XML_ID);
		bookEnt = EntityUtilities.loadStringConstant(entityManager, BOOK_ENT_ID);
		iconSvg = EntityUtilities.loadStringConstant(entityManager, ICON_SVG_ID);

		errorTopic = EntityUtilities.loadStringConstant(entityManager, ERROR_TOPIC_ID);
		errorTagsTopic = EntityUtilities.loadStringConstant(entityManager, ERRORTAGS_TOPIC_ID);

		makefile = EntityUtilities.loadStringConstant(entityManager, MAKEFILE_ID);
		spec_in = EntityUtilities.loadStringConstant(entityManager, SPEC_IN_ID);
		package_sh = EntityUtilities.loadStringConstant(entityManager, PACKAGE_SH_ID);

		startPage = EntityUtilities.loadStringConstant(entityManager, START_PAGE_ID);
		jbossSvg = EntityUtilities.loadStringConstant(entityManager, JBOSS_SVG_ID);

		yahooDomEventJs = EntityUtilities.loadStringConstant(entityManager, YAHOO_DOM_EVENT_JS_ID);
		treeviewMinJs = EntityUtilities.loadStringConstant(entityManager, TREEVIEW_MIN_JS_ID);
		treeviewCss = EntityUtilities.loadStringConstant(entityManager, TREEVIEW_CSS_ID);
		jqueryMinJs = EntityUtilities.loadStringConstant(entityManager, JQUERY_MIN_JS_ID);

		treeviewSpriteGif = EntityUtilities.loadBlobConstant(entityManager, TREEVIEW_SPRITE_GIF_ID);
		treeviewLoadingGif = EntityUtilities.loadBlobConstant(entityManager, TREEVIEW_LOADING_GIF_ID);
		check1Gif = EntityUtilities.loadBlobConstant(entityManager, CHECK1_GIF_ID);
		check2Gif = EntityUtilities.loadBlobConstant(entityManager, CHECK2_GIF_ID);
		failpenguinPng = EntityUtilities.loadBlobConstant(entityManager, FAILPENGUIN_PNG_ID);

		pluginXml = EntityUtilities.loadStringConstant(entityManager, PLUGIN_XML_ID);
		eclisePackageSh = EntityUtilities.loadStringConstant(entityManager, ECLIPSE_PACKAGE_SH_ID);
		publicanEclipseCfg = EntityUtilities.loadStringConstant(entityManager, PUBLICAN_ECLIPSE_CFG_ID);
	}

	@SuppressWarnings("unchecked")
	private List<TagToCategory> getTagToCategories()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<TagToCategory> tagToCategories = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();
		return tagToCategories;
	}

	public byte[] buildDocbookZipFile(final Filter filter, final DocbookBuildingOptions docbookBuildingOptions)
	{
		loadConstantsFromDB();

		final List<TagToCategory> tagToCategories = getTagToCategories();

		/*
		 * get a list of the tag to category mappings. we will pass this
		 * collection through various function, which can then use the
		 * getTagToCatgeories function to filter it for the tags that are mapped
		 * to a specific category
		 */

		final List<TagToCategory> topicTypeTagIDs = getTagToCatgeories(Constants.TYPE_CATEGORY_ID, tagToCategories);
		final List<TagToCategory> technologyCommonNameTagIDs = getTagToCatgeories(Constants.TECHNOLOGY_CATEGORY_ID, tagToCategories);
		technologyCommonNameTagIDs.addAll(getTagToCatgeories(Constants.COMMON_NAME_CATEGORY_ID, tagToCategories));
		final List<TagToCategory> concernTagIDs = getTagToCatgeories(Constants.CONCERN_CATEGORY_ID, tagToCategories);

		final List<Pair<Integer, String>> topicTypeTagDetails = new ArrayList<Pair<Integer, String>>();
		topicTypeTagDetails.add(Pair.newPair(Constants.TASK_TAG_ID, Constants.TASK_TAG_NAME));
		topicTypeTagDetails.add(Pair.newPair(Constants.REFERENCE_TAG_ID, Constants.REFERENCE_TAG_NAME));
		topicTypeTagDetails.add(Pair.newPair(Constants.CONCEPT_TAG_ID, Constants.CONCEPT_TAG_NAME));
		topicTypeTagDetails.add(Pair.newPair(Constants.CONCEPTUALOVERVIEW_TAG_ID, Constants.CONCEPTUALOVERVIEW_TAG_NAME));

		/*
		 * All topics require a topic type, and only one can be assigned
		 */
		final ArrayList<List<TagToCategory>> mandatoryExclusiveTags = new ArrayList<List<TagToCategory>>();
		mandatoryExclusiveTags.add(topicTypeTagIDs);

		/*
		 * All topics need at least a technology or common name along with a
		 * concern
		 */
		final ArrayList<List<TagToCategory>> mandatoryTags = new ArrayList<List<TagToCategory>>();
		mandatoryTags.add(technologyCommonNameTagIDs);
		mandatoryTags.add(concernTagIDs);

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
		 * this collection holds all the id attributes used by the topics we use
		 * this list to look for duplicates which would cause docbook
		 * compilation errors
		 */
		final List<String> usedIds = new ArrayList<String>();

		/*
		 * build an initial list of "root" topics from the search page to be
		 * processed
		 */
		processTopics(filter, mandatoryExclusiveTags, mandatoryTags, Constants.LIFECYCLE_CATEGORY_ID, searchTagsUrl, tagToCategories, usedIds, docbookBuildingOptions);

		/*
		 * add a collection of tag description topics
		 */
		final TocTopLevel retValue = buildTOCAndLandingPages(usedIds, docbookBuildingOptions);

		/* add the bread crumbs to the topics */
		final List<Topic> normalTopics = topicDatabase.getNonLandingPageTopics();
		for (final Topic topic : normalTopics)
			processTopicBreadCrumbs(topic);

		/*
		 * take the information gathered by the processTopic() function, and use
		 * it to dynamically inject xrefs
		 */
		processInjections(topicTypeTagDetails, searchTagsUrl, Constants.LIFECYCLE_CATEGORY_ID, tagToCategories, docbookBuildingOptions);

		/*
		 * inject the topic fragments
		 */
		processFragmentInjections(searchTagsUrl, Constants.LIFECYCLE_CATEGORY_ID, tagToCategories, docbookBuildingOptions);

		/*
		 * go through and sort the topicList collection into the same order as
		 * the topic id filter field
		 */
		final List<Integer> buildOrder = new ArrayList<Integer>();
		if (docbookBuildingOptions.getBuildNarrative())
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
						SkynetExceptionUtilities.handleException(ex, true, "Probably some invalid code in the XML injection point");
					}
				}
			}
		}

		// now build the publican zip file that will be sent to the user
		return buildZipFile(topicTypeTagIDs, tagToCategories, usedIds, retValue, docbookBuildingOptions);
	}

	private Topic buildLandingPageTopic(final List<Tag> templateTags, final Integer topicId, final String title, final List<String> usedIds, final boolean processedOnly)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		Topic template = null;

		/* First, search the topicDatabase */
		final List<Topic> matchingExistingTopics = topicDatabase.getMatchingTopicsFromTag(templateTags);
		template = matchingExistingTopics.size() != 0 ? matchingExistingTopics.get(0) : null;

		/* if that fails, search the database */
		if (template == null && !processedOnly)
		{
			/*
			 * Try to find a topic in the database that can be used as a
			 * template for this landing page. Build up a Filter object, as this
			 * provides a convenient way to build a query that will get us the
			 * topics we need
			 */
			final Filter filter = new Filter();

			for (final Tag templateTag : templateTags)
			{
				final FilterTag filterTag = new FilterTag();
				filterTag.setTag(templateTag);
				filterTag.setTagState(Constants.MATCH_TAG_STATE);
				filter.getFilterTags().add(filterTag);
			}

			final String query = filter.buildQuery();

			final List<Topic> templates = entityManager.createQuery(query).getResultList();

			template = templates.size() != 0 ? templates.get(0) : null;
		}

		/*
		 * We have topics that match this intersection, so we need to build a
		 * landing page
		 */
		final Topic landingPage = new Topic();
		landingPage.setTopicId(topicId);
		landingPage.setTopicTitle(title);

		for (final Tag tag : templateTags)
			landingPage.addTag(tag);

		/*
		 * Apply the xml from the template topic, or a generic template if not
		 * template topic exists
		 */
		if (template != null)
		{
			/* copy the xml */
			landingPage.setTopicXML(template.getTopicXML());
			/* fix the title */
			landingPage.syncXML();
			/* convert the xml to a Document object */
			landingPage.initializeTempTopicXMLDoc();
			/*
			 * the temporary landing page topics gets the templates outgoing
			 * relationships
			 */
			landingPage.setParentTopicToTopics(template.getParentTopicToTopics());
		}

		/*
		 * if validation fails at this point the template in the database is not
		 * valid, so revert to a base template
		 */
		if (template == null || (!validateTopicXML(landingPage) || validateIdAttributesUnique(landingPage, usedIds) != null))
		{
			landingPage.getParentTopicToTopics().clear();
			landingPage.setTopicXML("<section><title></title><para></para></section>");
			landingPage.syncXML();
			landingPage.initializeTempTopicXMLDoc();
		}

		/*
		 * Apply some of the standard fixes to the landing page topics
		 */
		processTopicID(landingPage);
		processTopicFixImages(landingPage);

		/* Add the landing page to the topic pool */
		topicDatabase.addTopic(landingPage);

		return landingPage;

	}

	/**
	 * This function will take a look at the topics that have been added to the
	 * topicDatabase, and create new Topics for landing pages for the
	 * intersection between the technology / common name tags and concern tags.
	 * 
	 * TODO: should this style of navigation be accepted, this function should
	 * be split up into smaller, more specific and more descriptive functions.
	 * 
	 * @param usedIds
	 */
	private TocTopLevel buildTOCAndLandingPages(final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final TocTopLevel tocTopLevel = new TocTopLevel(docbookBuildingOptions);

		try
		{
			System.out.println("Processing landing pages");

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			/* Get a reference to the tag description tag */
			final Tag tagDescription = entityManager.find(Tag.class, Constants.TAG_DESCRIPTION_TAG_ID);

			/* Get a reference to the home tag */
			final Tag home = entityManager.find(Tag.class, Constants.HOME_TAG_ID);

			/*
			 * Get a reference to the other topic type tags that should be
			 * displayed in the landing pages. Here we are defining that the
			 * task and overview topics will be displayed.
			 */
			final List<Tag> topicTypes = new ArrayList<Tag>();
			topicTypes.add(entityManager.find(Tag.class, Constants.TASK_TAG_ID));
			topicTypes.add(entityManager.find(Tag.class, Constants.CONCEPTUALOVERVIEW_TAG_ID));
			if (!docbookBuildingOptions.getTaskAndOverviewOnly())
			{
				topicTypes.add(entityManager.find(Tag.class, Constants.CONCEPT_TAG_ID));
				topicTypes.add(entityManager.find(Tag.class, Constants.REFERENCE_TAG_ID));
			}

			/* The categories that make up the top level of the toc */
			final List<Integer> techCommonNameCategories = CollectionUtilities.toArrayList(Constants.COMMON_NAME_CATEGORY_ID, Constants.TECHNOLOGY_CATEGORY_ID);

			/* The categories that make up the second level of the toc */
			final List<Integer> concernCatgeory = CollectionUtilities.toArrayList(Constants.CONCERN_CATEGORY_ID);

			/*
			 * Get the tags that have been applied to topics in this build from
			 * the tech and common name categories
			 */
			final List<Tag> techCommonNameTags = topicDatabase.getTagsFromCategories(techCommonNameCategories);

			/*
			 * Get the tags that have been applied to topics in this build from
			 * the tech and common name categories
			 */
			final List<Tag> concernTags = topicDatabase.getTagsFromCategories(concernCatgeory);

			/*
			 * Landing pages are just topics, but with negative ids to
			 * distinguish them from opics pulled out of the database
			 */
			int nextLandingPageId = -1;

			/*
			 * Build the home landing page. This page will be renamed to
			 * index.html by the docbook compilation script.
			 * 
			 * The home landing page will always have an ID of -1.
			 */

			final String homeLinkLabel = "HOME";
			buildLandingPageTopic(CollectionUtilities.toArrayList(home), nextLandingPageId, homeLinkLabel, usedIds, true);
			tocTopLevel.addChild(new TocLink(docbookBuildingOptions, homeLinkLabel, nextLandingPageId + "", DocbookUtils.buildULinkListItem("index.html", homeLinkLabel)));

			/*
			 * decrement the nextLandingPageId counter, so the landing page
			 * topics start with an id of -2
			 */
			--nextLandingPageId;

			/* Loop over all the tech and common name tags */
			for (final Tag techCommonNameTag : techCommonNameTags)
			{
				/* This collection will hold the tock links to the landing pages */
				final List<TocLink> landingPageLinks = new ArrayList<TocLink>();

				/* Loop over all the concern tags */
				for (final Tag concernTag : concernTags)
				{
					/*
					 * tags that are encompassed by another tag don't show up in
					 * the toc
					 */
					final boolean techCommonNameTagHasParent = techCommonNameTag.getParentTagToTags().size() != 0;
					final boolean concernTagHasParent = concernTag.getParentTagToTags().size() != 0;

					if (techCommonNameTagHasParent || concernTagHasParent)
						continue;

					/*
					 * See if we have topics that match this intersection of
					 * tech / common name and concern. We also have to deal with
					 * those tags that these technology and concern tags
					 * encompass. To do this we build up a list of tags that
					 * includes the technology parent and all it's children, and
					 * the concern parent and all it's children. We will then
					 * check each tech tag against each concern tag.
					 */

					/* build up the list of tech tags */
					final List<Tag> techCommonNameTagAndChildTags = new ArrayList<Tag>();
					techCommonNameTagAndChildTags.add(techCommonNameTag);
					for (final TagToTag childTechCommonNameTag : techCommonNameTag.getChildrenTagToTags())
					{
						final Tag childTag = childTechCommonNameTag.getSecondaryTag();
						techCommonNameTagAndChildTags.add(childTag);
					}

					/* build up a list of concern tags */
					final List<Tag> concernTagAndChildTags = new ArrayList<Tag>();
					concernTagAndChildTags.add(concernTag);
					for (final TagToTag childConcernTag : concernTag.getChildrenTagToTags())
					{
						final Tag childTag = childConcernTag.getSecondaryTag();
						concernTagAndChildTags.add(childTag);
					}

					/*
					 * find those topics that are tagged with any of the tech
					 * tags and any of the concern tags, and a topic type tag
					 */
					final List<Topic> matchingTopics = new ArrayList<Topic>();

					for (final Tag topicType : topicTypes)
					{
						for (final Tag techTagOrChild : techCommonNameTagAndChildTags)
						{
							for (final Tag concernTagOrChild : concernTagAndChildTags)
							{
								final List<Topic> thisMatchingTopics = topicDatabase.getMatchingTopicsFromTag(CollectionUtilities.toArrayList(techTagOrChild, concernTagOrChild, topicType));
								CollectionUtilities.addAllThatDontExist(thisMatchingTopics, matchingTopics);
							}
						}
					}

					/* build a landing page for these topics (if any were found */
					if (matchingTopics.size() != 0)
					{
						/* Sort the topics by title */
						Collections.sort(matchingTopics, new TopicTitleComparator());

						/* define a title for the landing page */
						final String landingPageTitle = techCommonNameTag.getTagName() + " > " + concernTag.getTagName();

						/*
						 * we have found topics that fall into this intersection
						 * of technology / common name and concern tags. create
						 * a link in the treeview
						 */
						landingPageLinks.add(new TocLink(docbookBuildingOptions, concernTag.getTagName(), nextLandingPageId + ""));

						/*
						 * We have topics that match this intersection, so we
						 * need to build a landing page
						 */
						final Topic landingPage = buildLandingPageTopic(CollectionUtilities.toArrayList(techCommonNameTag, concernTag, tagDescription), nextLandingPageId, landingPageTitle, usedIds, false);

						/*
						 * Insert some links to those topics that have both the
						 * technology / common name tag and the concern tag
						 */
						final List<Node> listitems = new ArrayList<Node>();
						for (final Topic matchingTopic : matchingTopics)
							listitems.add(DocbookUtils.createRelatedTopicXRef(landingPage.getTempTopicXMLDoc(), matchingTopic.getXRefID()));
						final Node itemizedlist = DocbookUtils.wrapListItems(landingPage.getTempTopicXMLDoc(), listitems);
						landingPage.getTempTopicXMLDoc().getDocumentElement().appendChild(itemizedlist);

						/* Decrement the landing page topic id counter */
						--nextLandingPageId;
					}
				}

				/* test to see if there were any topics to add under this tech */
				if (landingPageLinks.size() != 0)
				{
					/*
					 * if so, create a folder, and add all of the concern links
					 * to it
					 */
					final TocFolderElement tocFolder = new TocFolderElement(docbookBuildingOptions, techCommonNameTag.getTagName());
					tocFolder.getChildren().addAll(landingPageLinks);
					tocFolder.sortChildren(new TocElementLabelComparator(true));
					/* add the tech folder to the top level folder */
					tocTopLevel.getChildren().add(tocFolder);
				}

			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error generating the TOC");
		}

		tocTopLevel.sortChildren(new TocElementLabelComparator(true));
		tocTopLevel.generateCode();
		return tocTopLevel;
	}

	private Document buildErrorTopic(final String template, final Topic xmlData, final String xref, final String filterUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		String retValue = template;

		// include a link to the error page if it has not been suppressed
		if (docbookBuildingOptions.getSuppressErrorsPage())
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
	private void populateIdXMLDataFromDB(final String errorTemplate, final Topic xmlData, final String filterUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final String replacement = xmlData.getTempTopicRole() == null ? "" : xmlData.getTempTopicRole();
		final String fixedTemplate = errorTemplate.replaceAll(ROLE_MARKER, replacement);
		populateIdXMLDataFromDB(fixedTemplate, xmlData, filterUrl, docbookBuildingOptions);
	}

	/**
	 * Populates the template with the topics information.
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
	private void populateIdXMLDataFromDB(final String errorTemplate, final Topic xmlData, final String filterUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		xmlData.setTempTopicXMLDoc(buildErrorTopic(errorTemplate, xmlData, xmlData.getXRefID(), filterUrl, docbookBuildingOptions));
		xmlData.setTempInvalidTopic(true);
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
	private boolean postValidateTopicDocbook(final Topic topic, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
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

			populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
			System.out.println(topic.getTopicId().toString() + " failed docbook validation after the injection points were processed.");

			errorDatabase.addError(topic, "Topic failed docbook validation after the injection points were processed. The error was <emphasis>" + validator.getErrorText() + "</emphasis> The XML after the injections points were processed is: <programlisting><![CDATA[" + originalXML + "]]></programlisting>");
			return false;
		}

		return true;
	}

	private boolean validateTopicXMLText(final Topic topic)
	{
		// see if we have some xml to work with
		return !(topic.getTopicXML() == null || topic.getTopicXML().trim().length() == 0);
	}

	private boolean validateTopicXMLDocument(final Topic topic)
	{
		/*
		 * if the xml is not blank, but the tempTopicXMLDoc variable is null, we
		 * must have had some invalid xml
		 */
		return topic.getTempTopicXMLDoc() != null;
	}

	private boolean validateTopicXML(final Topic topic)
	{
		return validateTopicXMLText(topic) && validateTopicXMLDocument(topic);
	}

	/**
	 * Test to make sure the XML is valid. This is done by getting Xerces to
	 * parse the xml, and if no exceptions and thrown we assume the xml is ok.
	 * In an ideal world, we should be able to assume that the xml coming from
	 * the repo is valid. But until the topic tool implements some kind of
	 * validation, this step is required.
	 */
	private boolean validateTopicXML(final Topic topic, final String searchTagsUrl, final int roleCategoryID, final List<TagToCategory> tagToCategories, final DocbookBuildingOptions docbookBuildingOptions)
	{
		// see if we have some xml to work with
		if (!validateTopicXMLText(topic))
		{
			populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);
			errorDatabase.addError(topic, "Topic has a blank Topic XML field");
			return false;
		}

		/*
		 * if the xml is not blank, but the tempTopicXMLDoc variable is null, we
		 * must have had some invalid xml
		 */
		if (!validateTopicXMLDocument(topic))
		{
			populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions);

			errorDatabase.addError(topic, "Topic failed XML validation.");
			return false;
		}

		return true;
	}

	private String validateIdAttributesUnique(final Topic topic, final List<String> usedIds)
	{
		final Document document = topic.getTempTopicXMLDoc();
		final Element root = document.getDocumentElement();
		final NodeList elements = root.getChildNodes();

		for (int i = 0; i < elements.getLength(); ++i)
		{
			final String retValue = validateIdAttributesUnique(topic, elements.item(i), usedIds);
			if (retValue != null)
				return retValue;
		}

		return null;
	}

	private boolean validateIdAttributesUnique(final Topic topic, final String searchTagsUrl, final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		final String retValue = validateIdAttributesUnique(topic, usedIds);

		if (retValue != null)
		{
			populateIdXMLDataFromDB(errorTopic, topic, searchTagsUrl, docbookBuildingOptions);
			errorDatabase.addError(topic, "Topic uses an id attribute called \"" + retValue + "\" that is already used in another topic.");
			return false;
		}

		return true;
	}

	private String validateIdAttributesUnique(final Topic xmlData, final Node node, final List<String> usedIds)
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
			final String retValue = validateIdAttributesUnique(xmlData, elements.item(i), usedIds);
			if (retValue != null)
				return retValue;
		}

		return null;
	}

	/**
	 * Find the highest priority topic lifecycle tag assigned to a topic and
	 * returns it. This is used when assigning a role to section element.
	 */
	private String getTopicLifecycleRole(final Topic topic, final int roleCategoryID, final List<TagToCategory> tagToCategories)
	{
		final List<TagToCategory> tags = getTagToCatgeories(roleCategoryID, tagToCategories);

		Collections.sort(tags, new TagToCategorySortingComparator());

		for (final TagToCategory tagToCat : tags)
		{
			final Integer tagID = tagToCat.getTag().getTagId();

			if (topic.isTaggedWith(tagID))
				return tagToCat.getTag().getTagName().replaceAll(" ", "");
		}

		return "";
	}

	/**
	 * Assign a role attribute to the section element of the docbook tag based
	 * on the highest priority topic lifecycle tag assigned to the topic. This
	 * role can then be used to highlight topics when they are compiled to HTML
	 */
	private void processTopicLifecycleRole(final Topic topic, final int roleCategoryID, final List<TagToCategory> tagToCategories)
	{
		final String topicTagRole = getTopicLifecycleRole(topic, roleCategoryID, tagToCategories);
		if (topicTagRole.length() != 0)
			topic.getTempTopicXMLDoc().getDocumentElement().setAttribute("role", topicTagRole);
	}

	/**
	 * Add a note to those topics that do not have the written tag assigned to
	 * them
	 */
	private void processTopicDraftWarning(final Topic topic)
	{
		// if this topic has not been marked as final, add a note
		if (!topic.isTaggedWith(Constants.TOPIC_FINAL_LIFECYCLE))
		{
			final Element note = topic.getTempTopicXMLDoc().createElement("note");
			final Element title = topic.getTempTopicXMLDoc().createElement("title");
			title.setTextContent("Topic is still in a draft state");
			note.appendChild(title);
			final Element para = topic.getTempTopicXMLDoc().createElement("para");
			para.setTextContent("This topic is still in a draft state. The contents below are subject to change");
			note.appendChild(para);

			final NodeList nodes = topic.getTempTopicXMLDoc().getDocumentElement().getChildNodes();
			for (int i = 0; i < nodes.getLength(); ++i)
			{
				final Node node = nodes.item(i);
				if (node.getNodeType() == 1 && !node.getNodeName().equals("title"))
				{
					topic.getTempTopicXMLDoc().getDocumentElement().insertBefore(note, node);
					break;
				}
			}
		}
	}

	/**
	 * Sets the topic xref id to the topic database id
	 */
	private void processTopicID(final Topic topic)
	{
		topic.getTempTopicXMLDoc().getDocumentElement().setAttribute("id", topic.getXRefID());
	}

	/**
	 * Adds some debug information and links to the end of the topic
	 */
	private void processTopicAdditionalInfo(final Topic topic, final String searchTagsUrl, final DocbookBuildingOptions docbookBuildingOptions)
	{
		// SIMPLESECT TO HOLD OTHER LINKS
		final Element bugzillaSection = topic.getTempTopicXMLDoc().createElement("simplesect");
		topic.getTempTopicXMLDoc().getDocumentElement().appendChild(bugzillaSection);

		final Element bugzillaSectionTitle = topic.getTempTopicXMLDoc().createElement("title");
		bugzillaSectionTitle.setTextContent("");
		bugzillaSection.appendChild(bugzillaSectionTitle);

		// BUGZILLA LINK

		try
		{
			final String instanceNameProperty = System.getProperty(Constants.INSTANCE_NAME_PROPERTY);
			final String fixedInstanceNameProperty = instanceNameProperty == null ? "Not Defined" : instanceNameProperty;
			
			final Element bugzillaPara = topic.getTempTopicXMLDoc().createElement("para");
			bugzillaPara.setAttribute("role", ROLE_CREATE_BUG_PARA);

			final Element bugzillaULink = topic.getTempTopicXMLDoc().createElement("ulink");

			bugzillaULink.setTextContent("Report a bug");

			final SimpleDateFormat formatter = new SimpleDateFormat(Constants.FILTER_DISPLAY_DATE_FORMAT);
			final String whiteboard = URLEncoder.encode("Instance Name: " + fixedInstanceNameProperty + " Topic ID:" + topic.getTopicId() + " Skynet Build: " + Constants.BUILD + " Topic Title: " + topic.getTopicTitle() + " Topic Revision: " + topic.getLatestRevision() + " Topic Revision Date: " + formatter.format(topic.getLatestRevisionDate()) + " Topic Tags: "
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
			SkynetExceptionUtilities.handleException(ex, false, "An error generating the bugzilla link");
		}

		// SURVEY LINK
		if (docbookBuildingOptions.getInsertSurveyLink())
		{
			final Element surveyPara = topic.getTempTopicXMLDoc().createElement("para");
			surveyPara.setAttribute("role", ROLE_CREATE_BUG_PARA);
			bugzillaSection.appendChild(surveyPara);

			final Text startSurveyText = topic.getTempTopicXMLDoc().createTextNode("Thank you for evaluating the new documentation format for JBoss Enterprise Application Platform. Let us know what you think by taking a short ");
			surveyPara.appendChild(startSurveyText);

			final Element surveyULink = topic.getTempTopicXMLDoc().createElement("ulink");
			surveyPara.appendChild(surveyULink);
			surveyULink.setTextContent("survey");
			surveyULink.setAttribute("url", "https://www.keysurvey.com/survey/380730/106f/");

			final Text endSurveyText = topic.getTempTopicXMLDoc().createTextNode(".");
			surveyPara.appendChild(endSurveyText);
		}

		// VIEW IN SKYNET

		final Element skynetElement = topic.getTempTopicXMLDoc().createElement("remark");
		skynetElement.setAttribute("role", ROLE_VIEW_IN_SKYNET_PARA);
		bugzillaSection.appendChild(skynetElement);

		final Element skynetLinkULink = topic.getTempTopicXMLDoc().createElement("ulink");
		skynetElement.appendChild(skynetLinkULink);
		skynetLinkULink.setTextContent("View in Skynet");
		skynetLinkULink.setAttribute("url", getTopicSkynetURL(topic));

		// SKYNET VERSION

		final Element buildVersionElement = topic.getTempTopicXMLDoc().createElement("remark");
		buildVersionElement.setAttribute("role", ROLE_BUILD_VERSION_PARA);
		bugzillaSection.appendChild(buildVersionElement);

		final Element skynetVersionElementULink = topic.getTempTopicXMLDoc().createElement("ulink");
		buildVersionElement.appendChild(skynetVersionElementULink);
		skynetVersionElementULink.setTextContent("Built with Skynet version " + Constants.BUILD);
		skynetVersionElementULink.setAttribute("url", searchTagsUrl);
	}

	/**
	 * Call the BRMS rule to calculate the priority of this topic
	 */
	private void processTopicCalculatePriority(final Topic topic, final WorkingMemory businessRulesWorkingMemory)
	{
		// generate the relative priority from the BRMS rule
		businessRulesWorkingMemory.setGlobal("topic", topic);
		businessRulesWorkingMemory.insert(new DroolsEvent("CalculateRelativePriority"));
		businessRulesWorkingMemory.fireAllRules();
	}

	private void processTopicBreadCrumbs(final Topic topic)
	{
		assert topic != null : "The topic parameter can not be null";
		assert topic.getTempTopicXMLDoc() != null : "topic.getTempTopicXMLDoc() can not be null";

		final Document xmlDocument = topic.getTempTopicXMLDoc();
		final Node docElement = xmlDocument.getDocumentElement();

		assert docElement != null : "xmlDocument.getDocumentElement() should not be null";

		final List<Node> titleNodes = XMLUtilities.getNodes(docElement, "title");
		final Node title = titleNodes.size() != 0 ? titleNodes.get(0) : null;

		Node insertBefore = null;

		if (title != null)
		{
			/*
			 * We expect the first child of the document element to be a title,
			 * and the bread crumb will be inserted after the title.
			 */
			if (title.getParentNode() == docElement)
			{
				insertBefore = title.getNextSibling();
			}
			/*
			 * If the first child is not a section title, we will insert the
			 * bread crumb before it
			 */
			else
			{
				insertBefore = docElement.getFirstChild();
			}
		}

		final Element breadCrumbPara = xmlDocument.createElement("para");

		/*
		 * If insertBefore == null, it means the document is empty, or the title
		 * has no sibling, so we just append to the document.
		 */
		if (insertBefore == null)
			docElement.appendChild(breadCrumbPara);
		else
			docElement.insertBefore(breadCrumbPara, insertBefore);

		final Element homeLink = xmlDocument.createElement("xref");
		homeLink.setAttribute("linkend", Constants.TOPIC_XREF_PREFIX + Constants.HOME_LANDING_PAGE_TOPIC_ID);
		breadCrumbPara.appendChild(homeLink);

		/*
		 * find the landing pages that match the combination of tags present in
		 * this topic
		 */
		final List<Tag> topicTags = topic.getTags();
		for (int i = 0; i < topicTags.size(); ++i)
		{
			final Tag tag1 = topicTags.get(i);
			final Integer tag1ID = tag1.getTagId();
			
			for (int j = i + 1; j < topicTags.size(); ++j)
			{
				final Tag tag2 = topicTags.get(j);
				final Integer tag2ID = tag2.getTagId();

				final List<Integer> matchingTags = CollectionUtilities.toArrayList(Constants.TAG_DESCRIPTION_TAG_ID, tag1ID, tag2ID);
				final List<Integer> excludeTags = new ArrayList<Integer>();
				final List<Topic> landingPage = topicDatabase.getMatchingTopicsFromInteger(matchingTags, excludeTags, true, true);

				if (landingPage.size() == 1)
				{
					final Text delimiter = xmlDocument.createTextNode(" : ");
					breadCrumbPara.appendChild(delimiter);

					final Element landingPageXRef = xmlDocument.createElement("xref");
					landingPageXRef.setAttribute("linkend", landingPage.get(0).getXRefID());
					breadCrumbPara.appendChild(landingPageXRef);
				}

			}
		}
	}

	/**
	 * Fix image location references so publican will compile them
	 */
	private void processTopicFixImages(final Topic topic)
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
	private boolean validateTopicTags(final Topic xmlData, final Topic topic, final String searchTagsUrl, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final DocbookBuildingOptions docbookBuildingOptions)
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
				populateIdXMLDataFromDB(errorTagsTopic, xmlData, searchTagsUrl, docbookBuildingOptions);

				String error = "Topic " + topic.getTopicId() + " has tags";
				for (final Tag foundTag : foundTypeTags)
					error += " " + foundTag.getTagName() + " [" + foundTag.getTagId() + "]";
				error += " assigned to it from a mutually exclusive catgeory group";

				errorDatabase.addError(topic, error);

				return false;
			}
			else if (foundTypeTags.size() == 0)
			{
				populateIdXMLDataFromDB(errorTagsTopic, xmlData, searchTagsUrl, docbookBuildingOptions);

				errorDatabase.addError(topic, "Topic is missing tags in an exclusive mandatory category");

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
				populateIdXMLDataFromDB(errorTagsTopic, xmlData, searchTagsUrl, docbookBuildingOptions);

				errorDatabase.addError(topic, "Topic is missing tags in a mandatory category");

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
	private void processTopic(final Topic topic, final ArrayList<List<TagToCategory>> mandatoryExclusiveTagCollections, final ArrayList<List<TagToCategory>> mandatoryTagCollections, final int roleCategoryID, final String searchTagsUrl,
			final List<TagToCategory> tagToCategories, final List<String> usedIds, final DocbookBuildingOptions docbookBuildingOptions)
	{
		/* we have already processed this topic, don't do it again */
		if (topicDatabase.containsTopic(topic))
			return;

		System.out.println("Processing Topic " + topic.getTopicTitle());

		/*
		 * Convert the XML text into an XML Document. It is this Document that
		 * the remainder of this function will modify
		 */
		topic.initializeTempTopicXMLDoc();

		/* the role is defined as the topic type and topic lifecycle */
		final String role = AttributeBuilder.GENERIC_NAV_LINK_ROLE + " " + getTopicLifecycleRole(topic, roleCategoryID, tagToCategories);
		topic.setTempNavLinkDocbook(DocbookUtils.buildXRefListItem(topic.getXRefID(), role));

		topicDatabase.addTopic(topic);

		/********** VALIDATE THE TAGS **********/

		if (!validateTopicTags(topic, topic, searchTagsUrl, mandatoryExclusiveTagCollections, mandatoryTagCollections, docbookBuildingOptions))
			return;

		/********** SET TOPIC ROLE **********/

		// define the role, which uses the topic lifecycle tag
		topic.setTempTopicRole(getTopicLifecycleRole(topic, roleCategoryID, tagToCategories));

		/********** ADDITIONAL VALIDATION **********/

		/*
		 * make sure that we have valid XML. this won't check the validity of
		 * the docbook, but will ensure that we are working with valid xml.
		 */
		if (!validateTopicXML(topic, searchTagsUrl, roleCategoryID, tagToCategories, docbookBuildingOptions))
			return;

		/*
		 * make sure this topic is not trying to use an id attribute that has
		 * been used previously
		 */
		if (!validateIdAttributesUnique(topic, searchTagsUrl, usedIds, docbookBuildingOptions))
			return;

		/********** TOPIC IS VALID **********/

		/*
		 * At this point we have a valid topic, so we can apply the various
		 * additions and fixes to it
		 */

		processTopicID(topic);
		//processTopicCalculatePriority(topic, businessRulesWorkingMemory);
		processTopicLifecycleRole(topic, roleCategoryID, tagToCategories);
		processTopicDraftWarning(topic);
		processTopicAdditionalInfo(topic, searchTagsUrl, docbookBuildingOptions);
		processTopicFixImages(topic);

		/********** PROCESS RELATED TOPICS **********/

		/*
		 * don't process related topics when creating a narrative, or when
		 * specifically instructed not to
		 */
		if (docbookBuildingOptions.getProcessRelatedTopics() && !docbookBuildingOptions.getBuildNarrative())
		{
			for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
			{
				processTopic(topicToTopic.getRelatedTopic(), mandatoryExclusiveTagCollections, mandatoryTagCollections, roleCategoryID, searchTagsUrl, tagToCategories, usedIds, docbookBuildingOptions);
			}
		}

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
	private List<TagToCategory> getTagToCatgeories(final Integer categoryID, final List<TagToCategory> tagToCategories)
	{
		final List<TagToCategory> retValue = filter(having(on(TagToCategory.class).getCategory().getCategoryId(), equalTo(categoryID)), tagToCategories);
		Collections.sort(retValue, new TagToCategorySortingComparator());
		return retValue;
	}

	private void reloadSystemPreferences(final WorkingMemory businessRulesWorkingMemory)
	{
		// generate the relative priority from the BRMS rule
		businessRulesWorkingMemory.insert(new DroolsEvent("SetSystemPreferences"));
		businessRulesWorkingMemory.fireAllRules();
	}

	private String getTopicSkynetURL(final Topic topic)
	{
		return Constants.SERVER_URL + "/TopicIndex/CustomSearchTopicList.seam?topicIds=" + topic.getTopicId();
	}
}