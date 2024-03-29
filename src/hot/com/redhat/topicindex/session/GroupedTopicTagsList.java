package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.collection.LambdaCollections.with;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

import com.redhat.ecs.commonutils.HTTPUtilities;
import com.redhat.ecs.commonutils.MIMEUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicToTag;
import com.redhat.topicindex.entity.TopicToTopic;
import com.redhat.topicindex.entity.TopicToTopicSourceUrl;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.GroupedTopicList;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UITagData;

@Name("groupedTopicTagsList")
public class GroupedTopicTagsList extends GroupedTopicListBase
{
	@In
	private Identity identity;
	@In
	private WorkingMemory businessRulesWorkingMemory;

	/** A list of quick tag ids, which will be populated by a drools rule */
	private ArrayList<UITagData> quickTags = new ArrayList<UITagData>();

	// used by the action links
	private Integer actionTopicID;

	// used by the remove tag link
	private Integer otherTopicId;

	public String doBackToSearchLink()
	{
		return "/CustomSearchTopics.seam?" + urlVars;
	}

	public void setOtherTopicId(final Integer otherTopicId)
	{
		this.otherTopicId = otherTopicId;
	}

	public Integer getOtherTopicId()
	{
		return otherTopicId;
	}

	public void setQuickTags(final ArrayList<UITagData> quickTags)
	{
		this.quickTags = quickTags;
	}

	public ArrayList<UITagData> getQuickTags()
	{
		return quickTags;
	}

	public void setActionTopicID(final Integer actionTopicID)
	{
		this.actionTopicID = actionTopicID;
	}

	public Integer getActionTopicID()
	{
		return actionTopicID;
	}

	@Create
	public void create()
	{
		super.create();

		// populate the bulk tag database
		selectedTags = new UIProjectData();
		selectedTags.populateTopicTags();

		loadQuickTags();
	}

	/********************************************************************************/

	private void loadQuickTags()
	{
		// Use drools to populate the quick tags
		businessRulesWorkingMemory.setGlobal("quickTags", quickTags);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		businessRulesWorkingMemory.insert(new DroolsEvent("PopulateQuickTags"));
		businessRulesWorkingMemory.fireAllRules();
	}

	/**
	 * This function will apply or remove the bulk tags that were selected
	 */
	public void applyBulkTags()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Topic> bulkTagList = entityManager.createQuery(getAllQuery).getResultList();

		/*
		 * Loop through once, and remove all the tags. then loop through again
		 * and add the tags. We do this because of the validation rules that
		 * prevent multiple tags from being added in a mutually exclusive
		 * category
		 */
		for (final Boolean remove : new Boolean[]
		{ true, false })
		{
			// loop through each topic
			for (final Topic topic : bulkTagList)
			{
				for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
				{
					// loop through each selected tag category
					for (final UICategoryData entry : project.getCategories())
					{
						// loop through each tag in the category
						for (final UITagData tagDetails : entry.getTags())
						{
							// we are only interested in those that have been
							// selected
							if ((tagDetails.isNotSelected() && remove) || (tagDetails.isSelected() && !remove))
							{
								manageTag(topic, tagDetails, remove);
							}
						}
					}
				}
			}
		}
	}

	protected void manageTag(final Topic topic, final UITagData tagDetails, final boolean remove)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.getReference(Tag.class, tagDetails.getId());
		final Topic persistTopic = entityManager.find(Topic.class, topic.getTopicId());

		// remove tags
		if (remove && tagDetails.isNotSelected())
		{
			with(persistTopic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(tag)));
		}
		// add tags
		else if (!remove && !tagDetails.isNotSelected())
		{
			if (filter(having(on(TopicToTag.class).getTag(), equalTo(tag)), persistTopic.getTopicToTags()).size() == 0)
				persistTopic.getTopicToTags().add(new TopicToTag(topic, tag));
		}

		persistTopic.validate();

		entityManager.persist(persistTopic);
		entityManager.flush();
	}

	public void downloadCSV()
	{
		String csv = Topic.getCSVHeaderRow();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Topic> bulkTagList = entityManager.createQuery(getAllQuery).getResultList();
		{
			// loop through each topic
			for (final Topic topic : bulkTagList)
				csv += "\n" + topic.getCSVRow();
		}

		HTTPUtilities.writeOutContent(csv.getBytes(), "Topics.csv");
	}

	public void downloadXML()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Topic> bulkTagList = entityManager.createQuery(getAllQuery).getResultList();

		// build up the files that will make up the zip file
		final HashMap<String, byte[]> files = new HashMap<String, byte[]>();

		for (final Topic topic : bulkTagList)
			files.put(topic.getTopicId() + ".xml", topic.getTopicXML() == null ? "".getBytes() : topic.getTopicXML().getBytes());

		byte[] zipFile = null;
		try
		{
			zipFile = ZipUtilities.createZip(files);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "An error creating a ZIP file");
			zipFile = null;
		}

		HTTPUtilities.writeOutContent(zipFile, "XML.zip", MIMEUtilities.ZIP_MIME_TYPE);
	}

	public String createPopulatedTopic()
	{
		return "/TopicEdit.seam?" + urlVars;
	}

	public void addTagById(final Integer topicID, final Integer tagID)
	{

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.getReference(Tag.class, tagID);
		final Topic topic = entityManager.find(Topic.class, topicID);

		/* validity checking */
		if (tag == null)
			return;

		if (topic == null)
			return;

		if (filter(having(on(TopicToTag.class).getTag(), equalTo(tag)), topic.getTopicToTags()).size() != 0)
		{
			with(topic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(tag)));
		}
		else
		{
			topic.getTopicToTags().add(new TopicToTag(topic, tag));

			// remove any excluded tags
			for (final Tag excludeTag : tag.getExcludedTags())
			{
				if (excludeTag.equals(tag))
					continue;

				with(topic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(excludeTag)));
			}

			for (final TagToCategory category : tag.getTagToCategories())
			{
				if (category.getCategory().isMutuallyExclusive())
				{
					for (final TagToCategory categoryTag : category.getCategory().getTagToCategories())
					{
						if (categoryTag.getTag().equals(tag))
							continue;

						with(topic.getTopicToTags()).remove(having(on(TopicToTag.class).getTag(), equalTo(categoryTag.getTag())));
					}
				}
			}
		}

		entityManager.persist(topic);
		entityManager.flush();

		/*
		 * we might have added a tag that excludes this topic from the previous
		 * search, so we have to refresh the list
		 */
		for (final GroupedTopicList groupedTopicList : groupedTopicLists)
			groupedTopicList.getTopicList().refresh();
	}

	protected void removeRelationship(final boolean to)
	{
		if (actionTopicID != null && otherTopicId != null)
		{
			try
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, actionTopicID);
				final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

				if (to)
				{
					topic.removeRelationshipTo(otherTopic);
					entityManager.persist(topic);
				}
				else
				{
					otherTopic.removeRelationshipTo(topic);
					entityManager.persist(otherTopic);
				}

				entityManager.flush();
			}
			catch (final EntityNotFoundException ex)
			{
				SkynetExceptionUtilities.handleException(ex, false, "Probably an issue retrieving the Topic entities");
			}
		}

		actionTopicID = null;
		otherTopicId = null;
	}

	/**
	 * This function removes a related topic. The two variables used by this
	 * function, actionTopicID and removeTagByIDRealtedTopicID, are set by a
	 * a4j:actionparam element in the xhtml file.
	 */
	public void removeRelatedById()
	{
		removeRelationship(true);
	}

	public void removeIncomingRelatedById()
	{
		removeRelationship(false);
	}

	public void makeTwoWayRelatedById()
	{
		try
		{
			if (actionTopicID != null && otherTopicId != null)
			{
				EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic actionTopic = entityManager.find(Topic.class, actionTopicID);
				final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

				actionTopic.addRelationshipTo(otherTopic);
				otherTopic.addRelationshipTo(actionTopic);

				entityManager.persist(actionTopic);
				entityManager.persist(otherTopic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}
	}

	public void removeOtherRelationship()
	{
		try
		{
			if (actionTopicID != null && otherTopicId != null)
			{
				EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic actionTopic = entityManager.find(Topic.class, actionTopicID);
				final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

				otherTopic.removeRelationshipTo(actionTopic);

				entityManager.persist(otherTopic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}
	}

	public void removeThisRelationship()
	{
		try
		{
			if (actionTopicID != null && otherTopicId != null)
			{
				EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic actionTopic = entityManager.find(Topic.class, actionTopicID);
				final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

				actionTopic.removeRelationshipTo(otherTopic);

				entityManager.persist(actionTopic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}
	}

	public void removeBothRelationships()
	{
		try
		{
			if (actionTopicID != null && otherTopicId != null)
			{
				EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic actionTopic = entityManager.find(Topic.class, actionTopicID);
				final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

				actionTopic.removeRelationshipTo(otherTopic);
				otherTopic.removeRelationshipTo(actionTopic);

				entityManager.persist(actionTopic);
				entityManager.persist(otherTopic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}
	}

	public void mergeTopics()
	{
		try
		{
			if (actionTopicID != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, actionTopicID);

				// merge the text
				String topicTags = "\n\n[Original Tags]\n";
				for (final TopicToTag topicToTag : topic.getTopicToTags())
				{
					final Tag topicTag = topicToTag.getTag();
					topicTags += "[" + topicTag.getTagId() + "] " + topicTag.getTagName() + "\n";
				}

				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					topicTags += "\n\n[Tags From TopicID " + relatedTopic.getTopicId() + "]\n";
					for (final TopicToTag relatedTopicToTag : relatedTopic.getTopicToTags())
					{
						final Tag relatedTag = relatedTopicToTag.getTag();
						topicTags += "[" + relatedTag.getTagId() + "] " + relatedTag.getTagName() + "\n";
					}
				}

				// merge the titles
				String topicTitle = "";
				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					topicTitle += "\n\n[TopicTitle From TopicID " + relatedTopic.getTopicId() + "]\n";
					topicTitle += relatedTopic.getTopicTitle();
				}

				// merge the text
				String topicText = "\n\n[Original TopicText]\n";
				topicText += topic.getTopicText();

				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					topicText += "\n\n[TopicText From TopicID " + relatedTopic.getTopicId() + "]\n";
					topicText += relatedTopic.getTopicText();
				}

				// merge the added by
				String topicAddedBy = "";
				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					topicAddedBy += "\n\n[TopicAddedBy From TopicID " + relatedTopic.getTopicId() + "]\n";
					topicAddedBy += relatedTopic.getTopicAddedBy();
				}

				// merge the source material urls
				String topicUrls = "";
				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					topicUrls += "\n\n[TopicURLs From TopicID " + relatedTopic.getTopicId() + "]\n";
					for (final TopicToTopicSourceUrl url : relatedTopic.getTopicToTopicSourceUrls())
						topicUrls += url.getTopicSourceUrl() + "\n";
				}

				// the topic text will contain the details of the other topics
				topic.setTopicText(topicTags + topicText + topicTitle + topicAddedBy + topicUrls);

				/*
				 * we will combine the tags, and let the topic resolve any
				 * conflicts when it is saved
				 */

				for (final TopicToTopic topicToTopic : topic.getParentTopicToTopics())
				{
					final Topic relatedTopic = topicToTopic.getRelatedTopic();
					for (final TopicToTag relatedTopicToTag : relatedTopic.getTopicToTags())
					{
						if (filter(having(on(TopicToTag.class).getTag(), equalTo(relatedTopicToTag.getTag())), topic.getTopicToTags()).size() == 0)
							topic.getTopicToTags().add(new TopicToTag(topic, relatedTopicToTag.getTag()));
					}
				}

				entityManager.persist(topic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving or updating a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}

	}

	public String getRelatedTopicsUrl()
	{

		final Integer thisActionTopicID = actionTopicID;

		actionTopicID = null;
		otherTopicId = null;

		if (thisActionTopicID != null)
			return "/CustomSearchTopicList.seam?topicRelatedTo=" + thisActionTopicID;

		return null;
	}

	public void removeRelationships()
	{
		try
		{
			if (actionTopicID != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, actionTopicID);
				topic.getParentTopicToTopics().clear();
				entityManager.persist(topic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably a problem retrieving or updating a Topic entity");
		}
		finally
		{
			actionTopicID = null;
			otherTopicId = null;
		}
	}

	public String viewRelatedTopic()
	{
		final Integer thisOtherTopicId = otherTopicId;

		actionTopicID = null;
		otherTopicId = null;

		if (thisOtherTopicId != null)
			return "/Topic.xhtml?topicTopicId=" + thisOtherTopicId;

		return null;
	}

}
