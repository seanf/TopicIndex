package com.redhat.topicindex.session;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.*;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("topicTagEdit")
public class TopicTagEdit extends EntityHome<Topic>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -4027786363334624546L;

	public void setTagId(Integer id)
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Tag tagEntity = entityManager.find(Tag.class, id);

		this.getInstance().getTopicToTags().add(new TopicToTag(this.getInstance(), tagEntity));
	}

	public Integer getTagId()
	{
		return 0;
	}

	public void setTopicTopicId(Integer id)
	{
		setId(id);
	}

	public Integer getTopicTopicId()
	{
		return (Integer) getId();
	}

	@Override
	protected Topic createInstance()
	{
		Topic topic = new Topic();
		return topic;
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

	public Topic getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
