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
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;
import org.drools.ClassObjectFilter;
import org.drools.WorkingMemory;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.jsoup.Jsoup;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.BlobConstants;
import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterCategory;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterTag;
import com.redhat.topicindex.entity.IntegerConstants;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.StringConstants;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToProject;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.User;
import com.redhat.topicindex.filter.TopicFilter;
import com.redhat.topicindex.sort.RoleNameComparator;
import com.redhat.topicindex.sort.TopicTagCategoryDataNameSorter;
import com.redhat.topicindex.sort.UserNameComparator;
import com.redhat.topicindex.utils.structures.roles.UIRoleUserData;
import com.redhat.topicindex.utils.structures.tags.UIProjectTagData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;

public class EntityUtilities
{
	public static byte[] loadBlobConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		final BlobConstants constant = entityManager.find(BlobConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the BlobConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
	}

	public static Integer loadIntegerConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		
		final IntegerConstants constant = entityManager.find(IntegerConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the IntegerConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
	}

	public static String loadStringConstant(final EntityManager entityManager, final Integer id)
	{
		if (id == null)
			return null;

		final StringConstants constant = entityManager.find(StringConstants.class, id);

		if (constant == null)
		{
			System.out.println("Expected to find a record in the StringConstants table with an ID of " + id);
			return null;
		}

		return constant.getConstantValue();
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

	static public void populateProjectTags(final Project project, final UIProjectData selectedTags)
	{
		selectedTags.populateTags(project.getTags(), null, true);
	}

	static public void populateTagTags(final Tag tag, final UIProjectData selectedTags)
	{
		selectedTags.populateTags(tag.getTags(), null, true);
	}

	static public void populateTagTags(final Category category, final UIProjectData selectedTags)
	{
		selectedTags.populateTags(category.getTags(), null, true);
	}

	/**
	 * When assigning tags to a category, we need to know the sorting order of
	 * the tags as it related to a specific category. This is different to the
	 * sorting order used to show the tags, because those values are specific to
	 * the categories that the tags appear in.
	 */
	static public void populateTagTagsSortingForCategory(final Category category, final UIProjectData selectedTags)
	{
		for (final UIProjectCategoriesData projectData : selectedTags.getProjectCategories())
		{
			for (final UICategoryData categoryData : projectData.getCategories())
			{
				if (categoryData.getId().equals(category.getCategoryId()))
				{
					for (final UITagData tagData : categoryData.getTags())
					{
						/*
						 * match the sorting order for the tags in the category
						 * with the newSort values for the UI tags
						 */

						for (final TagToCategory tagToCategory : category.getTagToCategories())
						{
							if (tagData.getId().equals(tagToCategory.getTag().getTagId()))
							{
								tagData.setNewSort(tagToCategory.getSorting());
								break;
							}
						}
					}
				}
			}
		}
	}

	public static Tag getTagFromId(final Integer tagId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.find(Tag.class, tagId);
		return tag;
	}
	
	public static com.redhat.topicindex.entity.Role getRoleFromId(final Integer roleId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final com.redhat.topicindex.entity.Role role = entityManager.find(com.redhat.topicindex.entity.Role.class, roleId);
		return role;
	}
	
	public static User getUserFromId(final Integer userId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final User user = entityManager.find(User.class, userId);
		return user;
	}


	static public List<UIRoleUserData> getUserRoles(final User user)
	{
		final List<UIRoleUserData> retValue = new ArrayList<UIRoleUserData>();
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<com.redhat.topicindex.entity.Role> roleList = entityManager.createQuery(com.redhat.topicindex.entity.Role.SELECT_ALL_QUERY).getResultList();
		Collections.sort(roleList, new RoleNameComparator());
		
		for (final com.redhat.topicindex.entity.Role role : roleList)
		{
			final boolean selected = user.isInRole(role);
			final UIRoleUserData roleUserData = new UIRoleUserData(role.getRoleId(), role.getRoleName(), selected);
			retValue.add(roleUserData);			
		}
		
		return retValue;
	}
	
	static public List<UIRoleUserData> getRoleUsers(final com.redhat.topicindex.entity.Role role)
	{
		final List<UIRoleUserData> retValue = new ArrayList<UIRoleUserData>();
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final List<User> userList = entityManager.createQuery(User.SELECT_ALL_QUERY).getResultList();
		Collections.sort(userList, new UserNameComparator());
		
		for (final User user : userList)
		{
			final boolean selected = role.hasUser(user);
			final UIRoleUserData roleUserData = new UIRoleUserData(user.getUserId(), user.getUserName(), selected);
			retValue.add(roleUserData);			
		}
		
		return retValue;
	}
	
	/**
	 * This function is used to populate the data structures that display the
	 * categories that a tag can and does belong to.
	 * 
	 * @param tag
	 *            The Tag being displayed
	 * @param categories
	 *            A collection of data structures representing the categories
	 * @param selectedCategories
	 *            A collection of selected categories
	 * @param tagSortValues
	 *            A collection of data structures representing the tags sorting
	 *            order within a category
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

	public static Filter populateFilter(final MultivaluedMap<String, String> paramMap, final String filterName, final String tagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix)
	{
		final Map<String, String> newParamMap = new HashMap<String, String>();
		for (final String key : paramMap.keySet())
			newParamMap.put(key, paramMap.getFirst(key));
		return populateFilter(newParamMap, filterName, tagPrefix, categoryInternalPrefix, categoryExternalPrefix);
		
	}
	
	/**
	 * This function takes the url parameters and uses them to populate a Filter
	 * object
	 */
	public static Filter populateFilter(final Map<String, String> paramMap, final String filterName, final String tagPrefix, final String categoryInternalPrefix, final String categoryExternalPrefix)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

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
	public static <E> List<Integer> getEditedEntities(final Class<E> type, final String pkColumnName, final DateTime startDate, final DateTime endDate)
	{
		if (startDate == null && endDate == null)
			return null;
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final AuditQuery query = reader.createQuery()
			.forRevisionsOfEntity(type, true, false)
			.addOrder(AuditEntity.revisionProperty("timestamp").asc())			
			.addProjection(AuditEntity.property("originalId." + pkColumnName).distinct());
		
		if (startDate != null)
			query.add(AuditEntity.revisionProperty("timestamp").ge(startDate.toDate().getTime()));
		
		if (endDate != null)
			query.add(AuditEntity.revisionProperty("timestamp").le(endDate.toDate().getTime()));
		
		final List<Integer> entityyIds = query.getResultList();
		
		return entityyIds;
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
	 * @param params
	 *            The existing url parameter string
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
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
	 * @param params
	 *            The existing url parameter string
	 * @param name
	 *            The parameter name
	 * @param value
	 *            The parameter value
	 * @param defaultValue
	 *            Used in place of value if it is null
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
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");

		// build the query that will be used to get the topics
		String query = filter.buildQuery();
		query = EntityUtilities.buildFilterFieldsQuery(query, filter);

		// get the base topic list
		final List<Topic> topicList = entityManager.createQuery(Topic.SELECT_ALL_QUERY + query).getResultList();

		return topicList;
	}
}
