package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

@Name("tagHome")
public class TagHome extends VersionedEntityHome<Tag> implements DisplayMessageInterface
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -7321335882716157007L;
	private List<UICategoryData> categories = new ArrayList<UICategoryData>();
	/** The message to be displayed to the user */
	private String displayMessage;

	private List<UITagProjectData> projects = new ArrayList<UITagProjectData>();

	private UIProjectData selectedTags;

	@Override
	protected Tag createInstance()
	{
		Tag tag = new Tag();
		return tag;
	}

	public List<UICategoryData> getCategories()
	{
		return categories;
	}

	public Tag getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	@Override
	public String getDisplayMessage()
	{
		return displayMessage;
	}

	public String getExclusionArray(final Integer id)
	{
		return "[]";
	}

	public List<UITagProjectData> getProjects()
	{
		return projects;
	}
	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}



	public Integer getTagTagId()
	{
		return (Integer) getId();
	}

	public boolean isWired()
	{
		return true;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public String persist()
	{
		try
		{
			updateCategories();
			updateTags();
			updateProjects();
			return super.persist();
		}
		catch (final PersistenceException ex)
		{
			ExceptionUtilities.handleException(ex);	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.setDisplayMessage("The tag requires a unique name");
			else
				this.setDisplayMessage("The tag could not be saved");
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			this.setDisplayMessage("The tag could not be saved");
		}
		
		return null;
	}

	public void populate()
	{
		EntityUtilities.populateTagCategories(this.getInstance(), categories);

		selectedTags = new UIProjectData();
		EntityUtilities.populateTagTags(this.getInstance(), selectedTags);
		EntityUtilities.populateTagProjects(this.getInstance(), projects);
	}

	public void setCategories(final List<UICategoryData> value)
	{
		categories = value;
	}

	public void setDisplayMessage(final String displayMessage)
	{
		this.displayMessage = displayMessage;
	}

	public void setProjects(final List<UITagProjectData> projects)
	{
		this.projects = projects;
	}

	public void setSelectedTags(final UIProjectData selectedTags)
	{
		this.selectedTags = selectedTags;
	}

	public void setTagTagId(final Integer id)
	{
		setId(id);
	}

	public String update()
	{
		try
		{
			updateCategories();
			updateTags();
			updateProjects();
			return super.update();
		}
		catch (final PersistenceException ex)
		{
			ExceptionUtilities.handleException(ex);	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.setDisplayMessage("The tag requires a unique name");
			else
				this.setDisplayMessage("The tag could not be saved");
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			this.setDisplayMessage("The tag could not be saved");
		}
		
		return null;
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
			final List<Tag> existingTags = tag.getTags();

			// make a note of the tags that were removed
			final List<Tag> removeTags = selectedTags.getRemovedTags(existingTags);
			// make a note of the tags that were added
			final List<Tag> addTags = selectedTags.getAddedTags(existingTags);

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
	
	public void wire()
	{
		getInstance();
	}

}
