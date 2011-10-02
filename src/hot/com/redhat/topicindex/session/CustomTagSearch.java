package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.HTTPUtilities;
import com.redhat.ecs.commonutils.MIMEUtilities;
import com.redhat.ecs.commonutils.ZipUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterCategory;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuilder;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

/**
 * This class is responsible for the functionality provided by the tag search
 * page, including the ability to load and modify filters, and create a docbook
 * zip file from related topics
 */
@Name("customTagSearch")
@Scope(ScopeType.PAGE)
public class CustomTagSearch
{
	/**
	 * a mapping of project and category details to tag details with sorting and
	 * selection information
	 */
	protected UIProjectData selectedTags = new UIProjectData();

	@In
	protected WorkingMemory businessRulesWorkingMemory;

	/** The id of the Filter object that has been loaded */
	private Integer selectedFilter;
	/** The name of the Filter object that has been loaded */
	private String selectedFilterName;
	/** A list of Filters from the database, used to populate the drop down list */
	private List<Filter> filters = new ArrayList<Filter>();
	/** The data structure that holds the docbook building options */
	private DocbookBuildingOptions docbookBuildingOptions = new DocbookBuildingOptions();

	private TopicFilter topic = new TopicFilter();

	public void setDocbookBuildingOptions(final DocbookBuildingOptions docbookBuildingOptions)
	{
		this.docbookBuildingOptions = docbookBuildingOptions;
	}

	public DocbookBuildingOptions getDocbookBuildingOptions()
	{
		return docbookBuildingOptions;
	}

	public void setTopic(final TopicFilter topic)
	{
		this.topic = topic;
	}

	public TopicFilter getTopic()
	{
		return topic;
	}

	public void setSelectedFilter(final Integer selectedFilter)
	{
		this.selectedFilter = selectedFilter;
	}

	public Integer getSelectedFilter()
	{
		return selectedFilter;
	}

	public void setFilters(final List<Filter> filters)
	{
		this.filters = filters;
	}

	public List<Filter> getFilters()
	{
		return filters;
	}

	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(final UIProjectData value)
	{
		selectedTags = value;
	}

	public void setSelectedFilterName(final String selectedFilterName)
	{
		this.selectedFilterName = selectedFilterName;
	}

	public String getSelectedFilterName()
	{
		return selectedFilterName;
	}

	public CustomTagSearch()
	{

	}

	/**
	 * This function is used to populate the list of filters, populate the
	 * current filter by the url variables, and to preselect the tags that match
	 * those selected in the current filter.
	 */
	@Create
	public void populate()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		Filter filter;

		// If we don't have a filter selected, populate the filter from the url
		// variables
		if (this.selectedFilter == null)
		{
			// build up a Filter object from the URL variables
			final FacesContext context = FacesContext.getCurrentInstance();
			filter = EntityUtilities.populateFilter(context, Constants.FILTER_ID, Constants.MATCH_TAG, Constants.CATEORY_INTERNAL_LOGIC, Constants.CATEORY_EXTERNAL_LOGIC);
		}
		// otherwise load the filter using the filter id
		else
		{
			filter = entityManager.find(Filter.class, selectedFilter);
		}

		// preselect the tags on the web page that relate to the tags selected
		// by the filter
		EntityUtilities.populateTopicTags(selectedTags, filter, false);

		// sync up the filter field values
		for (final FilterField field : filter.getFilterFields())
			this.topic.setFieldValue(field.getField(), field.getValue());

		getFilterList();
	}

	@SuppressWarnings("unchecked")
	protected void getFilterList()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// get a list of the existing filters in the database
		filters.clear();
		filters.addAll(entityManager.createQuery(Filter.SELECT_ALL_QUERY).getResultList());
	}

	/**
	 * This will redirect the browser to the search results. Since we want the
	 * search results to be bookmarkable, we can't use conversations or backing
	 * beans to store the data. Instead we have to provide url parameters that
	 * the TopicTagsList class can used to initialize itself.
	 */
	@End(beforeRedirect = true)
	public String doSearch()
	{
		final String retVal = doTopicSearch("/CustomSearchTopicList.seam");
		return retVal;
	}

	public void buildDocbook()
	{
		final Filter filter = new Filter();
		this.syncFilterWithUI(filter, false);

		final DocbookBuilder builder = new DocbookBuilder();
		builder.buildDocbookZipFile(filter, businessRulesWorkingMemory, docbookBuildingOptions);
	}

	public void downloadXML()
	{
		final Filter filter = new Filter();
		this.syncFilterWithUI(filter, false);

		final List<Topic> topicList = EntityUtilities.getTopicsFromFilter(filter);

		// build up the files that will make up the zip file
		final HashMap<String, byte[]> files = new HashMap<String, byte[]>();

		for (final Topic topic : topicList)
			files.put(topic.getTopicId() + ".xml", topic.getTopicXML() == null ? "".getBytes() : topic.getTopicXML().getBytes());

		byte[] zipFile = null;
		try
		{
			zipFile = ZipUtilities.createZip(files);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			zipFile = null;
		}

		HTTPUtilities.writeOutContent(zipFile, "XML.zip", MIMEUtilities.ZIP_MIME_TYPE);
	}

	/**
	 * This function takes the tags that have been selected, topic field filters
	 * and category boolean logic settings, and encapsulate this information in
	 * URL variables for CustomSearchTopicList.xhtml. The
	 * CustomSearchTopicList.xhtml then reads these variables and generates a
	 * list of Topic objects. By not using a backing bean, this URL can be
	 * copied and pasted without an existing conversation or session.
	 * 
	 * @return The URL containing the search information
	 */
	protected String doTopicSearch(final String url)
	{
		/*
		 * The filter class has a convenient method for creating url variables
		 * that match search queries. So create a new Filter instance, sync it
		 * with the UI (as defined by the values held by the selectedTags
		 * variable), and then pull out the URL variables.
		 */
		final Filter filter = new Filter();
		EntityUtilities.syncFilterWithCategories(filter, selectedTags);
		EntityUtilities.syncFilterWithFieldUIElements(filter, topic);
		EntityUtilities.syncFilterWithTags(filter, selectedTags);

		final String params = filter.buildFilterUrlVars();
		return url + "?" + params;
	}

	/**
	 * Loads a Filter object from the database given a FilterID, and selects the
	 * appropriate tags in the ui
	 */
	public void loadFilter()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// load the filter from the database using the filter id
		Filter filter = null;
		if (selectedFilter != null)
			filter = entityManager.find(Filter.class, selectedFilter);
		else
			filter = new Filter();

		this.selectedFilterName = filter.getFilterName();

		loadTagCheckboxes(filter);
		loadCategoryLogic(filter);
		loadFilterFields(filter);
	}

	protected void loadTagCheckboxes(final Filter filter)
	{
		// sync the Filter with the gui checkboxes
		for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
		{
			for (final UICategoryData category : project.getCategories())
			{
				for (final UITagData tag : category.getTags())
				{
					boolean found = false;
					for (final FilterTag filterTag : filter.getFilterTags())
					{
						if (tag.getId().equals(filterTag.getTag().getTagId()))
						{
							tag.setSelected(true);
							tag.setNotSelected(filterTag.getTagState() == Constants.NOT_MATCH_TAG_STATE);
							found = true;
							break;
						}
					}

					if (!found)
					{
						tag.setSelected(false);
						tag.setNotSelected(false);
					}
				}
			}
		}
	}

	protected void loadCategoryLogic(final Filter filter)
	{
		// sync the category logic states
		for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
		{
			for (final UICategoryData category : project.getCategories())
			{
				for (final FilterCategory filterCategory : filter.getFilterCategories())
				{
					final Integer categoryId = filterCategory.getCategory().getCategoryId();

					if (categoryId.equals(category.getId()))
					{
						final int catgeoryState = filterCategory.getCategoryState();

						if (catgeoryState == Constants.CATEGORY_INTERNAL_AND_STATE)
							category.setInternalLogic(Constants.AND_LOGIC);
						if (catgeoryState == Constants.CATEGORY_INTERNAL_OR_STATE)
							category.setInternalLogic(Constants.OR_LOGIC);

						if (catgeoryState == Constants.CATEGORY_EXTERNAL_AND_STATE)
							category.setExternalLogic(Constants.AND_LOGIC);
						if (catgeoryState == Constants.CATEGORY_EXTERNAL_OR_STATE)
							category.setExternalLogic(Constants.OR_LOGIC);
					}
				}

			}
		}
	}

	protected void loadFilterFields(final Filter filter)
	{
		for (final String fieldName : TopicFilter.getFilterNames().keySet())
			topic.setFieldValue(fieldName, null);

		for (final FilterField filterField : filter.getFilterFields())
		{
			final String field = filterField.getField();
			final String value = filterField.getValue();

			topic.setFieldValue(field, value);
		}
	}

	/**
	 * This function takes the tag selections and uses these to populate a
	 * Filter
	 * 
	 * @param filter The filter to sync with the tag selection
	 * @param persist true if this filter is being saved to the db (i.e. the
	 *            user is actually saving the filter), and false if it is not
	 *            (like when the user is building the docbook zip file)
	 */
	protected void syncFilterWithUI(final Filter filter, final boolean persist)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		EntityUtilities.syncFilterWithTags(filter, selectedTags);
		EntityUtilities.syncFilterWithCategories(filter, selectedTags);
		EntityUtilities.syncFilterWithFieldUIElements(filter, topic);

		if (persist)
			entityManager.persist(filter);

	}

	/**
	 * This function synchronizes the tags selected in the gui with the
	 * FilterTags in the Filter object, and saves the changes to the database.
	 * This persists the currently selected filter.
	 */
	public void saveFilter()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		Filter filter;

		// load the filter object if it exists
		if (this.selectedFilter != null)
			filter = entityManager.find(Filter.class, selectedFilter);
		else
			// get a reference to the Filter object
			filter = new Filter();

		// set the name
		filter.setFilterName(this.selectedFilterName);

		// populate the filter with the options that are selected in the ui
		syncFilterWithUI(filter, true);

		// save the changes
		entityManager.persist(filter);
		this.selectedFilter = filter.getFilterId();

		getFilterList();
	}

	public String getCreateNewTopicUrl()
	{
		return EntityUtilities.buildEditNewTopicUrl(selectedTags);
	}

	public String doTextSearch()
	{
		return "/CustomSearchTopicList.seam?" + Constants.TOPIC_TEXT_SEARCH_FILTER_VAR + "=" + this.topic.getTopicTextSearch();
	}
}
