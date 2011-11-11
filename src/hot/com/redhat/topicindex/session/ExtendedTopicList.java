package com.redhat.topicindex.session;

import java.util.Arrays;
import java.util.HashMap;
import javax.faces.context.FacesContext;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

/**
 * This is a base class extended by other classes that need to display a list of
 * topics with an extended filter set.
 */
public class ExtendedTopicList extends TopicList
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -4553573868560054166L;

	/** Provides the heading that identifies the currently selected filter tags */
	protected String searchTagHeading;
	/**
	 * a mapping of category details to tag details with sorting and selection
	 * information
	 */
	protected UIProjectData selectedTags;
	protected String constructedEJBQL;
	/** holds the url variables that define a filters options */
	protected String urlVars;
	/**
	 * The query used to find the entities in the list is set in the constructor
	 * using setEjbql. Because we modify this query based on the tags in the
	 * url, the url needs to have all the variables for the tags and categories.
	 * To ensure that the url always has these variables, we parse them out and
	 * save them in the filterVars collection, which is then read with a jstl
	 * foreach tag to place the required params into any link (like next,
	 * previous, first page, last page) that may require a new instance of this
	 * object to be constructed.
	 */
	private HashMap<String, String> filterVars;



	public void setSelectedTags(final UIProjectData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public String getSearchTagHeading()
	{
		return searchTagHeading;
	}

	public void setSearchTagHeading(final String value)
	{
		searchTagHeading = value;
	}

	public void setUrlVars(final String urlVars)
	{
		this.urlVars = urlVars;
	}

	public String getUrlVars()
	{
		return urlVars;
	}

	public ExtendedTopicList(final int limit)
	{
		super(limit);
	}

	public ExtendedTopicList(final int limit, final String constructedEJBQL, final TopicFilter topic)
	{
		super(limit, constructedEJBQL, topic);
	}

	public ExtendedTopicList(final int limit, final String constructedEJBQL)
	{
		super(limit, constructedEJBQL);
	}

	public ExtendedTopicList()
	{
		super(25);
	}

	/**
	 * construct provides common initialization for the overloaded constructors.
	 * We have the ability to supply the Topic object through which the filters
	 * work as well as the HQL query in order to overcome the limitation of the
	 * EntityQuery class whereby you can not unset the setMaxResults function.
	 * These two properties allow us to construct a new TopicTagsList without
	 * paging that has all the elements included in a paged version. This is
	 * used in bulk tagging where you view a paged list of results, but apply
	 * tags to all elements.
	 */
	protected void construct(final int limit, final String constructedEJBQL, final TopicFilter topic, final String beanName)
	{
		super.construct(limit, constructedEJBQL, topic);

		// populate the bulk tag database
		selectedTags = new UIProjectData();
		selectedTags.populateTopicTags();

		// if a query is not supplied, work it out from the url
		if (constructedEJBQL == null)
		{
			// initialize filter home
			final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG,
					Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

			/* the filter may be null if an invalid varibale was passed in the URL */
			if (filter != null)
			{
				// get the heading to display over the list of topics
				searchTagHeading = filter.getFilterTitle();
	
				// get a string that can be appended to a url that contains the url
				// variables
				urlVars = filter.buildFilterUrlVars();
	
				// get a map of variable names to variable values
				filterVars = filter.getUrlVariables();
	
				// add the and and or categories clause to the default statement
				final String query = filter.buildQuery();
				this.constructedEJBQL = query;
			}
			else
			{
				searchTagHeading = "Invalid URL variables. Showing all topics.";
				this.constructedEJBQL = Topic.SELECT_ALL_QUERY;
			}
		}
		else
		{
			// set the query to the value that was passed in
			this.constructedEJBQL = constructedEJBQL;

			/*
			 * don't worry about the other values, as in this case we are just
			 * creating a copy of the visible list of topics. this code won't
			 * have anything bound to the other values.
			 */
		}

		setEjbql(this.constructedEJBQL);

		// if we have supplied a topic to be used for the filter, assign it
		if (topic != null)
			this.topic = topic;
	}

	public HashMap<String, String> getFilterVars()
	{
		return filterVars;
	}

	public void setFilterVars(HashMap<String, String> filterVars)
	{
		this.filterVars = filterVars;
	}
}
