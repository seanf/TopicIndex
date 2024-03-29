package com.redhat.topicindex.entity;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsArray.array;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import net.htmlparser.jericho.Source;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.ecs.commonutils.DocBookUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.sort.TagIDComparator;
import com.redhat.topicindex.sort.TagNameComparator;
import com.redhat.topicindex.sort.TagToCategorySortingComparator;
import com.redhat.topicindex.sort.TopicIDComparator;
import com.redhat.topicindex.sort.TopicToTagTagIDSort;
import com.redhat.topicindex.sort.TopicToTopicTopicIDSort;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityQueries;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.XMLValidator;
import com.redhat.topicindex.utils.docbookbuilding.XMLPreProcessor;
import com.redhat.topicindex.utils.structures.NameIDSortMap;
import com.redhat.topicindex.utils.topicrenderer.TopicRenderer;
import com.redhat.topicindex.utils.topicrenderer.XMLRenderer;

@Entity
@Audited
@Indexed
@Table(name = "Topic")
public class Topic implements java.io.Serializable, Comparable<Topic>
{
	public static final String SELECT_ALL_QUERY = "select topic from com.redhat.topicindex.entity.Topic as Topic";
	/**
	 * The string that identifies the automatically generated comment added to
	 * the end of the XML
	 */
	private static final String DETAILS_COMMENT_NODE_START = " Generated Topic Details";
	/** The string constant that is used as a conceptual overview template */
	private static final Integer CONCEPTUAL_OVERVIEW_TOPIC_STRINGCONSTANTID = 11;
	/** The string constant that is used as a reference template */
	private static final Integer REFERENCE_TOPIC_STRINGCONSTANTID = 12;
	/** The string constant that is used as a task template */
	private static final Integer TASK_TOPIC_STRINGCONSTANTID = 13;
	/** The string constant that is used as a concept template */
	private static final Integer CONCEPT_TOPIC_STRINGCONSTANTID = 14;

	private static final long serialVersionUID = 5580473587657911655L;

	/**
	 * If this value is false, when this entity is saved it will not trigger a
	 * rerender of the any related topics
	 */
	private boolean rerenderRelatedTopics = true;

	@Transient
	public static String getCSVHeaderRow()
	{
		String topicColumns = EntityUtilities.buildString(new String[]
		{ "Topic ID", "Topic Title", "Topic Text", "Topic URL" }, ",");

		final List<String> categroies = EntityQueries.getAllPropertiesFromEntity("Category", "categoryName");

		for (final String category : categroies)
			topicColumns += "," + category;

		return topicColumns;
	}

	private String topicAddedBy;
	private Integer topicId;
	private String topicText;
	private Date topicTimeStamp;
	private String topicTitle;
	private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
	private Set<TopicToTopic> parentTopicToTopics = new HashSet<TopicToTopic>(0);
	private Set<TopicToTopic> childTopicToTopics = new HashSet<TopicToTopic>(0);
	private Set<TopicToTopicSourceUrl> topicToTopicSourceUrls = new HashSet<TopicToTopicSourceUrl>(0);
	private String topicXML;
	private TopicSecondOrderData topicSecondOrderData;

	/*
	 * While processing a topic for inclusion in the final output we need some
	 * additional temporary variables. These variables are all prefixed with
	 * 'temp'.
	 */

	/** the role (typically the highest priority lifecycle tag) is saved here */
	private String tempTopicRole;
	/** The parsed XML DOM */
	private Document tempTopicXMLDoc;
	/** the relative priority of this topic */
	private Integer tempRelativePriority;
	/** true if this topic has failed validation */
	private boolean tempInvalidTopic = false;
	/**
	 * The Docbook ListItem that holds an XRef to this topic. This link is used
	 * when building the TOC
	 */
	private String tempNavLinkDocbook;

	public Topic()
	{
	}

	public Topic(final Date topicTimeStamp, final String topicProduct)
	{
		this.topicTimeStamp = topicTimeStamp;
	}

	public Topic(final String topicTitle, final String topicText, final String topicAddedBy, final Date topicTimeStamp, final Set<TopicToTag> topicToTags)
	{
		this.topicTitle = topicTitle;
		this.topicText = topicText;
		this.topicAddedBy = topicAddedBy;
		this.topicTimeStamp = topicTimeStamp;
		this.topicToTags = topicToTags;
	}

	public void addTag(final Tag tag)
	{
		if (filter(having(on(TopicToTag.class).getTag(), equalTo(tag)), this.getTopicToTags()).size() == 0)
		{			
			this.topicToTags.add(new TopicToTag(this, tag));
		}
	}
		
	public void addTag(final EntityManager entityManager, final int tagID)
	{
		final Tag tag = entityManager.getReference(Tag.class, tagID);
		addTag(tag);
	}
	
	public void addTag(final int tagID)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.getReference(Tag.class, tagID);
		addTag(tag);
	}
	
	public void removeTag(final Tag tag)
	{
		removeTag(tag.getTagId());
	}
	
	public void removeTag(final int tagID)
	{
		final List<TopicToTag> topicToTags = filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(tagID)), this.getTopicToTags());
		if (topicToTags.size() != 0)
		{
			this.topicToTags.remove(topicToTags.get(0));
		}
	}

	public int compareTo(Topic o)
	{
		if (o == null)
			return 1;

		if (o.getTopicId() == null && this.getTopicId() == null)
			return 0;

		if (o.getTopicId() == null)
			return 1;

		if (this.getTopicId() == null)
			return -1;

		return this.getTopicId().compareTo(o.getTopicId());
	}

	@SuppressWarnings("unchecked")
	@Transient
	public String getCSVRow()
	{
		// get a space separated list of source URLs
		String sourceUrls = "";
		for (final TopicToTopicSourceUrl url : this.getTopicToTopicSourceUrls())
		{
			if (sourceUrls.length() != 0)
				sourceUrls += " ";
			sourceUrls += url.getTopicSourceUrl().getSourceUrl();
		}

		String topicColumns = EntityUtilities.cleanTextForCSV(this.topicId.toString()) + "," + EntityUtilities.cleanTextForCSV(this.topicTitle) + "," + EntityUtilities.cleanTextForCSV(this.topicText) + "," + EntityUtilities.cleanTextForCSV(sourceUrls);

		final List<Category> categroies = EntityQueries.getAllCategories();

		for (final Category category : categroies)
		{
			final List<TopicToTag> matchingTags = filter(having(on(TopicToTag.class).getTag().getTagToCategories().toArray(), array(hasProperty("category", equalTo(category)))), this.getTopicToTags());

			String tags = "";
			for (final TopicToTag topicToTag : matchingTags)
			{
				final Tag tag = topicToTag.getTag();
				if (tags.length() != 0)
					tags += " ";
				tags += "[" + tag.getTagId() + ": " + EntityUtilities.cleanTextForCSV(tag.getTagName()) + "]";
			}

			topicColumns += "," + tags;
		}

		return topicColumns;
	}

	@Transient
	public List<Topic> getOneWayRelatedTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			if (!topicToTopic.getRelatedTopic().isRelatedTo(this))
				retValue.add(topicToTopic.getRelatedTopic());
		}

		Collections.sort(retValue, new TopicIDComparator());

		return retValue;
	}

	@Transient
	public List<Topic> getTwoWayRelatedTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().isRelatedTo(this))
				retValue.add(topicToTopic.getRelatedTopic());

		Collections.sort(retValue, new TopicIDComparator());

		return retValue;
	}

	@Transient
	public List<Topic> getIncomingRelatedTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getChildTopicToTopics())
			if (!this.isRelatedTo(topicToTopic.getMainTopic()))
				retValue.add(topicToTopic.getMainTopic());

		Collections.sort(retValue, new TopicIDComparator());

		return retValue;
	}

	@Transient
	public List<Topic> getAllRelatedTopicsArray()
	{
		final List<Topic> retValue = new ArrayList<Topic>();
		retValue.addAll(getOneWayRelatedTopicsArray());
		retValue.addAll(getTwoWayRelatedTopicsArray());
		retValue.addAll(getIncomingRelatedTopicsArray());
		return retValue;
	}

	@Transient
	public List<Topic> getOutgoingTopicsArray()
	{
		final ArrayList<Topic> retValue = new ArrayList<Topic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			retValue.add(topicToTopic.getRelatedTopic());
		}
		return retValue;
	}

	@Transient
	public List<Integer> getRelatedTopicIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			retValue.add(topicToTopic.getRelatedTopic().getTopicId());
		return retValue;
	}

	@Transient
	public List<Integer> getIncomingRelatedTopicIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTopic topicToTopic : this.getChildTopicToTopics())
			retValue.add(topicToTopic.getMainTopic().getTopicId());
		return retValue;
	}

	@Transient
	public String getRelatedTopicsList()
	{
		String topicList = "";
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			final Topic relatedTopic = topicToTopic.getRelatedTopic();

			if (topicList.length() != 0)
				topicList += ", ";

			topicList += "<span title=\"" + relatedTopic.getTopicTitle() + " \n" + relatedTopic.getCommaSeparatedTagList() + "\">" + topicToTopic.getRelatedTopic().getTopicId() + "</span>";
		}
		return topicList;
	}

	/**
	 * @return Returns the list of revision numbers for this entity, as
	 *         maintained by Envers
	 */
	@Transient
	public List<Number> getRevisions()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final List<Number> retValue = reader.getRevisions(Topic.class, this.topicId);
		Collections.sort(retValue, Collections.reverseOrder());
		return retValue;
	}

	/**
	 * @return Returns the latest Envers revision number
	 */
	@Transient
	public Number getLatestRevision()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final List<Number> retValue = reader.getRevisions(Topic.class, this.topicId);
		Collections.sort(retValue, Collections.reverseOrder());
		return retValue.size() != 0 ? retValue.get(0) : -1;
	}

	/**
	 * @return Returns the latest Envers revision number
	 */
	@Transient
	public Date getLatestRevisionDate()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		return reader.getRevisionDate(getLatestRevision());
	}

	/**
	 * This is necessary because a4j:repeat does not work with a Set
	 */
	@Transient
	public ArrayList<Tag> getTagsArray()
	{
		final ArrayList<Tag> retValue = new ArrayList<Tag>();
		for (final TopicToTag topicToTag : this.topicToTags)
			retValue.add(topicToTag.getTag());
		return retValue;
	}

	@Transient
	private TreeMap<NameIDSortMap, ArrayList<Tag>> getCategoriesMappedToTags()
	{
		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = new TreeMap<NameIDSortMap, ArrayList<Tag>>();

		final Set<TopicToTag> topicToTags = this.topicToTags;
		for (final TopicToTag topicToTag : topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			final Set<TagToCategory> tagToCategories = tag.getTagToCategories();

			if (tagToCategories.size() == 0)
			{
				final NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);

				if (!tags.containsKey(categoryDetails))
					tags.put(categoryDetails, new ArrayList<Tag>());

				tags.get(categoryDetails).add(tag);
			}
			else
			{
				for (final TagToCategory category : tagToCategories)
				{
					final NameIDSortMap categoryDetails = new NameIDSortMap(category.getCategory().getCategoryName(), category.getCategory().getCategoryId(), category.getCategory().getCategorySort() == null ? 0 : category.getCategory().getCategorySort());

					if (!tags.containsKey(categoryDetails))
						tags.put(categoryDetails, new ArrayList<Tag>());

					tags.get(categoryDetails).add(tag);
				}
			}
		}

		return tags;
	}

	@Transient
	public String getTagsList()
	{
		return getTagsList(true);
	}

	/**
	 * Generates a HTML formatted and categorized list of the tags that are
	 * associated with this topic
	 * 
	 * @return A HTML String to display in a table
	 */
	@Transient
	public String getTagsList(final boolean brLineBreak)
	{
		// define the line breaks for html and for tooltips
		final String lineBreak = brLineBreak ? "<br/>" : " \n";
		final String boldStart = brLineBreak ? "<b>" : "";
		final String boldEnd = brLineBreak ? "</b>" : "";

		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags();

		String tagsList = "";
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key), new TagNameComparator());

			if (tagsList.length() != 0)
				tagsList += lineBreak;

			tagsList += boldStart + key.getName() + boldEnd + ": ";

			String thisTagList = "";

			for (final Tag tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";

				thisTagList += "<span title=\"Tag ID: " + tag.getTagId() + " &#13;Tag Description: " + tag.getTagDescription() + "\">" + tag.getTagName() + "</span>";
			}

			tagsList += thisTagList + " ";
		}

		return tagsList;
	}

	@Transient
	public String getCommaSeparatedTagList()
	{
		final TreeMap<NameIDSortMap, ArrayList<Tag>> tags = getCategoriesMappedToTags();

		String tagsList = "";
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key), new TagNameComparator());

			if (tagsList.length() != 0)
				tagsList += " ";

			tagsList += key.getName() + ": ";

			String thisTagList = "";

			for (final Tag tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";

				thisTagList += tag.getTagName();
			}

			tagsList += thisTagList + " ";
		}

		return tagsList;
	}

	@Column(name = "TopicAddedBy", length = 512)
	@Length(max = 512)
	public String getTopicAddedBy()
	{
		return this.topicAddedBy;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicID", unique = true, nullable = false)
	public Integer getTopicId()
	{
		return this.topicId;
	}

	// @Column(name = "TopicText", length = 65535)
	@Column(name = "TopicText", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicText()
	{
		return this.topicText;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TopicTimeStamp", nullable = false, length = 0)
	@NotNull
	public Date getTopicTimeStamp()
	{
		return this.topicTimeStamp;
	}

	@Column(name = "TopicTitle", length = 512)
	@Length(max = 512)
	public String getTopicTitle()
	{
		return this.topicTitle;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTag> getTopicToTags()
	{
		return this.topicToTags;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTopicSourceUrl> getTopicToTopicSourceUrls()
	{
		return this.topicToTopicSourceUrls;
	}

	// @Column(name = "TopicXML", length = 65535)
	@Column(name = "TopicXML", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicXML()
	{
		return this.topicXML;
	}

	@Override
	public int hashCode()
	{
		final int result = (topicId != null ? topicId.hashCode() : 0);
		return result;
	}

	@Transient
	public boolean isRelatedTo(final Integer relatedTopicId)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().topicId.equals(relatedTopicId))
				return true;

		return false;
	}

	@Transient
	public boolean isRelatedTo(final Topic relatedTopic)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().equals(relatedTopic))
				return true;

		return false;
	}

	@Transient
	public boolean isTaggedWith(final Integer tagId)
	{
		for (final TopicToTag topicToTag : this.getTopicToTags())
			if (topicToTag.getTag().getTagId().equals(tagId))
				return true;

		return false;
	}

	@Transient
	public boolean isTaggedWith(final Tag tag)
	{
		for (final TopicToTag topicToTag : this.getTopicToTags())
			if (topicToTag.getTag().equals(tag))
				return true;

		return false;
	}

	public boolean removeRelationshipTo(final Integer relatedTopicId)
	{
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
		{
			final Topic relatedTopic = topicToTopic.getRelatedTopic();

			if (relatedTopic.getTopicId().equals(relatedTopicId))
			{
				/* remove the relationship from this topic */
				this.getParentTopicToTopics().remove(topicToTopic);

				/* now remove the relationship from the other topic */
				for (final TopicToTopic childTopicToTopic : relatedTopic.getChildTopicToTopics())
				{
					if (childTopicToTopic.getMainTopic().equals(this))
					{
						relatedTopic.getChildTopicToTopics().remove(childTopicToTopic);
						break;
					}
				}

				return true;
			}
		}

		return false;
	}

	public boolean removeRelationshipTo(final EntityManager entityManager, final Integer topicId)
	{
		final Topic topic = entityManager.getReference(Topic.class, topicId);
		return removeRelationshipTo(topic);
	}
	
	public boolean removeRelationshipTo(final Topic relatedTopic)
	{
		return removeRelationshipTo(relatedTopic.getTopicId());
	}
	
	public boolean addRelationshipTo(final EntityManager entityManager, final Integer topicId)
	{
		final Topic topic = entityManager.getReference(Topic.class, topicId);
		return addRelationshipTo(topic);
	}

	public boolean addRelationshipTo(final Topic relatedTopic)
	{
		if (!this.isRelatedTo(relatedTopic))
		{
			final TopicToTopic topicToTopic = new TopicToTopic(this, relatedTopic);
			this.getParentTopicToTopics().add(topicToTopic);
			relatedTopic.getChildTopicToTopics().add(topicToTopic);
			return true;
		}

		return false;
	}

	@PrePersist
	private void onPrePresist()
	{
		this.topicTimeStamp = new Date();
		validate();
	}

	@PreUpdate
	private void onPreUpdate()
	{
		validate();
	}

	@PostUpdate
	private void onPostUpdate()
	{
		renderTopics();
	}

	@PostPersist
	private void onPostPersist()
	{
		renderTopics();
	}

	public void setTopicAddedBy(final String topicAddedBy)
	{
		this.topicAddedBy = topicAddedBy;
	}

	public void setTopicId(final Integer topicId)
	{
		this.topicId = topicId;
	}

	public void setTopicText(final String topicText)
	{
		this.topicText = topicText;
	}

	public void setTopicTimeStamp(final Date topicTimeStamp)
	{
		this.topicTimeStamp = topicTimeStamp;
	}

	public void setTopicTitle(final String topicTitle)
	{
		this.topicTitle = topicTitle;
	}

	public void setTopicToTags(final Set<TopicToTag> topicToTags)
	{
		this.topicToTags = topicToTags;
	}

	public void setTopicToTopicSourceUrls(final Set<TopicToTopicSourceUrl> topicToTopicSourceUrls)
	{
		this.topicToTopicSourceUrls = topicToTopicSourceUrls;
	}

	public void setTopicXML(final String topicXML)
	{
		this.topicXML = topicXML;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "mainTopic", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TopicToTopic> getParentTopicToTopics()
	{
		return parentTopicToTopics;
	}

	public void setParentTopicToTopics(final Set<TopicToTopic> parentTopicToTopics)
	{
		this.parentTopicToTopics = parentTopicToTopics;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "relatedTopic")
	public Set<TopicToTopic> getChildTopicToTopics()
	{
		return childTopicToTopics;
	}

	public void setChildTopicToTopics(final Set<TopicToTopic> childTopicToTopics)
	{
		this.childTopicToTopics = childTopicToTopics;
	}

	/**
	 * Syncs the XML with the topic details, such as setting the topic title as
	 * the title of the XML.
	 */
	public void syncXML()
	{
		/* remove line breaks from the title */
		this.topicTitle = this.topicTitle.replaceAll("\n", " ").trim();
		syncTopicTitleWithXML();

	}

	public void validate()
	{
		syncXML();
		validateTags();
		validateRelationships();
	}

	private void renderTopics()
	{
		if (!isRerenderRelatedTopics())
			return;

		try
		{
			final InitialContext initCtx = new InitialContext();
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			final Transaction transaction = transactionManager.getTransaction();

			WorkQueue.getInstance().execute(TopicRenderer.createNewInstance(this.getTopicId(), transaction));

			/*
			 * Because of the ability to inject topic contents into other
			 * topics, we need rerender all topics that have an incoming
			 * relationship.
			 */
			for (final Topic relatedTopic : this.getIncomingRelatedTopicsArray())
				WorkQueue.getInstance().execute(TopicRenderer.createNewInstance(relatedTopic.getTopicId(), transaction));

			for (final Topic relatedTopic : this.getTwoWayRelatedTopicsArray())
				WorkQueue.getInstance().execute(TopicRenderer.createNewInstance(relatedTopic.getTopicId(), transaction));
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a thread problem");
		}
	}

	public void renderXML(final EntityManager entityManager)
	{
		this.setTopicRendered(TopicRenderer.renderXML(entityManager, this));
		this.setTopicXMLErrors(TopicRenderer.validateXML(this));
	}

	private void validateRelationships()
	{
		final ArrayList<TopicToTopic> removeList = new ArrayList<TopicToTopic>();
		for (final TopicToTopic topicToTopic : this.getParentTopicToTopics())
			if (topicToTopic.getRelatedTopic().getTopicId().equals(this.getTopicId()))
				removeList.add(topicToTopic);

		for (final TopicToTopic topicToTopic : removeList)
			this.getParentTopicToTopics().remove(topicToTopic);
	}

	private void validateTags()
	{
		/*
		 * validate the tags that are applied to this topic. generally the gui
		 * should enforce these rules, with the exception of the bulk tag apply
		 * function
		 */

		/*
		 * create a collection of Categories mapped to TagToCategories, sorted
		 * by the Category sorting order
		 */
		final TreeMap<Category, ArrayList<TagToCategory>> tagDB = new TreeMap<Category, ArrayList<TagToCategory>>(Collections.reverseOrder());

		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			for (final TagToCategory tagToCategory : tag.getTagToCategories())
			{
				final Category category = tagToCategory.getCategory();

				if (!tagDB.containsKey(category))
					tagDB.put(category, new ArrayList<TagToCategory>());

				tagDB.get(category).add(tagToCategory);
			}
		}

		// now remove conflicting tags
		for (final Category category : tagDB.keySet())
		{
			// sort by the tags position in the category
			Collections.sort(tagDB.get(category), new TagToCategorySortingComparator(false));

			// because of the way we have ordered the tagDB collections, and
			// the ArrayLists it contains, this process will remove those tags
			// that belong to lower priority categories, and lower priority
			// tags in those categories

			final ArrayList<TagToCategory> tagToCategories = tagDB.get(category);

			// remove tags in the same mutually exclusive categories
			if (category.isMutuallyExclusive() && tagToCategories.size() > 1)
			{
				while (tagToCategories.size() > 1)
				{
					final TagToCategory tagToCategory = tagToCategories.get(1);
					// get the lower priority tag
					final Tag removeTag = tagToCategory.getTag();
					// remove it from the tagDB collection
					tagToCategories.remove(tagToCategory);

					// and remove it from the tag collection
					final ArrayList<TopicToTag> removeTopicToTagList = new ArrayList<TopicToTag>();
					for (final TopicToTag topicToTag : this.topicToTags)
					{
						if (topicToTag.getTag().equals(removeTag))
							removeTopicToTagList.add(topicToTag);
					}

					for (final TopicToTag removeTopicToTag : removeTopicToTagList)
					{
						this.topicToTags.remove(removeTopicToTag);
					}
				}
			}

			// remove tags that are explicitly defined as mutually exclusive
			for (final TagToCategory tagToCategory : tagToCategories)
			{
				final Tag tag = tagToCategory.getTag();
				for (final Tag exclusionTag : tag.getExcludedTags())
				{
					if (filter(having(on(TopicToTag.class).getTag(), equalTo(tagToCategory.getTag())), this.topicToTags).size() != 0 && // make
																																		// sure
																																		// that
																																		// we
																																		// have
																																		// not
																																		// removed
																																		// this
																																		// tag
																																		// already
							filter(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)), this.topicToTags).size() != 0 && // make
																																	// sure
																																	// the
																																	// exclusion
																																	// tag
																																	// exists
							!exclusionTag.equals(tagToCategory.getTag())) // make
																			// sure
																			// we
																			// are
																			// not
																			// trying
																			// to
																			// remove
																			// ourselves
					{
						with(this.topicToTags).remove(having(on(TopicToTag.class).getTag(), equalTo(exclusionTag)));
					}
				}
			}
		}
	}

	public void initializeTempTopicXMLDoc()
	{
		this.tempTopicXMLDoc = new XMLValidator().validateTopicXML(this.topicXML, false);
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Tag tag = topicToTag.getTag();
			retValue.add(tag);
		}

		Collections.sort(retValue, new TagIDComparator());

		return retValue;
	}

	@Transient
	public List<Integer> getTagIDs()
	{
		final List<Integer> retValue = new ArrayList<Integer>();
		for (final TopicToTag topicToTag : this.topicToTags)
		{
			final Integer tagId = topicToTag.getTag().getTagId();
			retValue.add(tagId);
		}

		return retValue;
	}

	@Transient
	public String getXRefID()
	{
		return Constants.TOPIC_XREF_PREFIX + this.topicId;
	}

	@Transient
	public String getTempTopicRole()
	{
		return tempTopicRole;
	}

	public void setTempTopicRole(final String tempTopicRole)
	{
		this.tempTopicRole = tempTopicRole;
	}

	@Transient
	public Document getTempTopicXMLDoc()
	{
		return tempTopicXMLDoc;
	}

	public void setTempTopicXMLDoc(final Document tempTopicXMLDoc)
	{
		this.tempTopicXMLDoc = tempTopicXMLDoc;
	}

	/**
	 * Convert an in memory XML document to a string. Also remove any XML
	 * preamble, meaning the XML returned by this function can be easily
	 * appended to existing XML.
	 */
	@Transient
	public String getTempTopicXMLDocString()
	{
		if (tempTopicXMLDoc != null)
		{
			String retValue = XMLUtilities.convertDocumentToString(this.tempTopicXMLDoc, Constants.XML_ENCODING);

			retValue = "<!-- Topic ID: " + this.topicId + " -->\n" + retValue.replaceAll("<\\?xml.*?\\?>", "").trim();

			return retValue;
		}

		return "";
	}

	@Transient
	public Integer getTempRelativePriority()
	{
		return tempRelativePriority;
	}

	public void setTempRelativePriority(final Integer tempRelativePriority)
	{
		this.tempRelativePriority = tempRelativePriority;
	}

	@Transient
	public boolean isTempInvalidTopic()
	{
		return tempInvalidTopic;
	}

	public void setTempInvalidTopic(final boolean tempInvalidTopic)
	{
		this.tempInvalidTopic = tempInvalidTopic;
	}

	@Transient
	public String getTempNavLinkDocbook()
	{
		return tempNavLinkDocbook;
	}

	public void setTempNavLinkDocbook(final String tempNavLinkDocbook)
	{
		this.tempNavLinkDocbook = tempNavLinkDocbook;
	}

	/**
	 * This function will take the XML in the topicXML String and use it to
	 * generate a text only view that will be used by Hibernate Search. The text
	 * extraction uses Jericho - http://jericho.htmlparser.net/
	 */
	@Transient
	@Field(name = "TopicSearchText", index = Index.TOKENIZED, store = Store.YES)
	public String getTopicSearchText()
	{
		final Source source = new Source(this.topicXML);
		source.fullSequentialParse();
		return source.getTextExtractor().toString();
	}

	public void addDetailsCommentToXML()
	{
		final Document doc = XMLUtilities.convertStringToDocument(this.topicXML);
		if (doc != null)
		{
			String detailsCommentContent = DETAILS_COMMENT_NODE_START + "\n\n" + "GENERAL DETAILS\n" + "\n" + "Topic ID: " + this.topicId + "\n" + "Topic Title: " + this.topicTitle + "\n" + "Topic Added By: " + this.topicAddedBy + "\n" + "Topic Description: " + this.topicText + "\n\n" + "TOPIC TAGS\n" + "\n";

			final ArrayList<TopicToTag> sortedTags = new ArrayList<TopicToTag>(this.getTopicToTags());
			Collections.sort(sortedTags, new TopicToTagTagIDSort());

			for (final TopicToTag topicToTag : sortedTags)
			{
				final Tag tag = topicToTag.getTag();
				detailsCommentContent += tag.getTagId() + ": " + tag.getTagName() + "\n";
			}

			detailsCommentContent += "\nSOURCE URLS\n\n";
			for (final TopicToTopicSourceUrl topicToSourceUrl : this.getTopicToTopicSourceUrls())
			{
				final TopicSourceUrl url = topicToSourceUrl.getTopicSourceUrl();
				detailsCommentContent += (url.getTitle() == null || url.getTitle().length() == 0 ? "Source URL: " : url.getTitle() + ": ");
				detailsCommentContent += url.getSourceUrl() + "\n";
			}

			final ArrayList<TopicToTopic> sortedTopics = new ArrayList<TopicToTopic>(this.getParentTopicToTopics());
			Collections.sort(sortedTopics, new TopicToTopicTopicIDSort());

			detailsCommentContent += "\nRELATED TOPICS\n\n";
			for (final TopicToTopic topicToTopic : sortedTopics)
			{
				final Topic topic = topicToTopic.getRelatedTopic();
				detailsCommentContent += topic.getTopicId() + ": " + topic.getTopicTitle() + "\n";
			}

			detailsCommentContent += "\n";

			final Node detailsComment = doc.createComment(detailsCommentContent);

			boolean foundComment = false;
			for (final Node comment : XMLUtilities.getComments(doc))
			{
				final String commentContent = comment.getTextContent();
				if (commentContent.startsWith(DETAILS_COMMENT_NODE_START))
				{
					foundComment = true;
					comment.getParentNode().replaceChild(detailsComment, comment);
					break;
				}
			}

			if (!foundComment)
			{
				doc.getDocumentElement().appendChild(detailsComment);
			}

			this.topicXML = XMLUtilities.convertDocumentToString(doc, Constants.XML_ENCODING);
		}
	}

	public void initializeFromTemplate()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

			if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.CONCEPT_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, CONCEPT_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.TASK_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, TASK_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.REFERENCE_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, REFERENCE_TOPIC_STRINGCONSTANTID).getConstantValue();
			}
			else if (filter(having(on(TopicToTag.class).getTag().getTagId(), equalTo(Constants.CONCEPTUALOVERVIEW_TAG_ID)), this.topicToTags).size() != 0)
			{
				this.topicXML = entityManager.find(StringConstants.class, CONCEPTUAL_OVERVIEW_TOPIC_STRINGCONSTANTID).getConstantValue();
			}

			syncTopicTitleWithXML();

			addDetailsCommentToXML();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Pprobably countn't find one of the string constants");
		}
	}

	private void syncTopicTitleWithXML()
	{

		final Document doc = XMLUtilities.convertStringToDocument(this.topicXML);
		if (doc != null)
		{
			final Element newTitle = doc.createElement(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
			newTitle.setTextContent(this.getTopicTitle());

			final Element docElement = doc.getDocumentElement();
			if (docElement != null && docElement.getNodeName().equals(DocBookUtilities.TOPIC_ROOT_NODE_NAME))
			{
				final NodeList titleNodes = docElement.getElementsByTagName(DocBookUtilities.TOPIC_ROOT_TITLE_NODE_NAME);
				/* see if we have a title node whose parent is the section */
				if (titleNodes.getLength() != 0 && titleNodes.item(0).getParentNode().equals(docElement))
				{
					final Node title = titleNodes.item(0);
					title.getParentNode().replaceChild(newTitle, title);
				}
				else
				{
					final Node firstNode = docElement.getFirstChild();
					if (firstNode != null)
						docElement.insertBefore(newTitle, firstNode);
					else
						docElement.appendChild(newTitle);
				}

				this.topicXML = XMLUtilities.convertDocumentToString(doc, Constants.XML_ENCODING);
			}
		}
	}

	@Transient
	public List<Tag> getTagsInCategoriesByID(final List<Integer> categories)
	{
		final List<Tag> retValue = new ArrayList<Tag>();

		for (final Integer categoryId : categories)
		{
			for (final TopicToTag topicToTag : this.topicToTags)
			{
				final Tag tag = topicToTag.getTag();

				if (topicToTag.getTag().isInCategory(categoryId))
				{
					if (!retValue.contains(tag))
						retValue.add(tag);
				}
			}
		}

		return retValue;
	}

	@Transient
	public List<Tag> getTagsInCategories(final List<Category> categories)
	{
		final List<Integer> catgeoriesByID = new ArrayList<Integer>();
		for (final Category category : categories)
			catgeoriesByID.add(category.getCategoryId());
		return getTagsInCategoriesByID(catgeoriesByID);
	}
	
	/**
	 * This function forces this topic to be rerendered, without updating any
	 * related topics.
	 */
	public void reRenderTopic()
	{
		try
		{
			WorkQueue.runAndWait(TopicRenderer.createNewInstance(this.getTopicId()));

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			entityManager.refresh(this);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a thread problem");
		}
	}

	@Transient
	public Topic getRelatedTopicByID(final Integer id)
	{
		for (final Topic topic : this.getOutgoingTopicsArray())
			if (topic.getTopicId().equals(id))
				return topic;
		return null;
	}

	@Transient
	public boolean isRerenderRelatedTopics()
	{
		return rerenderRelatedTopics;
	}

	public void setRerenderRelatedTopics(boolean rerenderRelatedTopics)
	{
		this.rerenderRelatedTopics = rerenderRelatedTopics;
	}

	/*
	 * Envers has issues with this: https://hibernate.onjira.com/browse/HHH-3853
	 * Hibernate also has issues here, so we use a join table instead
	 */
	/*
	 * @OneToOne(cascade = CascadeType.ALL, optional = true, fetch =
	 * FetchType.EAGER, orphanRemoval = true)
	 * 
	 * @PrimaryKeyJoinColumn
	 */

	@OneToOne(fetch = FetchType.EAGER, optional = true, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinTable(name = "TopicToTopicSecondOrderData", joinColumns =
	{ @JoinColumn(name = "TopicID", unique = true) }, inverseJoinColumns =
	{ @JoinColumn(name = "TopicSecondOrderDataID") })
	@NotAudited
	public TopicSecondOrderData getTopicSecondOrderData()
	{
		return topicSecondOrderData;
	}

	public void setTopicSecondOrderData(TopicSecondOrderData topicSecondOrderData)
	{
		this.topicSecondOrderData = topicSecondOrderData;
	}

	@Transient
	public String getTopicRendered()
	{
		if (this.topicSecondOrderData == null)
			return null;

		return topicSecondOrderData.getTopicHTMLView();
	}

	public void setTopicRendered(final String value)
	{
		if (this.topicSecondOrderData == null)
			this.topicSecondOrderData = new TopicSecondOrderData();

		this.topicSecondOrderData.setTopicHTMLView(value);
	}

	@Transient
	public String getTopicXMLErrors()
	{
		if (this.topicSecondOrderData == null)
			return null;

		return topicSecondOrderData.getTopicXMLErrors();
	}

	public void setTopicXMLErrors(final String value)
	{
		if (this.topicSecondOrderData == null)
		{
			this.topicSecondOrderData = new TopicSecondOrderData();
		}

		this.topicSecondOrderData.setTopicXMLErrors(value);
	}
}
