package com.redhat.topicindex.session;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

@Name("relatedTopicTagsList")
public class RelatedTopicTagsList extends ExtendedTopicList
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -2877706994939648373L;
	
	/** The id of the main topic */ 
	private Integer topicTopicId;
	/** The actual Topic object found with the topicTopicId */
	private Topic instance;
	
	public RelatedTopicTagsList()
	{
		super();
	}
	
	public RelatedTopicTagsList(final int limit) 
	{
		super(limit);
	}
	
	public RelatedTopicTagsList(final int limit, final String constructedEJBQL) 
	{
		super(limit, constructedEJBQL);
	}

	public RelatedTopicTagsList(final int limit, final String constructedEJBQL, final TopicFilter topic) 
	{
		super(limit, constructedEJBQL, topic);
	}
	
	protected void construct(final int limit, final String constructedEJBQL, final TopicFilter topic)
	{
		super.construct(limit, constructedEJBQL, topic, "relatedTopicTagsList");
	}
	
	public void oneWayToAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
		for (final Topic topic : topics)
		{
			final boolean isChild = mainTopic.isRelatedTo(topic);
			
			if (!isChild && !mainTopic.equals(topic))
				mainTopic.addRelationshipTo(topic);			
		}
		entityManager.persist(mainTopic);
	}
	
	public void oneWayFromAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
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
	
	public void twoWayWithAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
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
	
	public void removeToAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
		for (final Topic topic : topics)
		{
			mainTopic.removeRelationshipTo(topic);
		}
		entityManager.persist(mainTopic);
	}

	public void removeFromAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
		for (final Topic topic : topics)
		{
			if (topic.removeRelationshipTo(mainTopic))
				entityManager.persist(topic);
		}	
	}

	public void removeBetweenAll()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Topic mainTopic = entityManager.find(Topic.class, topicTopicId);
		final RelatedTopicTagsList fullList = new RelatedTopicTagsList(-1, this.constructedEJBQL, this.topic);
		final List<Topic> topics = fullList.getResultList();
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
	
	public void validate()
	{		
		super.validate();
		
		// build up a Filter object from the URL variables
		final Filter filter = EntityUtilities.populateFilter(
				FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), 
				Constants.FILTER_ID, 
				Constants.MATCH_TAG, 
				Constants.CATEORY_INTERNAL_LOGIC, 
				Constants.CATEORY_EXTERNAL_LOGIC);		
		
		// preselect the tags on the web page that relate to the tags selected by the filter
		selectedTags.populateTopicTags(filter, false);
		
		// sync up the filter field values
		for (final FilterField field : filter.getFilterFields())
			this.topic.setFieldValue(field.getField(), field.getValue());
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
		
		final String retValue = returnToSearch ?
				"/CustomSearchTopicList.xhtml": null;
		
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
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Topic from the database" );
		}
	}

	public Integer getTopicTopicId() {
		return topicTopicId;
	}

	public void setInstance(final Topic instance) 
	{
		this.instance = instance;
	}

	public Topic getInstance() {
		return instance;
	}
}
