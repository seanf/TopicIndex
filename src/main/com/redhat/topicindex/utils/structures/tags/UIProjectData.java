package com.redhat.topicindex.utils.structures.tags;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the collection of projects that form the top level of
 * the tagging structure, as used by the GUI. These classes differ from the
 * underlying entities in their relationships (e.g. the project and category database
 * entities have no relationship, while the UI tag display does have a direct
 * relationship between projects and categories), and include extra data such as
 * whether a tag/category has been selected or not.
 */
public class UIProjectData
{
	/**
	 * A collection of UIProjectCategoriesData objects, which represent the
	 * categories that hold tags assigned to a project. A PriorityQueue is used
	 * to get automatic sorting.
	 */
	private List<UIProjectCategoriesData> projectCategories = new ArrayList<UIProjectCategoriesData>();

	public List<UIProjectCategoriesData> getProjectCategories()
	{
		return projectCategories;
	}

	public void setProjectCategories(final List<UIProjectCategoriesData> categories)
	{
		this.projectCategories = categories;
	}

	public void clear()
	{
		projectCategories.clear();
	}
}
