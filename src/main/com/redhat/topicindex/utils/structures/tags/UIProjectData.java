package com.redhat.topicindex.utils.structures.tags;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.CollectionUtilities;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.TagToTag;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class represents the collection of projects that form the top level of
 * the tagging structure, as used by the GUI. These classes differ from the
 * underlying entities in their relationships (e.g. the project and category
 * database entities have no relationship, while the UI tag display does have a
 * direct relationship between projects and categories), and include extra data
 * such as whether a tag/category has been selected or not.
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

	/**
	 * @return A collection of Tags that were selected in the UI
	 */
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

	/**
	 * @return A collection of Tags that were selected in the UI, paired with a
	 *         UITagData object
	 */
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

	/**
	 * @param existingTagsExtended
	 *            A collection of the existing TagToCategory objects held by a
	 *            Category
	 * @return A collection of selected Tags paired with the UITagData (that
	 *         includes additional information such as the sorting order) that
	 *         either do not exist in the existingTagsExtended collection, or
	 *         exist in the existingTagsExtended collection with a different
	 *         sorting order
	 */
	public List<Pair<Tag, UITagData>> getAddedOrModifiedTags(final Set<TagToCategory> existingTagsExtended)
	{
		final List<Pair<Tag, UITagData>> selectedTags = getExtendedSelectedTags();

		// now make a note of the additions
		final List<Pair<Tag, UITagData>> addTags = new ArrayList<Pair<Tag, UITagData>>();
		for (final Pair<Tag, UITagData> selectedTagData : selectedTags)
		{
			final Tag selectedTag = selectedTagData.getFirst();
			final Integer sorting = selectedTagData.getSecond().getNewSort();

			/*
			 * Loop over the TagToCategory collection, and see if the tag we
			 * have selected exists with the same sorting order
			 */
			boolean found = false;
			for (final TagToCategory tagToCategory : existingTagsExtended)
			{
				if (tagToCategory.getTag().equals(selectedTag) && CollectionUtilities.isEqual(tagToCategory.getSorting(), sorting))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				addTags.add(selectedTagData);
			}
		}

		return addTags;
	}

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were selected in the UI and do
	 *         not exist in the existingTags collection, paired with a UITagData
	 *         object that contains additional information such as sorting oder
	 */
	public List<Pair<Tag, UITagData>> getExtendedAddedTags(final List<Tag> existingTags)
	{
		final List<Pair<Tag, UITagData>> selectedTags = getExtendedSelectedTags();

		// now make a note of the additions
		final List<Pair<Tag, UITagData>> addTags = new ArrayList<Pair<Tag, UITagData>>();
		for (final Pair<Tag, UITagData> selectedTag : selectedTags)
		{
			if (!existingTags.contains(selectedTag))
			{
				addTags.add(selectedTag);
			}
		}

		return addTags;
	}

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were selected in the UI and do
	 *         not exist in the existingTags collection
	 */
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

	/**
	 * @param existingTags
	 *            A collection of the existing tags
	 * @return A collection of the tags that are were not selected in the UI and
	 *         exist in the existingTags collection
	 */
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
