package com.redhat.topicindex.session;

import static ch.lambdaj.Lambda.*;
import static ch.lambdaj.collection.LambdaCollections.with;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.ImageFile;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.TagToCategory;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.entity.TopicSourceUrl;
import com.redhat.topicindex.entity.TopicToTag;
import com.redhat.topicindex.entity.TopicToTopicSourceUrl;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;
import com.redhat.topicindex.utils.structures.DroolsEvent;
import com.redhat.topicindex.utils.structures.tags.UICategoryData;
import com.redhat.topicindex.utils.structures.tags.UIProjectData;
import com.redhat.topicindex.utils.structures.tags.UITagData;
import com.redhat.topicindex.utils.structures.tags.UIProjectCategoriesData;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

@Name("topicHome")
public class TopicHome extends VersionedEntityHome<Topic> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 1701692331956663275L;
	private @In WorkingMemory businessRulesWorkingMemory;
	private @In Identity identity;
	private ImageFile newImageFile = new ImageFile();
	private TopicSourceUrl newTopicSourceUrl = new TopicSourceUrl();
	private ArrayList<Integer> processedTopics = new ArrayList<Integer>();
	private UIProjectData selectedTags;
	private HashMap<Integer, ArrayList<Integer>> tagExclusions;
	
	public TopicHome()
	{
		this.setCreatedMessage(createValueExpression("Successfully Created Topic With ID: #{topic.topicId} Title: #{topic.topicTitle}"));
		this.setDeletedMessage(createValueExpression("Successfully Created Topic With ID: #{topic.topicId} Title: #{topic.topicTitle}"));
		this.setUpdatedMessage(createValueExpression("Successfully Created Topic With ID: #{topic.topicId} Title: #{topic.topicTitle}"));
	}
	
	@Override
	protected Topic createInstance() 
    {
		Topic topic = new Topic();
		return topic;
	}
	
	public Topic getDefinedInstance() 
	{
		return isIdDefined() ? getInstance() : null;
	}
	
    public String getExclusionArray(final Integer id)
	{
		String values = "";
		if (tagExclusions.containsKey(id))
		{
			for (final Integer exclusion : tagExclusions.get(id))
			{
				if (values.length() != 0)
					values += ", ";
				
				values += exclusion.toString();
			}
		}
		
		return "[" + values + "]";
	}
	
	@Override
	@Factory(value = "topic")
	public Topic getInstance() 
	{
		return super.getInstance();
	}
	
	public String getMultipleUpdateUrl()
	{
		final String retvalue = "/CustomSearchTopicList.seam?topicIds=" + getTopicList();
		return retvalue;
	}
	
	public ImageFile getNewImageFile() {
		return newImageFile;
	}
	
	public TopicSourceUrl getNewTopicSourceUrl() {
		return newTopicSourceUrl;
	}
	
	public UIProjectData getSelectedTags()
	{
		return selectedTags;
	}

	public HashMap<Integer, ArrayList<Integer>> getTagExclusions() {
		return tagExclusions;
	}

	protected Tag getTagFromId(final Integer tagId)
	{
		final TagHome tagHome = new TagHome();
		tagHome.setId(tagId);
		return tagHome.getInstance();
	}

	protected String getTopicList()
	{
		String retValue = "";
		for (final Integer topicId : processedTopics)
		{
			if (retValue.length() != 0)
				retValue += ",";
			retValue += topicId;
		}		
		return retValue;
	}

	public Integer getTopicTopicId() {
		return (Integer) getId();
	}

	public boolean isDoingMultipleUpdates()
	{
		return processedTopics.size() != 0;
	}

	public boolean isWired() {
		return true;
	}
	
	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}
	
	@Override
	public String persist()
	{
		updateTags();
		prePersist();		
		return super.persist();
	}
	
	public String persistEx(final boolean addMore)
	{
		persist();
		processedTopics.add(this.getInstance().getTopicId());
		this.clearInstance();
		this.clearDirty();	
		
		if (addMore) return EntityUtilities.buildEditNewTopicUrl(selectedTags);
		
		return "backToList";
	}
	
	public void populate()
	{
		populateTags();
	}
	
	public void populateTags()
	{
		selectedTags = new UIProjectData();
		EntityUtilities.populateTopicTags(this.getInstance(), selectedTags);
		tagExclusions = EntityUtilities.populateExclusionTags();
		EntityUtilities.populateMutuallyExclusiveCategories(selectedTags);
	}

	
	protected void prePersist()
	{

	}
	
	protected void preUpdate()
	{
		final Topic topic = this.getInstance();
		businessRulesWorkingMemory.setGlobal("topic", topic);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		businessRulesWorkingMemory.insert(new DroolsEvent("UpdateTopicHome"));
		businessRulesWorkingMemory.fireAllRules();
	}
	
	public void removeTopicURL(final TopicToTopicSourceUrl url)
    {
    	this.getInstance().getTopicToTopicSourceUrls().remove(url);
    	if (this.isManaged())
    		this.persist();
    	else
    		this.update();
    }
	
	public void saveNewTopicSourceUrl()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");	
			
			entityManager.persist(newTopicSourceUrl);
			
			final TopicToTopicSourceUrl topicToTopicSourceUrl = new TopicToTopicSourceUrl(newTopicSourceUrl, this.getInstance());
			
			entityManager.persist(topicToTopicSourceUrl);
			
			this.getInstance().getTopicToTopicSourceUrls().add(topicToTopicSourceUrl);
			newTopicSourceUrl = new TopicSourceUrl();
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}
	
	public void setNewImageFile(ImageFile newImageFile) {
		this.newImageFile = newImageFile;
	}
	
	public void setNewTopicSourceUrl(final TopicSourceUrl newTopicSourceUrl) {
		this.newTopicSourceUrl = newTopicSourceUrl;
	}
	
	public void setSelectedTags(UIProjectData value)
	{
		selectedTags = value;
	}
	
	public void setTagExclusions(HashMap<Integer, ArrayList<Integer>> tagExclusions) {
		this.tagExclusions = tagExclusions;
	}
	
	public void setTopicTopicId(Integer id) {
		setId(id);
	}

	public void triggerCreateEvent()
	{
		final Topic topic = this.getInstance();
	
		if (!this.isManaged())
		{
			// get the tags from the url, and add them by default		
			final FacesContext context = FacesContext.getCurrentInstance();
			final Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
			
			for (final String key : paramMap.keySet())
			{
				try
				{
					if (key.startsWith(Constants.MATCH_TAG) && 
						Integer.parseInt(paramMap.get(key)) == Constants.MATCH_TAG_STATE)
					{
						final Integer tagID = Integer.parseInt(key.replace(Constants.MATCH_TAG, ""));						
						topic.addTag(tagID);
					}				
				}
				catch (final NumberFormatException ex)
				{
					ExceptionUtilities.handleException(ex);
				}
			}
		}
		
		// now run drools to modify the topic as needed
		businessRulesWorkingMemory.setGlobal("topic", topic);
		EntityUtilities.injectSecurity(businessRulesWorkingMemory, identity);
		
		if (this.isManaged())
			businessRulesWorkingMemory.insert(new DroolsEvent("LoadTopicHome"));
		else
			businessRulesWorkingMemory.insert(new DroolsEvent("NewTopicHome"));
		
		businessRulesWorkingMemory.fireAllRules();
	}

	@Override
	public String update()
	{
		updateTags();
		preUpdate();		
		return super.update();
	}
	
	public String updateEx(final boolean addMore)
	{
		update();
		processedTopics.add(this.getInstance().getTopicId());
		this.clearInstance();
		this.clearDirty();		
		
		if (addMore) return EntityUtilities.buildEditNewTopicUrl(selectedTags);
		
		return "backToList";
	}

	@SuppressWarnings("serial")
	public void updateTags()
	{
		final Topic topic = this.getInstance();
		
		if (topic != null)
		{		
			final ArrayList<Tag> selectedTagObjects = new ArrayList<Tag>();
			
			for (final UIProjectCategoriesData project : selectedTags.getProjectCategories())
			{
				for (final UICategoryData cat : project.getCategories())
				{
					// is this a mutually exclusive category?
					if (cat.isMutuallyExclusive())
					{
						// if so, the selected tag is stored in the selectedID field
						// of the category GuiInputData
						// this has the effect of removing any other tags that might
						// be already selected in this category
						if (cat.getSelectedTag() != null)
							selectedTagObjects.add(getTagFromId(cat.getSelectedTag()));
	
					}
					else
					{
						// otherwise we find the selected tags from the tag GuiInputData
						// objects in the ArrayList
						for (final UITagData tagId : cat.getTags())
						{
							// if tag is selected
							if (tagId.isSelected())
								selectedTagObjects.add(getTagFromId(tagId.getId()));
						}
					}
				}
			}
										
			// match up selected tags with existing tags			
			final Set<TopicToTag> topicToTags = topic.getTopicToTags();
									
			// make a note of the tags that were removed
			final ArrayList<Tag> removeTags = new ArrayList<Tag>();
			for (final TopicToTag topicToTag : topicToTags)
			{
				final Tag existingTag = topicToTag.getTag();
				
				if (!selectedTagObjects.contains(existingTag))
				{
					boolean hasPermission = true;
					
					// check to see if we have authority to modify this flag thanks to the
					// category(s) it belongs to
					for (TagToCategory category : existingTag.getTagToCategories())
					{
						try
						{
							Identity.instance().checkRestriction("#{s:hasPermission('" + category.getCategory().getCategoryName() + "', 'Enabled', null)}");
							hasPermission = true;
							break;
						}
						catch (final NotLoggedInException ex)
						{
							ExceptionUtilities.handleException(ex);
						}
						catch (final AuthorizationException ex)
						{
							ExceptionUtilities.handleException(ex);
						}
					}
					
					// otherwise see if we had permission on the tag itself
					if (!hasPermission)
					{
						try
						{
							Identity.instance().checkRestriction("#{s:hasPermission('" + existingTag.getTagName() + "', 'Enabled', null)}");
							hasPermission = true;
							break;
						}
						catch (final NotLoggedInException ex)
						{
							ExceptionUtilities.handleException(ex);
						}
						catch (final AuthorizationException ex)
						{
							ExceptionUtilities.handleException(ex);
						}
					}
										
					if (hasPermission)
					{
						// if we get to this point (i.e. no exception was thrown), we had authority to alter this flag
						// add to external collection to avoid modifying a collection while looping over it
						removeTags.add(existingTag);
					}

				}
			}		
			
			// now make a note of the additions
			final ArrayList<Tag> addTags = new ArrayList<Tag>();
			for (final Tag selectedTag : selectedTagObjects)
			{
				if (filter(having(on(TopicToTag.class).getTag(), equalTo(selectedTag)), topicToTags).size() == 0)
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
					// clean the tag relationship from both collections
					for (final Set<TopicToTag> collection : new ArrayList<Set<TopicToTag>> () {{ add(topicToTags); add(removeTag.getTopicToTags()); }})
						with(collection).remove(having(on(TopicToTag.class).getTag(), equalTo(removeTag)));					
				}
				
				for (final Tag addTag : addTags)
				{
					topicToTags.add(new TopicToTag(topic, addTag));					
				}
			}
		}
	}

	public void wire() {
		getInstance();
	}
}
