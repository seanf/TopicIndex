package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

@Name("tagHome")
public class TagHome extends VersionedEntityHome<Tag>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -7321335882716157007L;
	private UIProjectData selectedTags;

	public void setTagTagId(final Integer id)
	{
		setId(id);
	}

	public Integer getTagTagId()
	{
		return (Integer) getId();
	}

	@Override
	protected Tag createInstance()
	{
		Tag tag = new Tag();
		return tag;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
	}

	public boolean isWired()
	{
		return true;
	}

	public Tag getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	/*********************************************************************/

	private List<UICategoryData> categories = new ArrayList<UICategoryData>();
	private List<UITagProjectData> projects = new ArrayList<UITagProjectData>();

	public List<UICategoryData> getCategories()
	{
		return categories;
	}

	public void setCategories(final List<UICategoryData> value)
	{
		categories = value;
	}

	public void populate()
	{
		EntityUtilities.populateTagCategories(this.getInstance(), categories);

		selectedTags = new UIProjectData();
		EntityUtilities.populateTagTags(this.getInstance(), selectedTags);
		EntityUtilities.populateTagProjects(this.getInstance(), projects);
	}

	public String update()
	{
		updateCategories();
		updateTags();
		updateProjects();
		return super.update();
	}

	public String persist()
	{
		updateCategories();
		updateTags();
		updateProjects();
		return super.persist();
	}

	private Tag getTagFromId(final Integer tagId)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tag = entityManager.find(Tag.class, tagId);
		return tag;
	}

	private void updateProjects()
	{
		final Tag tag = this.getInstance();

		if (tag != null)
		{
			final ArrayList<Project> selectedProjects = new ArrayList<Project>();

			for (final UITagProjectData project : projects)
			{
				if (project.isSelected())
					selectedProjects.add(project.getProject());
			}

			// match up selected tags with existing tags
			final Set<TagToProject> tagToProjects = tag.getTagToProjects();

			// make a note of the tags that were removed
			final ArrayList<Project> removeProjects = new ArrayList<Project>();
			for (final TagToProject tagToProject : tagToProjects)
			{
				final Project existingProject = tagToProject.getProject();

				if (!selectedProjects.contains(existingProject))
				{
					// add to external collection to avoid modifying a
					// collection while looping over it
					removeProjects.add(existingProject);
				}
			}

			// now make a note of the additions
			final ArrayList<Project> addProjects = new ArrayList<Project>();
			for (final Project selectedProject : selectedProjects)
			{
				if (filter(having(on(TagToProject.class).getProject(), equalTo(selectedProject)), tagToProjects).size() == 0)
				{
					addProjects.add(selectedProject);
				}
			}

			// only proceed if there are some changes to make
			if (removeProjects.size() != 0 || addProjects.size() != 0)
			{
				// remove the tags
				for (final Project removeProject : removeProjects)
				{
					tag.removeProjectRelationship(removeProject);
				}

				for (final Project addProject : addProjects)
				{
					tag.addProjectRelationship(addProject);
				}
			}
		}
	}

	private void updateTags()
	{
		final Tag tag = this.getInstance();

		if (tag != null)
		{
			final ArrayList<Tag> selectedTagObjects = new ArrayList<Tag>();

			for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
			{
				for (final UICategoryData cat : project.getCategories())
				{
					// find the selected tags
					for (final UITagData tagId : cat.getTags())
					{
						// if tag is selected
						if (tagId.isSelected())
							selectedTagObjects.add(getTagFromId(tagId.getId()));
					}
				}
			}

			// match up selected tags with existing tags
			final Set<TagToTag> tagToTags = tag.getChildrenTagToTags();

			// make a note of the tags that were removed
			final ArrayList<Tag> removeTags = new ArrayList<Tag>();
			for (final TagToTag tagToTag : tagToTags)
			{
				final Tag existingTag = tagToTag.getSecondaryTag();

				if (!selectedTagObjects.contains(existingTag))
				{
					// add to external collection to avoid modifying a
					// collection while looping over it
					removeTags.add(existingTag);
				}
			}

			// now make a note of the additions
			final ArrayList<Tag> addTags = new ArrayList<Tag>();
			for (final Tag selectedTag : selectedTagObjects)
			{
				if (filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(selectedTag)), tagToTags).size() == 0)
				{
					addTags.add(selectedTag);
				}
			}

			// only proceed if there are some changes to make
			if (removeTags.size() != 0 || addTags.size() != 0)
			{
				// remove the tags
				for (final Tag removeTag : removeTags)
				{
					tag.removeTagRelationship(removeTag);
				}

				for (final Tag addTag : addTags)
				{
					tag.addTagRelationship(addTag);
				}
			}
		}
	}

	private void updateCategories()
	{
		try
		{
			final Tag tag = this.getInstance();

			final ArrayList<TagToCategory> removeCategoryies = new ArrayList<TagToCategory>();

			// find categories that we need to add
			for (final UICategoryData category : categories)
			{
				final Integer categoryId = category.getId();
				final Integer sortValue = category.getSort();
				final TagToCategory existingTagToCategory = tag.getCategory(categoryId);

				// if the mapping does not already exist, create it
				if (category.isSelected() && existingTagToCategory == null)
				{
					final CategoryHome categoryHome = new CategoryHome();
					categoryHome.setId(categoryId);
					final Category existingCategory = categoryHome.getInstance();

					final TagToCategory tagToCategory = new TagToCategory(tag, existingCategory);
					tagToCategory.setSorting(sortValue);

					tag.getTagToCategories().add(tagToCategory);
				}
				// if the mapping does exist, update it
				else if (category.isSelected() && existingTagToCategory != null)
				{
					existingTagToCategory.setSorting(sortValue);
				}
				// if the mapping is to be removed, add it to the intermediate
				// removeCategoryies collection
				else if (!category.isSelected() && existingTagToCategory != null)
				{
					removeCategoryies.add(existingTagToCategory);
				}

			}

			// remove the category mapping, from both the tag and the category
			for (final TagToCategory removeCategory : removeCategoryies)
			{
				tag.getTagToCategories().remove(removeCategory);
				removeCategory.getCategory().getTagToCategories().remove(removeCategory);
			}

		}
		catch (final Exception ex)
		{
			// probably could not find the tag, but this shouldn't happen
			ExceptionUtilities.handleException(ex);
		}
	}

	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(final UIProjectData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public List<UITagProjectData> getProjects()
	{
		return projects;
	}

	public void setProjects(final List<UITagProjectData> projects)
	{
		this.projects = projects;
	}
	
    public String getExclusionArray(final Integer id)
	{
		return "[]";
	}

}
