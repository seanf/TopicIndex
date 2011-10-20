package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static org.hamcrest.Matchers.equalTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.PersistenceException;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UITagProjectData;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("categoryHome")
public class CategoryHome extends EntityHome<Category> implements DisplayMessageInterface
{
	private static final long serialVersionUID = 6943432713479412363L;

	private UIProjectData selectedTags;
	/** The message to be displayed to the user */
	private String displayMessage;

	public void setCategoryCategoryId(Integer id)
	{
		setId(id);
	}

	public Integer getCategoryCategoryId()
	{
		return (Integer) getId();
	}

	@Override
	protected Category createInstance()
	{
		Category category = new Category();
		return category;
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

	public Category getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

	public void populate()
	{
		selectedTags = new UIProjectData();
		EntityUtilities.populateTagTags(this.getInstance(), selectedTags);
		EntityUtilities.populateTagTagsSortingForCategory(this.getInstance(), selectedTags);
	}

	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public void setSelectedTags(UIProjectData selectedTags)
	{
		this.selectedTags = selectedTags;
	}
	
	public String getExclusionArray(final Integer id)
	{
		return "[]";
	}
	
	private void updateTags()
	{
		final Category category = this.getInstance();

		if (category != null)
		{
			final List<Tag> existingTags = category.getTags();

			// make a note of the tags that were removed
			final List<Tag> removeTags = selectedTags.getRemovedTags(existingTags);
			// make a note of the tahs that were added
			final List<Pair<Tag, UITagData>> addTags = selectedTags.getExtendedAddedTags(existingTags);
			

			// only proceed if there are some changes to make
			if (removeTags.size() != 0 || addTags.size() != 0)
			{
				// remove the tags
				for (final Tag removeTag : removeTags)
				{
					category.removeTagRelationship(removeTag);
				}

				for (final Pair<Tag, UITagData> addTag : addTags)
				{
					category.addTagRelationship(addTag.getFirst(), addTag.getSecond().getNewSort());
				}
			}
		}
	}
	
	public String persist()
	{
		try
		{
			updateTags();
			return super.persist();
		}
		catch (final PersistenceException ex)
		{
			ExceptionUtilities.handleException(ex);	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.displayMessage = "The category requires a unique name";
			else
				this.displayMessage = "The category could not be saved";
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			this.displayMessage = "The category could not be saved";
		}
		
		return null;
	}
	
	public String update()
	{
		try
		{
			updateTags();
			return super.update();
		}
		catch (final PersistenceException ex)
		{
			ExceptionUtilities.handleException(ex);	
			if (ex.getCause() instanceof ConstraintViolationException)
				this.displayMessage = "The category requires a unique name";
			else
				this.displayMessage = "The category could not be saved";
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			this.displayMessage = "The category could not be saved";
		}
		
		
		return null;
	}

	@Override
	public String getDisplayMessage()
	{
		return displayMessage;
	}
}
