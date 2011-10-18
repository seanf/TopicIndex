package com.redhat.topicindex.session;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;

@Name("topBar")
public class TopBar
{
	private String topicId;

	public String getBuild()
	{
		return Constants.BUILD;
	}

	public String getTopicId()
	{
		return topicId;
	}

	public void setTopicId(String topicId)
	{
		this.topicId = topicId;
	}

	public String getTopicViewUrl()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Integer topicIdInt = Integer.parseInt(topicId);
			final Topic topic = entityManager.find(Topic.class, topicIdInt);
			if (topic != null)
				return "/Topic.xhtml?topicTopicId=" + topicId;
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}

		return null;
	}

	public String getTopicEditUrl()
	{
		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			final Integer topicIdInt = Integer.parseInt(topicId);
			final Topic topic = entityManager.find(Topic.class, topicIdInt);
			if (topic != null)
				return "/TopicEdit.xhtml?topicTopicId=" + topicId;
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return null;
	}

}