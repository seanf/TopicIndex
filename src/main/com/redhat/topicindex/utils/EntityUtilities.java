package com.redhat.topicindex.utils;

import static ch.lambdaj.Lambda.*;
import static org.hamcrest.Matchers.*;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.joda.time.format.ISODateTimeFormat;
import org.jsoup.Jsoup;

import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterCategory;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToProject;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.sort.TopicTagCategoryDataNameSorter;
import com.redhat.topicindex.utils.structures.tags.UIProjectTagData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

public class EntityUtilities
{
	static public void populateTopicTags(final Topic topic, final UIProjectData selectedTags)
	{
		populateTags(topic.getTags(), selectedTags, null, true);
	}

	static public void populateTopicTags(final UIProjectData selectedTags)
	{
		populateTags(null, selectedTags, null, true);
	}

	static public void populateTopicTags(final UIProjectData selectedTags, final Filter filter)
	{
		populateTags(null, selectedTags, filter, true);
	}

	static public void populateTopicTags(final UIProjectData selectedTags, final Filter filter, final boolean setSelectedItemInCategory)
	{
		populateTags(null, selectedTags, filter, setSelectedItemInCategory);
	}

	static protected void populateTopicTags(final Topic topic, final UIProjectData selectedTags, final Filter filter)
	{
		populateTags(topic.getTags(), selectedTags, filter, true);
	}

	/**
	 * This function is used to populate the data structures used to display
	 * categories and their tags.
	 * 
	 * @param topic If this is not null, it is used to determine which tags are
	 *            currently selected
	 * @param selectedTags This is a map of Category data to an ArrayList of Tag
	 *            data. When used to represent categories, the GuiInputData
	 *            selected field is used to indicate whether this is a mutually
	 *            exclusive category. If true, only one tag should be able to be
	 *            selected. If false, many tags can be selected. The decision to
	 *            make a category mutually exclusive is left up to a Drools rule
	 *            file in order to keep this code as process agnostic as
	 *            possible. This function will populate the category
	 *            GuiInputData objects (see the setSelectedItemInCategory param)
	 *            so they can be used either way.
	 * @param filter This object represents the filter applied to the page. Like
	 *            topic, this is optionally used to preselect those tags that
	 *            are used in the filter. Either filter or topic can be not
	 *            null, but if both are supplied (and this shouldn't happen) the
	 *            topic is used.
	 * @param setSelectedItemInCategory If this is false, the selectedID value
	 *            in the Key of the selectedTags parameter will not be modified
	 *            to indicate which of the children tags has been selected. This
	 *            needs to be set to false to avoid changing the equality of the
	 *            keys in the TreeMap as tag selections change.
	 */
	@SuppressWarnings("unchecked")
	static public void populateTags(final List<Tag> checkedTags, final UIProjectData selectedTags, final Filter filter, final boolean setSelectedItemInCategory)
	{
		try
		{
			/*
			 * this should be empty anyway, but make sure we start with a clean
			 * slate
			 */
			selectedTags.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Project> projectList = entityManager.createQuery(Project.SELECT_ALL_QUERY).getResultList();
			final List<TagToCategory> tagToCategoryList = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();

			/*
			 * First we create an entry for tags that have not been associated
			 * with a project. These will become common tags.
			 * 
			 * Use a HQL query to find those tags that have no associated
			 * project.
			 */
			final List<Tag> commonTagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY + " where tag.tagToProjects is empty").getResultList();

			/* create a "common" project */
			final UIProjectCategoriesData commonProjectDetails = new UIProjectCategoriesData(Constants.COMMON_PROJECT_NAME, Constants.COMMON_PROJECT_DESCRIPTION, Constants.COMMON_PROJECT_ID);
			selectedTags.getProjectCategories().add(commonProjectDetails);

			/* get a list of the categories that the common tags fall into */
			final List<Category> commonCategories = new ArrayList<Category>();
			for (final Tag tag : commonTagList)
			{
				for (final TagToCategory tagToCategory : tag.getTagToCategories())
				{
					final Category category = tagToCategory.getCategory();
					if (!commonCategories.contains(category))
						commonCategories.add(category);
				}
			}

			/* create the categories under the common project */
			for (final Category category : commonCategories)
			{
				final Integer catID = category.getCategoryId();

				/* the object that represents the category */
				final UICategoryData commonCatDetails = createUICategoryData(category, null, filter);
				commonProjectDetails.getCategories().add(commonCatDetails);

				/*
				 * create an entry for all the tags that have no assigned
				 * project
				 */
				for (final TagToCategory tagToCategory : category.getTagToCategories())
				{
					final Tag tag = tagToCategory.getTag();
					if (tag.getTagToProjects().size() == 0)
					{
						final UITagData tagData = createUITagData(tag, catID, filter, checkedTags, tagToCategoryList);

						/*
						 * set the selected id in the category to the last
						 * selected tag this is used by the xhtml page when a
						 * category is marked as mutually exclusive, and ignored
						 * otherwise
						 */
						if (tagData.isSelected() && setSelectedItemInCategory)
							commonCatDetails.setSelectedTag(tag.getTagId());

						commonCatDetails.getTags().add(tagData);
					}
				}
			}

			/*
			 * This is a three step process: [1] we find the tags assigned to a
			 * product. [2] we find the categories assigned to the tags found in
			 * step 1 [3] we loop over the categories found in step 2, which we
			 * know contain tags assigned to the product, and pull out the tags
			 * that are associated with the product we are looking at.
			 * 
			 * This seems a little redundant, but it is necessary because there
			 * is no direct relationship between a category and a project - a
			 * category is only listed in a project if the tags contained in a
			 * category are assigned to a project.
			 */

			// loop through the projects
			for (final Project project : projectList)
			{
				// create a project
				final UIProjectCategoriesData projectDetails = new UIProjectCategoriesData(project.getProjectName(), project.getProjectDescription(), project.getProjectId());
				selectedTags.getProjectCategories().add(projectDetails);

				/*
				 * Step 1: find the tags assigned to a product
				 */
				final List<Category> projectCategories = new ArrayList<Category>();
				final Set<TagToProject> tags = project.getTagToProjects();
				for (final TagToProject tagToProject : tags)
				{
					final Tag tag = tagToProject.getTag();
					final Set<TagToCategory> categories = tag.getTagToCategories();

					for (final TagToCategory tagToCategory : categories)
					{
						final Category category = tagToCategory.getCategory();
						if (!projectCategories.contains(category))
							projectCategories.add(category);
					}
				}

				/*
				 * Step 2: find the categories assigned to the tags found in
				 * step 1
				 */
				for (final Category category : projectCategories)
				{
					final Integer catID = category.getCategoryId();

					// the key that represents the category
					final UICategoryData catDetails = createUICategoryData(category, project, filter);
					projectDetails.getCategories().add(catDetails);

					// sync category logic states with the filter
					if (filter != null)
					{
						final ArrayList<Integer> categoryStates = filter.hasCategory(category.getCategoryId());

						// override the default "or" state if the filter has
						// saved an "and" state
						if (categoryStates.contains(Constants.CATEGORY_INTERNAL_AND_STATE))
						{
							catDetails.setInternalLogic(Constants.AND_LOGIC);
						}

						// override the default external "and" state if the
						// filter has saved an "o" state
						if (categoryStates.contains(Constants.CATEGORY_EXTERNAL_OR_STATE))
						{
							catDetails.setExternalLogic(Constants.OR_LOGIC);
						}
					}

					/*
					 * Step 3: loop over the categories found in step 2, which
					 * we know contain tags assigned to the product, and pull
					 * out the tags that are associated with the product we are
					 * looking at
					 */
					final Set<TagToCategory> tagsInCategory = category.getTagToCategories();
					for (final TagToCategory tagToCategory : tagsInCategory)
					{
						final Tag tag = tagToCategory.getTag();
						if (tag.isInProject(project))
						{
							final UITagData tagData = createUITagData(tag, catID, filter, checkedTags, tagToCategoryList);

							/*
							 * set the selected id in the category to the last
							 * selected tag this is used by the xhtml page when
							 * a category is marked as mutually exclusive, and
							 * ignored otherwise
							 */
							if (tagData.isSelected() && setSelectedItemInCategory)
								catDetails.setSelectedTag(tag.getTagId());

							catDetails.getTags().add(tagData);
						}
					}
				}
			}

			/* The final step is to order the collections */
			for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
			{
				Collections.sort(project.getCategories());
				for (final UICategoryData category : project.getCategories())
				{
					Collections.sort(category.getTags());
				}

			}

		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	static private UICategoryData createUICategoryData(final Category category, final Project project, final Filter filter)
	{
		final String catName = category.getCategoryName();
		final String catDesc = category.getCategoryDescription();
		final Integer catID = category.getCategoryId();
		final Integer catSort = category.getCategorySort();

		final UICategoryData retValue = new UICategoryData(catName, catDesc, catID, catSort == null ? 0 : catSort, false);

		/* sync the category logic with the filter */

		if (filter != null)
		{
			/*
			 * loop over all the categories, looking for a match with the
			 * project and category used to create the UI data
			 */
			for (final FilterCategory filterCategory : filter.getFilterCategories())
			{
				/*
				 * we have a match if a null project was supplied and a null
				 * project was found or the supplied project matches the filter
				 * project and the categories match.
				 * CollectionUtilities.isEqual() handles equality between null
				 * objects.
				 */

				final boolean projectsMatch = CollectionUtilities.isEqual(project, filterCategory.getProject());
				final boolean categoriesMatch = filterCategory.getCategory().equals(category);

				if (projectsMatch && categoriesMatch)
				{
					if (filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_AND_STATE)
						retValue.setInternalLogic(Constants.AND_LOGIC);
					else if (filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_OR_STATE)
						retValue.setInternalLogic(Constants.OR_LOGIC);
					else if (filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_AND_STATE)
						retValue.setExternalLogic(Constants.AND_LOGIC);
					if (filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_OR_STATE)
						retValue.setExternalLogic(Constants.OR_LOGIC);
				}
			}
		}

		return retValue;
	}

	@SuppressWarnings("javadoc")
	static private UITagData createUITagData(final Tag tag, final Integer catID, final Filter filter, final List<Tag> selectedTags, final List<TagToCategory> tagToCategoryList)
	{
		final Integer tagId = tag.getTagId();
		final String tagName = tag.getTagName();
		final String tagDescription = tag.getTagDescription();
		final String tagChildrenList = tag.getChildrenList();
		final String tagParentList = tag.getParentList();

		/*
		 * find out if this tag is already selected by the topic ...
		 */
		boolean selected = false;
		boolean selectedNot = false;

		if (selectedTags != null)
		{
			selected = selectedTags.contains(tag);
		}
		// ... or by the filter
		else if (filter != null)
		{
			final int tagState = filter.hasTag(tagId);
			if (tagState == Constants.NOT_MATCH_TAG_STATE)
				selected = selectedNot = true;
			else if (tagState == Constants.MATCH_TAG_STATE)
				selected = true;
		}

		// find the sorting order
		Integer sorting = null;
		for (final TagToCategory tagToCategory : tagToCategoryList)
		{
			if (tagToCategory.getCategory().getCategoryId() == catID && tagToCategory.getTag().getTagId() == tagId)
				sorting = tagToCategory.getSorting();
		}

		final UITagData retValue = new UITagData(tagName, tagDescription, tagId, sorting == null ? 0 : sorting, selected, selectedNot, tagParentList, tagChildrenList);
		return retValue;
	}

	@SuppressWarnings("unchecked")
	static public void populateProjectTags(final Project project, final List<UIProjectTagData> tags)
	{
		try
		{
			tags.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Tag> tagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY).getResultList();

			for (final Tag tag : tagList)
			{
				boolean found = false;

				for (final TagToProject tagToProject : project.getTagToProjects())
				{
					if (tagToProject.getProject().equals(project))
					{
						found = true;
						break;
					}
				}

				tags.add(new UIProjectTagData(tag, found));
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	static public void populateTagProjects(final Tag mainTag, final List<UITagProjectData> projects)
	{
		try
		{
			projects.clear();

			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final List<Project> projectList = entityManager.createQuery(Project.SELECT_ALL_QUERY).getResultList();

			for (final Project project : projectList)
			{
				boolean found = false;

				for (final TagToProject tagToProject : mainTag.getTagToProjects())
				{
					if (tagToProject.getProject().equals(project))
					{
						found = true;
						break;
					}
				}

				projects.add(new UITagProjectData(project, found));
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	static public void populateTagTags(final Project project, final UIProjectData selectedTags)
	{
		populateTags(project.getTags(), selectedTags, null, true);
	}

	static public void populateTagTags(final Tag tag, final UIProjectData selectedTags)
	{
		populateTags(tag.getTags(), selectedTags, null, true);
	}

	/**
	 * This function is used to populate the data structures that display the
	 * categories that a tag can and does belong to.
	 * 
	 * @param tag The Tag being displayed
	 * @param categories A collection of data structures representing the
	 *            categories
	 * @param selectedCategories A collection of selected categories
	 * @param tagSortValues A collection of data structures representing the
	 *            tags sorting order within a category
	 */
	@SuppressWarnings("unchecked")
	static public void populateTagCategories(final Tag tag, final List<UICategoryData> categories)
	{
		categories.clear();

		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Category> categoryList = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		final List<TagToCategory> tagToCategoryList = entityManager.createQuery(TagToCategory.SELECT_ALL_QUERY).getResultList();

		// then loop through the categories
		for (final Category category : categoryList)
		{
			final String catName = category.getCategoryName();
			final Integer catID = category.getCategoryId();
			final String catDesc = category.getCategoryDescription();
			final Integer tagId = tag.getTagId();

			// find out if the tag is already in the category
			final boolean selected = tag.isInCategory(category);

			// get the sorting value.
			Integer sorting = null;
			for (final TagToCategory tagToCategory : tagToCategoryList)
			{
				if (tagToCategory.getCategory().getCategoryId() == catID && tagToCategory.getTag().getTagId() == tagId)
				{
					sorting = tagToCategory.getSorting();
					break;
				}
			}

			// in this case the sort value in the TopicTagCategoryData
			// represents the tags sorting position within
			// the category, not the category's sorting position amongst other
			// categories
			categories.add(new UICategoryData(catName, catDesc, catID, sorting == null ? 0 : sorting, selected, false, false));
		}

		// sort the categories by name
		Collections.sort(categories, new TopicTagCategoryDataNameSorter());
	}

	/**
	 * Used to add the current user roles into Drools working memory. This
	 * function has been copied from
	 * RuleBasedPermissionResolver.synchronizeContext()
	 */
	@SuppressWarnings("unchecked")
	public static void injectSecurity(WorkingMemory workingMemory, Identity identity)
	{
		for (final Group sg : identity.getSubject().getPrincipals(Group.class))
		{
			if (Identity.ROLES_GROUP.equals(sg.getName()))
			{
				Enumeration<Principal> e = (Enumeration<Principal>) sg.members();
				while (e.hasMoreElements())
				{
					Principal role = e.nextElement();

					boolean found = false;
					Iterator<Role> iter = (Iterator<Role>) workingMemory.iterateObjects(new ClassObjectFilter(Role.class));
					while (iter.hasNext())
					{
						Role r = iter.next();
						if (r.getName().equals(role.getName()))
						{
							found = true;
							break;
						}
					}

					if (!found)
					{
						workingMemory.insert(new Role(role.getName()));
					}

				}
			}
		}
	}

	/**
	 * This function takes the url parameters and uses them to populate a Filter
	 * object
	 * 
	 * @param filterHome The object that is used to manage the Filter object
	 * @param context This is used to access the url parameters
	 * @param filterName The name of the url parameter that defines a Filter ID
	 * @param tagPrefix The prefix assigned to url parameters that identify tag
	 *            filters
	 * @param categoryInternalPrefix The prefix assigned to url parameters that
	 *            identify category internal logic settings
	 * @param categoryExternalPrefix The prefix assigned to url parameters that
	 *            identify category external logic settings
	 */
	public static Filter populateFilter(final FacesContext context, final String filterName, final String tagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// get the tags from the url, and categorize them
		final Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();

		// attempt to get the filter id from the url
		Integer filterId = null;
		if (paramMap.containsKey(filterName))
		{
			try
			{
				filterId = Integer.parseInt(paramMap.get(filterName));
			}
			catch (final Exception ex)
			{
				// filter value was not an integer
				filterId = null;

				ExceptionUtilities.handleException(ex);
			}
		}

		Filter filter;

		if (filterId != null)
		{
			filter = entityManager.find(Filter.class, filterId);
		}
		else
		{
			filter = new Filter();

			for (final String key : paramMap.keySet())
			{
				try
				{
					final boolean tagVar = key.startsWith(tagPrefix);
					final boolean catIntVar = key.startsWith(categoryInternalPrefix);
					final boolean catExtVar = key.startsWith(categoryExternalPrefix);
					final String state = paramMap.get(key);

					// add the filter category states
					if (catIntVar || catExtVar)
					{
						// get the category and project id data from the
						// variable name
						final String catProjDetails = catIntVar ? key.replaceFirst(categoryInternalPrefix, "") : key.replaceFirst(categoryExternalPrefix, "");
						// split the category and project id out of the data
						final String[] catProjID = catProjDetails.split("-");

						/*
						 * some validity checks. make sure we have one or two
						 * strings after the split.
						 */
						if (catProjID.length != 1 && catProjID.length != 2)
							continue;

						// try to get the category and project ids
						Integer catID = null;
						Integer projID = null;
						try
						{
							catID = Integer.parseInt(catProjID[0]);

							/*
							 * if the array has just one element, we have only
							 * specified the category. in this case the project
							 * is the common project
							 */
							if (catProjID.length == 2)
								projID = Integer.parseInt(catProjID[1]);
						}
						catch (final Exception ex)
						{
							ExceptionUtilities.handleException(ex);
							continue;
						}

						// at this point we have found a url variable that
						// contains a catgeory and project id

						final Category category = entityManager.find(Category.class, catID);
						final Project project = projID != null ? entityManager.find(Project.class, projID) : null;

						Integer dbState;

						if (catIntVar)
						{
							if (state.equals(Constants.AND_LOGIC))
								dbState = Constants.CATEGORY_INTERNAL_AND_STATE;
							else
								dbState = Constants.CATEGORY_INTERNAL_OR_STATE;
						}
						else
						{
							if (state.equals(Constants.AND_LOGIC))
								dbState = Constants.CATEGORY_EXTERNAL_AND_STATE;
							else
								dbState = Constants.CATEGORY_EXTERNAL_OR_STATE;
						}

						final FilterCategory filterCategory = new FilterCategory();
						filterCategory.setFilter(filter);
						filterCategory.setProject(project);
						filterCategory.setCategory(category);
						filterCategory.setCategoryState(dbState);

						filter.getFilterCategories().add(filterCategory);
					}

					// add the filter tag states
					if (tagVar)
					{
						final Integer tagId = Integer.parseInt(key.replaceFirst(tagPrefix, ""));
						final Integer intState = Integer.parseInt(state);

						// get the Tag object that the tag id represents
						final Tag tag = entityManager.getReference(Tag.class, tagId);

						final FilterTag filterTag = new FilterTag();
						filterTag.setTag(tag);
						filterTag.setTagState(intState);
						filterTag.setFilter(filter);
						filter.getFilterTags().add(filterTag);
					}
					else
					{
						if (TopicFilter.getFilterNames().keySet().contains(key))
						{
							final FilterField filterField = new FilterField();
							filterField.setFilter(filter);
							filterField.setField(key);
							filterField.setValue(state);
							filterField.setDescription(TopicFilter.getFilterNames().get(key));
							filter.getFilterFields().add(filterField);
						}
					}
				}
				catch (final NumberFormatException ex)
				{
					// probably a misformed tag request parameter
					ExceptionUtilities.handleException(ex);
				}
				catch (final EntityNotFoundException ex)
				{
					// probably a missing tagid
					ExceptionUtilities.handleException(ex);
				}
			}

		}

		return filter;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> getTextSearchTopicMatch(final String phrase)
	{
		final List<Integer> retValue = new ArrayList<Integer>();

		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			// get the Hibernate session from the EntityManager
			final Session session = (Session) entityManager.getDelegate();
			// get a Hibernate full text session. we use the Hibernate version,
			// instead of the JPA version,
			// because we can use the Hibernate versions to do projections
			final FullTextSession fullTextSession = Search.getFullTextSession(session);
			// create a query parser
			final QueryParser parser = new QueryParser(Version.LUCENE_31, "TopicSearchText", fullTextSession.getSearchFactory().getAnalyzer(Topic.class));
			// parse the query string
			final org.apache.lucene.search.Query query = parser.parse(phrase);

			// build a lucene query
			/*
			 * final org.apache.lucene.search.Query query = qb .keyword()
			 * .onFields("TopicSearchText") .matching(phrase) .createQuery();
			 */

			// build a hibernate query
			final org.hibernate.search.FullTextQuery hibQuery = fullTextSession.createFullTextQuery(query, Topic.class);
			// set the projection to return the id's of any topic's that match
			// the query
			hibQuery.setProjection("topicId");
			// get the results. because we setup a projection, there is no trip
			// to the database
			final List<Object[]> results = hibQuery.list();
			// extract the data into the List<Integer>
			for (final Object[] projection : results)
			{
				final Integer id = (Integer) projection[0];
				retValue.add(id);
			}

			// an empty list will be interpreted as no restriction as opposed to
			// return none. so add a non existent topic id so no matches are
			// made
			if (retValue.size() == 0)
				retValue.add(-1);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}

		return retValue;
	}

	/**
	 * This function is used to create the HQL query where clause that is
	 * appended to the generic EJBQL (as created in default EntityList objects)
	 * select statement. It takes the request parameters to get the tags that
	 * are to be included in the clause, groups them by category, and then take
	 * additional request parameters to define the boolean operations to use
	 * between tags in a category, and between categories.
	 * 
	 * @param tagPrefix Defines the request parameter prefix that defines a tag
	 *            to be matched
	 * @param notTagPrefix Defines the request parameter prefix that defines a
	 *            tag to be not matched
	 * @param categoryInternalPrefix Defines the request parameter prefix that
	 *            defines the boolean operation between tags
	 * @param categoryExternalPrefix Defines the request parameter prefix that
	 *            defines the boolean operation between categories
	 * @param filterDatabase Used to store the tags, grouped by category. Needs
	 *            to be not null.
	 * @return the clause to append to the EJBQL select statement
	 */
	public static String buildQuery(final FacesContext context, final Filter filter)
	{
		// the categories to be ANDed will be added to this string
		String andQueryBlock = "";
		// the categories to be ORed will be added to this string
		String orQueryBlock = "";

		// loop over the projects that the tags in this filter are assigned to
		for (final Project project : filter.getFilterTagProjects())
		{
			// loop over the categories of tags we found
			for (final Category category : filter.getFilterTagCategories())
			{
				// define the default logic used for the FilterTag categories
				String catInternalLogic = Constants.OR_LOGIC;
				String catExternalLogic = Constants.AND_LOGIC;

				/*
				 * now loop over the FilterCategories, looking for any
				 * categories that specify a particular boolean logic to apply.
				 * remember that not all FilterTags will have an associated
				 * FilterCategory that specifies the logic to use, in which case
				 * the default logic defined above will be used
				 */
				final Set<FilterCategory> filterCatgeories = filter.getFilterCategories();
				for (final FilterCategory filterCatgeory : filterCatgeories)
				{
					final boolean categoryMatch = category.equals(filterCatgeory.getCategory());
					/*
					 * project or filterCatgeory.getProject() might be null.
					 * CollectionUtilities.isEqual deals with this situation
					 */
					final boolean projectMatch = CollectionUtilities.isEqual(project, filterCatgeory.getProject());

					if (categoryMatch && projectMatch)
					{
						final int categoryState = filterCatgeory.getCategoryState();

						if (categoryState == Constants.CATEGORY_INTERNAL_AND_STATE)
							catInternalLogic = Constants.AND_LOGIC;
						else if (categoryState == Constants.CATEGORY_INTERNAL_OR_STATE)
							catInternalLogic = Constants.OR_LOGIC;
						else if (categoryState == Constants.CATEGORY_EXTERNAL_AND_STATE)
							catExternalLogic = Constants.AND_LOGIC;
						else if (categoryState == Constants.CATEGORY_EXTERNAL_OR_STATE)
							catExternalLogic = Constants.OR_LOGIC;
					}
				}

				/*
				 * now build up the HQL that checks to see if the FilterTags
				 * exist (or not) in this category
				 */
				String categoryBlock = "";
				
				boolean matchedSomeTags = false;

				final Set<FilterTag> filterTags = filter.getFilterTags();
				for (final FilterTag filterTag : filterTags)
				{
					final Tag tag = filterTag.getTag();

					/*
					 * first check to make sure that the FilterTag is actually
					 * associated with the category we are looking at now
					 */
					if (tag.isInCategory(category) && tag.isInProject(project))
					{
						matchedSomeTags = true;
						
						// get the TagID for convenience
						final String value = tag.getTagId().toString();
						/*
						 * a FilterTag state of 1 "means exists in category",
						 * and 0 means "does not exist in category"
						 */
						final boolean matchTag = filterTag.getTagState() == Constants.MATCH_TAG_STATE;

						if (categoryBlock.length() != 0)
							categoryBlock += " " + catInternalLogic + " ";

						if (matchTag)
						{
							// match the tag in this category
							categoryBlock += "exists (select 1 from TopicToTag topicToTag where topicToTag.topic = topic and topicToTag.tag.tagId = " + value + ") ";
						}
						else
						{
							// make sure this tag does not exist in this
							// category
							categoryBlock += "not exists (select 1 from TopicToTag topicToTag where topicToTag.topic = topic and topicToTag.tag.tagId = " + value + ") ";
						}
					}
				}

				if (matchedSomeTags)
				{
					categoryBlock = "(" + categoryBlock + ")";
	
					// append this clause to the appropriate block
					if (catExternalLogic.equals(Constants.AND_LOGIC))
					{
						if (andQueryBlock.length() != 0)
							andQueryBlock += " " + Constants.AND_LOGIC + " ";
	
						andQueryBlock += categoryBlock;
					}
					else
					{
						if (orQueryBlock.length() != 0)
							orQueryBlock += " " + Constants.OR_LOGIC + " ";
	
						orQueryBlock += categoryBlock;
					}
				}

			}
		}

		String query = "";

		// build up the category query if some conditions were specified
		// if not, we will just return an empty string
		if (andQueryBlock.length() != 0 || orQueryBlock.length() != 0)
		{
			// add the and categories
			if (andQueryBlock.length() != 0)
				query += "(" + andQueryBlock + ")";

			// add the or categories
			if (orQueryBlock.length() != 0)
				query += (query.length() != 0 ? " And " : "") + "(" + orQueryBlock + ")";

			// add the where clause
			// have to join the topic and its collection of tags in order for
			// the filter to work
			if (query.length() != 0)
				query = " where " + query;
		}

		return query;
	}

	/**
	 * This function will add to a query to replace the functionality of the
	 * restrictions used by EntityQuery objects.
	 */
	public static String buildFilterFieldsQuery(final String query, final Filter filter)
	{
		final List<FilterField> logicField = filter(having(on(FilterField.class).getField(), equalTo(Constants.TOPIC_LOGIC_FILTER_VAR)), filter.getFilterFields());

		String logic = Constants.TOPIC_LOGIC_FILTER_VAR_DEFAULT_VALUE;
		if (logicField.size() == 1)
			logic = logicField.get(0).getValue();

		String myQuery = "";

		for (final FilterField filterField : filter.getFilterFields())
		{
			if (!filterField.getField().equals(Constants.TOPIC_LOGIC_FILTER_VAR))
			{
				final String fieldName = filterField.getField();
				final String fieldValue = filterField.getValue();

				if (myQuery.length() != 0)
					myQuery += logic + " ";

				if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR) || fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
				{
					try
					{
						// this will throw an exception if the format of the
						// field in the database is incorrect
						ISODateTimeFormat.dateTime().parseDateTime(filterField.getValue());

						// if we get to this line, it is safe to say we have a
						// properly formatted date

						if (filterField.getField().equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
						{
							myQuery += "topic.topicTimeStamp >= '" + fieldValue + "'";
						}
						else if (filterField.getField().equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
						{
							myQuery += "topic.topicTimeStamp <= '" + fieldValue + "'";
						}
					}
					catch (final Exception ex)
					{
						ExceptionUtilities.handleException(ex);
					}
				}
				else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
				{
					int minimum = 0;
					if (fieldValue.equalsIgnoreCase("true"))
						minimum = 1;

					myQuery += "topic.parentTopicToTopics.size >= " + minimum;
				}
				else if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
				{
					myQuery += "topic.topicId in (" + fieldValue + ")";
				}
				else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
				{
					try
					{
						final Integer topicId = Integer.parseInt(fieldValue);
						myQuery += "topic.topicId in (" + getRelatedTopicIDsString(topicId) + ")";
					}
					catch (final Exception ex)
					{
						// failed to parse integer
						ExceptionUtilities.handleException(ex);
					}
				}
				else
				{
					myQuery += "'" + fieldName.toLowerCase() + "' like '%" + fieldValue.toLowerCase() + "%')";
				}
			}
		}

		if (myQuery.length() != 0)
		{
			if (query.length() != 0)
				myQuery = " and (" + myQuery + ")";
			else
				myQuery = " where (" + myQuery + ")";
		}

		return query + myQuery;
	}

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, ArrayList<Integer>> populateExclusionTags()
	{
		final HashMap<Integer, ArrayList<Integer>> retValue = new HashMap<Integer, ArrayList<Integer>>();
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		List<Tag> tagList = entityManager.createQuery(Tag.SELECT_ALL_QUERY).getResultList();
		for (final Tag tag : tagList)
			retValue.put(tag.getTagId(), tag.getExclusionTagIDs());
		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static void populateMutuallyExclusiveCategories(final UIProjectData guiData)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<Category> categoryList = entityManager.createQuery(Category.SELECT_ALL_QUERY).getResultList();
		for (final Category category : categoryList)
		{
			for (final UIProjectCategoriesData project : guiData.getProjectCategories())
			{
				for (final UICategoryData guiInputData : project.getCategories())
				{
					if (guiInputData.getId().equals(category.getCategoryId()) && category.isMutuallyExclusive())
					{
						guiInputData.setMutuallyExclusive(true);
						break;
					}
				}
			}
		}
	}

	/**
	 * A utility function to allow us to build a single string with line breaks
	 * from an array of strings. This is really just used to make defining text
	 * files in code easier to read, as opposed to having to add and maintain
	 * line breaks in the initial string definition.
	 */
	public static String buildString(final String[] lines)
	{
		return buildString(lines, "\n");
	}

	public static String buildString(final String[] lines, final String seperator)
	{
		String retValue = "";
		for (final String line : lines)
		{
			if (retValue.length() != 0)
				retValue += seperator;
			retValue += line;
		}

		return retValue;
	}

	/**
	 * Clean up generic text so it can be included in an XML tag
	 */
	public static String cleanTextForXML(final String input)
	{
		if (input == null)
			return "";

		return Jsoup.parse(input).text().replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;").replaceAll("", "");
	}

	public static String cleanTextForCSV(final String input)
	{
		if (input == null)
			return "";

		return input.replaceAll(",", "").replace("\r\n", " ").replaceAll("\n", " ").replaceAll("", "");
	}

	/**
	 * This function is used to get a list of the currently selected tags. This
	 * is used to prepopulate the selected tags when creating a new topic
	 */
	public static String urlTagParameters(final FacesContext context)
	{
		final Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();

		String variables = "";

		for (final String key : paramMap.keySet())
		{
			if (key.startsWith(Constants.MATCH_TAG))
			{
				if (variables.length() == 0)
					variables += "?";
				else
					variables += "&";

				variables += key + "=" + paramMap.get(key);
			}
		}

		return variables;
	}

	/**
	 * A utility function that is used to append url parameters to a collection
	 * of url parameters
	 * 
	 * @param params The existing url parameter string
	 * @param name The parameter name
	 * @param value The parameter value
	 * @return The url parameters that were passed in via params, with the new
	 *         parameter appended to it
	 */
	public static String addParameter(final String params, final String name, final String value)
	{
		// Just use an empty string as the default value
		return addParameter(params, name, value, "");
	}

	/**
	 * A utility function that is used to append url parameters to a collection
	 * of url parameters
	 * 
	 * @param params The existing url parameter string
	 * @param name The parameter name
	 * @param value The parameter value
	 * @param defaultValue Used in place of value if it is null
	 * @return The url parameters that were passed in via params, with the new
	 *         parameter appended to it
	 */
	public static String addParameter(final String params, final String name, final String value, final String defaultValue)
	{
		String newParams = params;

		if (newParams.length() == 0)
			newParams += "?";
		else
			newParams += "&";

		newParams += name + "=" + (value == null ? defaultValue : value);

		return newParams;
	}

	public static void syncFilterWithTags(final Filter filter, final UIProjectData selectedTags)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// loop through the list of tags selected via the checkboxes, and update
		// their
		// representation in the Filter
		for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
		{
			for (final UICategoryData category : project.getCategories())
			{
				for (final UITagData tag : category.getTags())
				{
					final boolean tagSelected = tag.isSelected();
					final boolean notTagSelected = tag.isNotSelected();
					final int state = notTagSelected ? Constants.NOT_MATCH_TAG_STATE : Constants.MATCH_TAG_STATE;

					if (tagSelected)
					{
						boolean found = false;
						for (final FilterTag filterTag : filter.getFilterTags())
						{
							if (filterTag.getTag().getTagId().equals(tag.getId()))
							{
								System.out.println("Updating tag state");

								filterTag.setTagState(state);
								found = true;
								break;
							}
						}

						if (!found)
						{
							System.out.println("Adding new FilterTag");

							final Tag dbTag = entityManager.getReference(Tag.class, tag.getId());

							final FilterTag filterTag = new FilterTag();
							filterTag.setFilter(filter);
							filterTag.setTag(dbTag);
							filterTag.setTagState(state);

							filter.getFilterTags().add(filterTag);
						}
					}
				}
			}
		}

		/*
		 * now loop through the tags in the Filter, and remove any that are no
		 * longer checked in the gui
		 */

		final ArrayList<FilterTag> removeTags = new ArrayList<FilterTag>();

		for (final FilterTag filterTag : filter.getFilterTags())
		{
			boolean found = false;
			for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
			{
				for (final UICategoryData category : project.getCategories())
				{
					for (final UITagData tag : category.getTags())
					{
						final boolean tagSelected = tag.isSelected();
						if (!tagSelected && tag.getId().equals(filterTag.getTag().getTagId()))
						{
							found = true;
							break;
						}
					}
				}
			}

			// add to a temporary container so we don't modify the collection we
			// are looping over
			if (found)
				removeTags.add(filterTag);
		}

		// now clean out the obsolete tags
		for (final FilterTag filterTag : removeTags)
			filter.getFilterTags().remove(filterTag);
	}

	public static void syncFilterWithCategories(final Filter filter, final UIProjectData selectedTags)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
		{
			final Integer projKey = project.getId();

			for (final UICategoryData category : project.getCategories())
			{
				final Integer catKey = category.getId();

				/*
				 * match the radioboxes with the internal db flag used to
				 * indicate logic
				 */
				int dbIntCatLogic;
				int dbExtCatLogic;

				final String intCatLogic = category.getInternalLogic();
				final String extCatLogic = category.getExternalLogic();

				if (intCatLogic.equals(Constants.AND_LOGIC))
					dbIntCatLogic = Constants.CATEGORY_INTERNAL_AND_STATE;
				else
					dbIntCatLogic = Constants.CATEGORY_INTERNAL_OR_STATE;

				if (extCatLogic.equals(Constants.AND_LOGIC))
					dbExtCatLogic = Constants.CATEGORY_EXTERNAL_AND_STATE;
				else
					dbExtCatLogic = Constants.CATEGORY_EXTERNAL_OR_STATE;

				/*
				 * set the existing filter category states to the ones selected
				 * in the gui
				 */
				boolean foundIntCat = false;
				boolean foundExtCat = false;
				final Set<FilterCategory> filterCategories = filter.getFilterCategories();
				for (final FilterCategory filterCategory : filterCategories)
				{
					final int filterCategoryID = filterCategory.getCategory().getCategoryId();

					/*
					 * The UIProjectCategoriesData class uses
					 * Constants.COMMON_PROJECT_ID to denote the common project.
					 * The FilterCategory uses a null project to denote the
					 * common project. We handle this special case here.
					 */
					final Project filterProject = filterCategory.getProject();
					final int filterProjectID = filterProject == null ? Constants.COMMON_PROJECT_ID : filterCategory.getProject().getProjectId();

					if (filterCategoryID == catKey && filterProjectID == projKey)
					{
						/*
						 * we are looking at the internal logic filter, and it
						 * does not match what the user just selected
						 */
						if ((filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_AND_STATE || filterCategory.getCategoryState() == Constants.CATEGORY_INTERNAL_OR_STATE))
						{
							if (filterCategory.getCategoryState() != dbIntCatLogic)
								filterCategory.setCategoryState(dbIntCatLogic);
							foundIntCat = true;
						}

						/*
						 * we are looking at the external logic filter, and it
						 * does not match what the user just selected
						 */
						if ((filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_AND_STATE || filterCategory.getCategoryState() == Constants.CATEGORY_EXTERNAL_OR_STATE))
						{
							if (filterCategory.getCategoryState() != dbExtCatLogic)
								filterCategory.setCategoryState(dbExtCatLogic);
							foundExtCat = true;
						}
					}

					// if we find both the internal and external filter
					// categories,
					// we can exit the loop
					if (foundIntCat && foundExtCat)
						break;
				}

				// create the missing filter categories
				if (!foundIntCat || !foundExtCat)
				{
					final Category categoryEntity = entityManager.find(Category.class, catKey);
					final Project projectEntity = entityManager.find(Project.class, projKey);

					if (!foundIntCat)
						createFilterCategory(filter, projectEntity, categoryEntity, dbIntCatLogic);
					if (!foundExtCat)
						createFilterCategory(filter, projectEntity, categoryEntity, dbExtCatLogic);
				}
			}
		}
	}

	public static void syncFilterWithFieldUIElements(final Filter filter, final TopicFilter topic)
	{
		for (final String fieldName : TopicFilter.getFilterNames().keySet())
			syncFilterField(filter, fieldName, topic.getFieldValue(fieldName), TopicFilter.getFilterNames().get(fieldName));
	}

	protected static void syncFilterField(final Filter filter, final String fieldName, final String fieldValue, final String fieldDescription)
	{
		// get the database filterfield object that matches the fieldName
		final List<FilterField> filterField = filter(having(on(FilterField.class).getField(), equalTo(fieldName)), filter.getFilterFields());

		// if fieldValue is set to a value, we need to modify or add a
		// FilterField entity
		if (fieldValue != null && fieldValue.trim().length() != 0)
		{
			final String fixedFieldValue = fieldValue.trim();

			FilterField newField = null;

			// add a new FilterField entity
			if (filterField.size() == 0)
			{
				newField = new FilterField();
				newField.setFilter(filter);
				newField.setField(fieldName);
				newField.setValue(fixedFieldValue);
				newField.setDescription(fieldDescription);
				filter.getFilterFields().add(newField);
			}
			// update a FilterField entity
			else if (filterField.size() == 1)
			{
				newField = filterField.get(0);
				newField.setValue(fixedFieldValue);
				newField.setDescription(fieldDescription);
			}
		}
		else
		{
			// remove the FilterField entity
			if (filterField.size() == 1)
				filter.getFilterFields().remove(filterField.get(0));
		}
	}

	/**
	 * A utility function that persists a FilterCategory object to the database
	 */
	protected static void createFilterCategory(final Filter filter, final Project project, final Category category, final Integer state)
	{
		final FilterCategory filterCategory = new FilterCategory();
		filterCategory.setCategoryState(state);
		filterCategory.setFilter(filter);
		filterCategory.setProject(project);
		filterCategory.setCategory(category);
		filter.getFilterCategories().add(filterCategory);
	}

	public static String cleanStringForJavaScriptVariableName(final String input)
	{
		return input.replaceAll("[^a-zA-Z]", "");
	}

	public static List<Integer> getRelatedTopicIDs(final Integer topicRelatedTo)
	{
		if (topicRelatedTo != null)
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, topicRelatedTo);
			return topic.getRelatedTopicIDs();
		}

		return null;
	}

	public static List<Integer> getIncomingRelatedTopicIDs(final Integer topicRelatedFrom)
	{
		if (topicRelatedFrom != null)
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Topic topic = entityManager.find(Topic.class, topicRelatedFrom);
			return topic.getIncomingRelatedTopicIDs();
		}

		return null;
	}

	public static String getRelatedTopicIDsString(final Integer topicRelatedTo)
	{
		final List<Integer> ids = getRelatedTopicIDs(topicRelatedTo);
		if (ids != null)
		{
			String retValue = "";
			for (final Integer id : ids)
			{
				if (retValue.length() != 0)
					retValue += ",";
				retValue += id.toString();
			}
			return retValue;
		}
		return "";
	}

	public static String buildEditNewTopicUrl(final UIProjectData selectedTags)
	{
		String tags = "";
		for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				if (cat.isMutuallyExclusive())
				{
					if (cat.getSelectedTag() != null)
					{
						if (tags.length() != 0)
							tags += "&";
						tags += "tag" + cat.getSelectedTag() + "=1";
					}

				}
				else
				{
					for (final UITagData tag : cat.getTags())
					{
						if (tag.isSelected())
						{
							if (tags.length() != 0)
								tags += "&";
							tags += "tag" + tag.getId() + "=1";
						}
					}
				}
			}
		}
		final String retValue = "/TopicEdit.seam?" + tags;
		return retValue;
	}

	@SuppressWarnings("unchecked")
	public static List<Topic> getTopicsFromFilter(final Filter filter)
	{
		final FacesContext facesContext = FacesContext.getCurrentInstance();
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// build the query that will be used to get the topics
		String query = EntityUtilities.buildQuery(facesContext, filter);
		query = EntityUtilities.buildFilterFieldsQuery(query, filter);

		// get the base topic list
		final List<Topic> topicList = entityManager.createQuery(Topic.SELECT_ALL_QUERY + query).getResultList();

		return topicList;
	}
}
