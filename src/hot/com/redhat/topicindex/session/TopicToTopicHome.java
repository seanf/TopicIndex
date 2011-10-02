package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("topicToTopicHome")
public class TopicToTopicHome extends EntityHome<TopicToTopic>
{

	public void setTopicToTopicTopicToTopicId(Integer id)
	{
		setId(id);
	}

	public Integer getTopicToTopicTopicToTopicId()
	{
		return (Integer) getId();
	}

	@Override
	protected TopicToTopic createInstance()
	{
		TopicToTopic topicToTopic = new TopicToTopic();
		return topicToTopic;
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

	public TopicToTopic getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
