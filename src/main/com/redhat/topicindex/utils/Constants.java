package com.redhat.topicindex.utils;

public class Constants
{
	/**
	 * The Skynet build number, displayed on the top bar of all Skynet pages. Is
	 * in the format yyyymmdd-hhmm
	 */
	public static final String BUILD = "20111108-0820";
	
	/**
	 * The system property that determines if topics should be rendered into
	 * HTML
	 */
	public static final String ENABLE_RENDERING_PROPERTY = "topicindex.rerenderTopic";
	
	/**
	 * The system property that identifies this skynet instance
	 */
	public static final String INSTANCE_NAME_PROPERTY = "topicindex.instanceName";
	
	/** The encoding of the XML, used when converting a DOM object to a string */
	public static final String XML_ENCODING = "UTF-8";
	
	public static final String XSL_ERROR_TEMPLATE = "<html><head><title>ERROR</title></head><body>The topic could not be transformed into HTML</body></html>";
	
	public static final String TOPIC_XREF_PREFIX = "TopicID";
	public static final int HOME_LANDING_PAGE_TOPIC_ID = -1;
	
	public static final String BASE_REST_PATH = "/seam/resource/rest";
	


	/** The format of the date to be displayed by any date widget */
	public static final String FILTER_DISPLAY_DATE_FORMAT = "dd MMM yyyy HH:mm";
	/** The ISO8601 date format, used for SQL queries */
	public static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	/**
	 * The URL of the main web Skynet instance. This is used when generating
	 * links in the documentation.
	 */
	public static final String SERVER_URL = "http://jboss-eap.bne.redhat.com:8080";

	/**
	 * The "Common" project includes any tags that are not assigned to any other
	 * project. The Common project has not setup in the database anywhere, but
	 * when processing the data structures that contain the list of projects we
	 * need a name, which is defined in this constant.
	 */
	public static final String COMMON_PROJECT_NAME = "Common";
	/** This is the ID for the Common project */
	public static final Integer COMMON_PROJECT_ID = -1;
	/** This is the description for the Common project */
	public static final String COMMON_PROJECT_DESCRIPTION = "This project holds tags that are not assigned to any other project";
	/**
	 * This is the host name of the live SQL server. This value is used to
	 * provide a label in the top bar when modifying live data.
	 */
	public static final String LIVE_SQL_SERVER = "jboss-eap.bne.redhat.com";
	/** The SQL logic keyword to use when two conditions need to be and'ed */
	public static final String AND_LOGIC = "And";
	/** The SQL logic keyword to use when two conditions need to be or'ed */
	public static final String OR_LOGIC = "Or";
	/** The default logic to be applied to tags within a category */
	public static final String DEFAULT_INTERNAL_LOGIC = OR_LOGIC;
	/** The default logic to be applied between categories */
	public static final String DEFAULT_EXTERNAL_LOGIC = AND_LOGIC;
	/** The url variable prefix to indicate that a tag needs to be matched */
	public static final String MATCH_TAG = "tag";
	/** The URL variable prefix to indicate that a tag needs to be excluded */
	public static final String NOT_MATCH_TAG = "nottag";
	/**
	 * The URL variable prefix to indicate the internal logic of a category (and
	 * optionally also specify a project)
	 */
	public static final String CATEORY_INTERNAL_LOGIC = "catint";
	/**
	 * The URL variable prefix to indicate the external logic of a category (and
	 * optionally also specify a project)
	 */
	public static final String CATEORY_EXTERNAL_LOGIC = "catext";
	/** The URL variable the indicates the filter to be used */
	public static final String FILTER_ID = "filterId";
	/**
	 * The value (as used in the FilterTag database TagState field) the
	 * indicates that a tag should be matched
	 */
	public static final int MATCH_TAG_STATE = 1;
	/**
	 * The value (as used in the FilterTag database TagState field) the
	 * indicates that a tag should be excluded
	 */
	public static final int NOT_MATCH_TAG_STATE = 0;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an internal "and" state
	 */
	public static final int CATEGORY_INTERNAL_AND_STATE = 0;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an internal "or" state
	 */
	public static final int CATEGORY_INTERNAL_OR_STATE = 1;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an external "and" state
	 */
	public static final int CATEGORY_EXTERNAL_AND_STATE = 2;
	/**
	 * The value (as used in the FilterCategory database CategoryState field)
	 * the indicates that a category has an external "or" state
	 */
	public static final int CATEGORY_EXTERNAL_OR_STATE = 3;
	/** The default internal category logic state */
	public static final int CATEGORY_INTERNAL_DEFAULT_STATE = CATEGORY_INTERNAL_OR_STATE;
	/** The default external category logic state */
	public static final int CATEGORY_EXTERNAL_DEFAULT_STATE = CATEGORY_EXTERNAL_AND_STATE;

	/**
	 * The file name for the RocBook DTD schema. This is used when matching and
	 * providing XML resources
	 */
	public static final String ROCBOOK_DTD = "rocbook.dtd";
	/**
	 * The file name for the DocBook DTD schema. This is used when matching and
	 * providing XML resources
	 */
	public static final String DOCBOOK_DTD = "docbook.dtd";
	/** The URL variable that defines the topic text search field */
	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR = "topicTextSearch";
	/** The description of the topic text search field */
	public static final String TOPIC_TEXT_SEARCH_FILTER_VAR_DESC = "Topic Text Search";
	/** The URL variable that defines the topic IDs search field */
	public static final String TOPIC_IDS_FILTER_VAR = "topicIds";
	/** The description of the topic IDs search field */
	public static final String TOPIC_IDS_FILTER_VAR_DESC = "Topic IDs";
	/** The URL variable that defines the topic added by search field */
	public static final String TOPIC_ADDED_BY_FILTER_VAR = "topicAddedBy";
	/** The description of the topic added by search field */
	public static final String TOPIC_ADDED_BY_FILTER_VAR_DESC = "Added By";
	/** The URL variable that defines the topic title search field */
	public static final String TOPIC_TITLE_FILTER_VAR = "topicTitle";
	/** The description of the topic title search field */
	public static final String TOPIC_TITLE_FILTER_VAR_DESC = "Title";
	/** The URL variable that defines the topic description search field */
	public static final String TOPIC_DESCRIPTION_FILTER_VAR = "topicText";
	/** The description of the topic description search field */
	public static final String TOPIC_DESCRIPTION_FILTER_VAR_DESC = "Text";
	/** The URL variable that defines the topic xml search field */
	public static final String TOPIC_XML_FILTER_VAR = "topicXml";
	/** The description of the topic xml search field */
	public static final String TOPIC_XML_FILTER_VAR_DESC = "XML";
	/** The URL variable that defines the start range for the topic create date search field */
	public static final String TOPIC_STARTDATE_FILTER_VAR = "startDate";
	/** The description of the start range for the topic create date search field */
	public static final String TOPIC_STARTDATE_FILTER_VAR_DESC = "Min Creation Date";
	/** The URL variable that defines the end range for the topic create date search field */
	public static final String TOPIC_ENDDATE_FILTER_VAR = "endDate";
	/** The description of the end range for the topic create date search field */
	public static final String TOPIC_ENDDATE_FILTER_VAR_DESC = "Max Creation Date";
	/** The URL variable that defines the logic to be applied to the search fields */
	public static final String TOPIC_LOGIC_FILTER_VAR = "logic";
	/** The description the logic to be applied to the search fields */
	public static final String TOPIC_LOGIC_FILTER_VAR_DESC = "Field Logic";
	/** The URL variable that defines the has relationships search field */
	public static final String TOPIC_HAS_RELATIONSHIPS = "topicHasRelationships";
	/** The description of the has relationships search field */
	public static final String TOPIC_HAS_RELATIONSHIPS_DESC = "Has Relationships";
	/** The URL variable that defines the has incoming relationships search field */
	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS = "topicHasIncomingRelationships";
	/** The description of the has incoming relationships search field */
	public static final String TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC = "Has Incoming Relationships";
	/** The URL variable that defines the has related to search field */
	public static final String TOPIC_RELATED_TO = "topicRelatedTo";
	/** The description of the has related to search field */
	public static final String TOPIC_RELATED_TO_DESC = "Related To";
	/** The URL variable that defines the has related from search field */
	public static final String TOPIC_RELATED_FROM = "topicRelatedFrom";
	/** The description of the has related from search field */
	public static final String TOPIC_RELATED_FROM_DESC = "Related From";
	/** The default logic to be applied to the search fields */
	public static final String TOPIC_LOGIC_FILTER_VAR_DEFAULT_VALUE = "and";

	/*
	 * TODO: These tag and category ids should probably come from a
	 * configuration file instead of being hard coded. Any changes to the tags
	 * will break the docbook compilation, and require this source code to be
	 * modified to reflect the new tag ids.
	 * 
	 * Generally speaking, tags referenced here should eventually become fields
	 * on a topic.
	 */
	public static final Integer TYPE_CATEGORY_ID = 4;
	public static final Integer TECHNOLOGY_CATEGORY_ID = 3;
	public static final Integer RELEASE_CATEGORY_ID = 15;
	public static final Integer WRITER_CATEGORY_ID = 12;
	public static final Integer COMMON_NAME_CATEGORY_ID = 17;
	public static final String TECHNOLOGY_CATEGORY_NAME = "Technologies";
	public static final Integer CONCERN_CATEGORY_ID = 2;
	public static final String CONCERN_CATEGORY_NAME = "Concerns";
	public static final Integer LIFECYCLE_CATEGORY_ID = 5;

	/** The Concept tag ID */
	public static final Integer CONCEPT_TAG_ID = 5;
	public static final String CONCEPT_TAG_NAME = "Concept";
	/** The Conceptual Overview tag ID */
	public static final Integer CONCEPTUALOVERVIEW_TAG_ID = 93;
	public static final String CONCEPTUALOVERVIEW_TAG_NAME = "Overview";
	/** The Reference tag ID */
	public static final Integer REFERENCE_TAG_ID = 6;
	public static final String REFERENCE_TAG_NAME = "Reference";
	/** The Task tag ID */
	public static final Integer TASK_TAG_ID = 4;
	public static final String TASK_TAG_NAME = "Task";
	/** The Written tag ID */
	public static final Integer WRITTEN_TAG_ID = 19;
	/** The Tag Description tag ID */
	public static final Integer TAG_DESCRIPTION_TAG_ID = 215;
	/** The Home tag ID */
	public static final Integer HOME_TAG_ID = 216;


	/**
	 * This identifies the the tag that is assigned to a topics when it is in
	 * its final state
	 */
	public static final Integer TOPIC_FINAL_LIFECYCLE = 19;

	/**
	 * The ID for the inherit relationship type, as defined in the
	 * RoleToRoleRelationship table
	 */
	public static final Integer INHERIT_ROLE_RELATIONSHIP_TYPE = 1;
}
