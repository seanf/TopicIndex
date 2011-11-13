package com.redhat.topicindex.session;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

@Name("relatedTopicTagsList")
public class RelatedTopicTagsList extends GroupedTopicListBase
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -2877706994939648373L;

	/** The id of the main topic */
	private Integer topicTopicId;
	/** The actual Topic object found with the topicTopicId */
	private Topic instance;
	/** The object that holds the filter field values */
	private TopicFilter topic = new TopicFilter();
	/** The selected tab */
	private String tab;

	public RelatedTopicTagsList()
	{

	}

	@Create
	public void create()
	{
		super.create();

		// build up a Filter object from the URL variables
		final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

		/* preselect the tags on the web page that relate to the tags selected
		 by the filter */
		selectedTags = new UIProjectData();
		selectedTags.populateTopicTags(filter, false);

		// sync up the filter field values
		for (final FilterField field : filter.getFilterFields())
			this.topic.setFieldValue(field.getField(), field.getValue());
	}

	public void oneWayToAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isChild = mainTopic.isRelatedTo(topic);

				if (!isChild && !mainTopic.equals(topic))
					mainTopic.addRelationshipTo(topic);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void oneWayFromAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isChild = topic.isRelatedTo(mainTopic);

				if (!isChild && !mainTopic.equals(topic))
				{
					topic.addRelationshipTo(mainTopic);
					entityManager.persist(topic);
				}
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void twoWayWithAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				final boolean isMainTopicChild = topic.isRelatedTo(mainTopic);

				if (!isMainTopicChild && !mainTopic.equals(topic))
				{
					topic.addRelationshipTo(mainTopic);
					entityManager.persist(topic);
				}

				final boolean isTopicChild = mainTopic.isRelatedTo(topic);

				if (!isTopicChild && !mainTopic.equals(topic))
					mainTopic.addRelationshipTo(topic);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeToAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				mainTopic.removeRelationshipTo(topic);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeFromAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				if (topic.removeRelationshipTo(mainTopic))
					entityManager.persist(topic);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public void removeBetweenAll()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
			final List<Topic> topics = entityManager.createQuery(getAllQuery).getResultList();
			for (final Topic topic : topics)
			{
				if (topic.removeRelationshipTo(mainTopic))
				{
					entityManager.persist(topic);
				}

				mainTopic.removeRelationshipTo(topic);
			}
			entityManager.persist(mainTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an issue with the topic with id" + topicTopicId);
		}
	}

	public String doSearch()
	{
		return "/CustomRelatedTopicList.seam?" + getSearchUrlVars();
	}

	protected String getSearchUrlVars()
	{
		return getSearchUrlVars(null);
	}

	protected String getSearchUrlVars(final String startRecord)
	{
		final Filter filter = new Filter();
		filter.syncFilterWithCategories(selectedTags);
		filter.syncFilterWithFieldUIElements(topic);
		filter.syncFilterWithTags(selectedTags);

		final String params = filter.buildFilterUrlVars();
		return "topicTopicId=" + topicTopicId + "&" + params;
	}

	public boolean isRelatedTo(final Integer otherTopicId)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			return topic.isRelatedTo(otherTopic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving Topics from the database");
		}

		return false;
	}

	public boolean isRelatedFrom(final Integer otherTopicId)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);
			return otherTopic.isRelatedTo(topic);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving Topics from the database");
		}

		return false;
	}

	public boolean isRelatedBothWays(final Integer otherTopicId)
	{
		return isRelatedFrom(otherTopicId) && isRelatedTo(otherTopicId);
	}

	public boolean isNotRelated(final Integer otherTopicId)
	{
		return !isRelatedFrom(otherTopicId) && !isRelatedTo(otherTopicId);
	}

	public boolean isRelatedOneWay(final Integer otherTopicId)
	{
		return isRelatedFrom(otherTopicId) != isRelatedTo(otherTopicId);
	}

	public String removeRelationship(final Integer otherTopicId, final boolean to, final boolean from, final boolean returnToSearch)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic thisTopic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

			if (from)
			{
				if (thisTopic.removeRelationshipTo(otherTopic))
				{
					entityManager.persist(thisTopic);
					instance = thisTopic;
				}
			}

			if (to)
			{
				if (otherTopic.removeRelationshipTo(thisTopic))
				{
					entityManager.persist(otherTopic);
				}
			}

			entityManager.flush();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving or persiting Topics in the database");
		}

		final String retValue = returnToSearch ? "/CustomSearchTopicList.xhtml" : null;

		return retValue;
	}

	public String createRelationship(final Integer otherTopicId, final boolean to, final boolean from, final boolean returnToSearch)
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic thisTopic = entityManager.find(Topic.class, topicTopicId);
			final Topic otherTopic = entityManager.find(Topic.class, otherTopicId);

			if (from)
			{
				if (!thisTopic.isRelatedTo(otherTopic) && !thisTopic.equals(otherTopic))
				{
					if (thisTopic.addRelationshipTo(otherTopic))
					{
						entityManager.persist(thisTopic);
					}
				}
			}

			if (to)
			{
				if (!otherTopic.isRelatedTo(thisTopic) && !thisTopic.equals(otherTopic))
				{
					if (otherTopic.addRelationshipTo(thisTopic))
					{
						entityManager.persist(otherTopic);
						entityManager.refresh(thisTopic);
					}
				}
			}

			instance = thisTopic;
			entityManager.flush();

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving or perssting Topics in the database");
		}

		final String retValue = returnToSearch ? "/CustomSearchTopicList.xhtml" : null;

		return retValue;
	}

	public void setTopicTopicId(final Integer topicTopicId)
	{
		this.topicTopicId = topicTopicId;

		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			instance = entityManager.find(Topic.class, topicTopicId);
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Topic from the database");
		}
	}

	public Integer getTopicTopicId()
	{
		return topicTopicId;
	}

	public void setInstance(final Topic instance)
	{
		this.instance = instance;
	}

	public Topic getInstance()
	{
		return instance;
	}

	public TopicFilter getTopic()
	{
		return topic;
	}

	public void setTopic(final TopicFilter topic)
	{
		this.topic = topic;
	}

	public String getTab()
	{
		return tab;
	}

	public void setTab(final String tab)
	{
		this.tab = tab;
	}
}
