package com.redhat.topicindex.utils.structures.tags;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToTag;
import com.redhat.topicindex.utils.EntityUtilities;

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
	
	public List<Tag> getSelectedTags()
	{
		final List<Tag> selectedTagObjects = new ArrayList<Tag>();

		for (final UIProjectCategoriesData project : this.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				// find the selected tags
				for (final UITagData tagId : cat.getTags())
				{
					// if tag is selected
					if (tagId.isSelected())
						selectedTagObjects.add(EntityUtilities.getTagFromId(tagId.getId()));
				}
			}
		}
		
		return selectedTagObjects;
	}
	
	public List<Pair<Tag, UITagData>> getExtendedSelectedTags()
	{
		final List<Pair<Tag, UITagData>> selectedTagObjects = new ArrayList<Pair<Tag, UITagData>>();

		for (final UIProjectCategoriesData project : this.getProjectCategories())
		{
			for (final UICategoryData cat : project.getCategories())
			{
				// find the selected tags
				for (final UITagData tagId : cat.getTags())
				{
					// if tag is selected
					if (tagId.isSelected())
						selectedTagObjects.add(Pair.newPair(EntityUtilities.getTagFromId(tagId.getId()), tagId));
				}
			}
		}
		
		return selectedTagObjects;
	}
	
	public List<Pair<Tag, UITagData>> getExtendedAddedTags(final List<Tag> existingTags)
	{
		final List<Pair<Tag, UITagData>> selectedTags = getExtendedSelectedTags();
		
		// now make a note of the additions
		final List<Pair<Tag, UITagData>> addTags = new ArrayList<Pair<Tag, UITagData>>();
		for (final Pair<Tag, UITagData> selectedTag : selectedTags)
		{
			if (!existingTags.contains(existingTags))
			{
				addTags.add(selectedTag);
			}
		}
		
		return addTags;
	}
	
	public List<Tag> getAddedTags(final List<Tag> existingTags)
	{
		final List<Tag> selectedTags = getSelectedTags();
		
		// now make a note of the additions
		final ArrayList<Tag> addTags = new ArrayList<Tag>();
		for (final Tag selectedTag : selectedTags)
		{
			if (!existingTags.contains(existingTags))
			{
				addTags.add(selectedTag);
			}
		}
		
		return addTags;
	}
	
	public List<Tag> getRemovedTags(final List<Tag> existingTags)
	{
		final List<Tag> selectedTags = getSelectedTags();
		
		// make a note of the tags that were removed
		final List<Tag> removeTags = new ArrayList<Tag>();
		for (final Tag existingTag : existingTags)
		{
			if (!selectedTags.contains(existingTag))
			{
				// add to external collection to avoid modifying a
				// collection while looping over it
				removeTags.add(existingTag);
			}
		}
		
		return removeTags;
	}
}
