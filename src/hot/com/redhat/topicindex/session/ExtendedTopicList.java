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

	protected String constructedEJBQL;

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
		super(Constants.DEFAULT_PAGING_SIZE);
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
	protected void construct(final int limit, final String constructedEJBQL, final TopicFilter topic)
	{
		super.construct(limit, constructedEJBQL, topic);

		// if a query is not supplied, work it out from the url
		if (constructedEJBQL == null)
		{
			// initialize filter home
			final Filter filter = EntityUtilities.populateFilter(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap(), Constants.FILTER_ID, Constants.MATCH_TAG,
					Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);

			/* the filter may be null if an invalid varibale was passed in the URL */
			if (filter != null)
			{
				// add the and and or categories clause to the default statement
				final String query = filter.buildQuery();
				this.constructedEJBQL = query;
			}
			else
			{
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

}
