package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.framework.EntityQuery;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.sort.GroupedTopicListNameComparator;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.structures.GroupedTopicList;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

public class GroupedTopicListBase
{
	protected List<GroupedTopicList> groupedTopicLists = new ArrayList<GroupedTopicList>();
	protected EntityQuery<Topic> pagingEntityQuery;
	protected String getAllQuery;
	/** holds the url variables that define a filters options */
	protected String urlVars;

	/**
	 * a mapping of category details to tag details with sorting and selection
	 * information
	 */
	protected UIProjectData selectedTags;
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
	protected HashMap<String, String> filterVars;
	/** Provides the heading that identifies the currently selected filter tags */
	protected String searchTagHeading;
	/** The selected tab */
	private String tab;

	public String getSearchTagHeading()
	{
		return searchTagHeading;
	}

	public void setSearchTagHeading(final String value)
	{
		searchTagHeading = value;
	}
	
	public String getTab()
	{
		return tab;
	}

	public void setTab(final String tab)
	{
		this.tab = tab;
	}

	public void setUrlVars(final String urlVars)
	{
		this.urlVars = urlVars;
	}

	public String getUrlVars()
	{
		return urlVars;
	}

	public HashMap<String, String> getFilterVars()
	{
		return filterVars;
	}

	public void setFilterVars(final HashMap<String, String> filterVars)
	{
		this.filterVars = filterVars;
	}

	public void setSelectedTags(final UIProjectData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public List<GroupedTopicList> getGroupedTopicLists()
	{
		return groupedTopicLists;
	}

	public void setGroupedTopicLists(List<GroupedTopicList> groupedTopicLists)
	{
		this.groupedTopicLists = groupedTopicLists;
	}

	public boolean isNextExists()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isNextExists();
		return false;
	}

	public boolean isPreviousExists()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isPreviousExists();
		return false;
	}

	public Long getLastFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getLastFirstResult();
		return 0l;
	}

	public int getNextFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getNextFirstResult();
		return 0;
	}

	public int getPreviousFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getPreviousFirstResult();
		return 0;
	}

	public Integer getFirstResult()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getFirstResult();
		return 0;
	}

	public void setFirstResult(final Integer firstResult)
	{
		for (final GroupedTopicList groupedTopicList : groupedTopicLists)
			groupedTopicList.getTopicList().setFirstResult(firstResult);
	}

	public List<Topic> getResultList()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getResultList();
		return null;
	}

	public boolean isPaginated()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.isPaginated();
		return false;
	}

	public Long getResultCount()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getResultCount();
		return 0l;
	}

	public void setOrderColumn(final String orderColumn)
	{
		if (pagingEntityQuery != null)
			pagingEntityQuery.setOrderColumn(orderColumn);
	}

	public String getOrderColumn()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getOrderColumn();
		return null;
	}

	public void setOrderDirection(final String orderDirection)
	{
		if (pagingEntityQuery != null)
			pagingEntityQuery.setOrderDirection(orderDirection);
	}

	public String getOrderDirection()
	{
		if (pagingEntityQuery != null)
			return pagingEntityQuery.getOrderDirection();
		return null;
	}

	public void create()
	{		
		final Map<String, String> urlParameters = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		final Filter filter = EntityUtilities.populateFilter(urlParameters, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.GROUP_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);
		getAllQuery = filter.buildQuery();

		// get a map of variable names to variable values
		filterVars = filter.getUrlVariables();

		// get the heading to display over the list of topics
		searchTagHeading = filter.getFilterTitle();

		/*
		 * get a string that can be appended to a url that contains the url
		 * variables
		 */
		urlVars = filter.buildFilterUrlVars();

		final List<Integer> groupedTags = new ArrayList<Integer>();

		for (final String urlParam : urlParameters.keySet())
		{
			if (urlParam.startsWith(Constants.GROUP_TAG))
			{
				try
				{
					final Integer tagID = Integer.parseInt(urlParam.replace(Constants.GROUP_TAG, ""));
					final Integer state = Integer.parseInt(urlParameters.get(urlParam));
					if (state == Constants.GROUP_TAG_STATE)
					{
						final Tag tag = EntityUtilities.getTagFromId(tagID);
						if (tag != null)
						{
							/*
							 * build the query, and add a new restriction that
							 * forces the group tag to be present
							 */
							final String query = getAllQuery + " AND exists (select 1 from TopicToTag topicToTag where topicToTag.topic = topic and topicToTag.tag.tagId = " + tagID + ") ";
							final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, query);
							if (topics.getResultCount() != 0)
							{
								final GroupedTopicList groupedTopicList = new GroupedTopicList();
								groupedTopicList.setDetachedTag(tag);
								groupedTopicList.setTopicList(topics);

								groupedTopicLists.add(groupedTopicList);

								if (pagingEntityQuery == null || pagingEntityQuery.getResultCount() < topics.getResultCount())
									pagingEntityQuery = topics;

								groupedTags.add(tagID);
							}
						}
					}
				}
				catch (final Exception ex)
				{
					SkynetExceptionUtilities.handleException(ex, true, "Probably a bad url parameter passed as a grouping tag");
				}
			}
		}
		
		/* sort by tag name, and then add the unsorted topics on the end */
		Collections.sort(groupedTopicLists, new GroupedTopicListNameComparator());

		/*
		 * we didn't have any groups, so just find all the matching topics and
		 * dump them in the default group
		 */
		if (groupedTags.size() == 0)
		{
			final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, getAllQuery);

			final Tag dummyTag = new Tag();
			dummyTag.setTagName("Ungrouped Results");

			final GroupedTopicList groupedTopicList = new GroupedTopicList();
			groupedTopicList.setDetachedTag(dummyTag);
			groupedTopicList.setTopicList(topics);

			groupedTopicLists.add(groupedTopicList);
			pagingEntityQuery = topics;
		}
		/*
		 * Find that topics that are part of the query, but couldn't be matched
		 * in any group
		 */
		else
		{
			String notQuery = "";
			for (final Integer tagID : groupedTags)
			{
				notQuery += " AND not exists (select 1 from TopicToTag topicToTag where topicToTag.topic = topic and topicToTag.tag.tagId = " + tagID + ")";
			}

			final ExtendedTopicList topics = new ExtendedTopicList(Constants.DEFAULT_PAGING_SIZE, getAllQuery + notQuery);

			if (topics.getResultCount() != 0)
			{
				final Tag dummyTag = new Tag();
				dummyTag.setTagName("Ungrouped Results");

				final GroupedTopicList groupedTopicList = new GroupedTopicList();
				groupedTopicList.setDetachedTag(dummyTag);
				groupedTopicList.setTopicList(topics);

				groupedTopicLists.add(groupedTopicList);

				if (pagingEntityQuery == null || pagingEntityQuery.getMaxResults() < topics.getMaxResults())
					pagingEntityQuery = topics;
			}
		}
	}
}
