package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("topicToTopicSourceUrlHome")
public class TopicToTopicSourceUrlHome extends EntityHome<TopicToTopicSourceUrl>
{

	@In(create = true)
	TopicSourceUrlHome topicSourceUrlHome;

	public void setTopicToTopicSourceUrlTopicToTopicSourceUrlid(Integer id)
	{
		setId(id);
	}

	public Integer getTopicToTopicSourceUrlTopicToTopicSourceUrlid()
	{
		return (Integer) getId();
	}

	@Override
	protected TopicToTopicSourceUrl createInstance()
	{
		TopicToTopicSourceUrl topicToTopicSourceUrl = new TopicToTopicSourceUrl();
		return topicToTopicSourceUrl;
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
		TopicSourceUrl topicSourceUrl = topicSourceUrlHome.getDefinedInstance();
		if (topicSourceUrl != null)
		{
			getInstance().setTopicSourceUrl(topicSourceUrl);
		}
	}

	public boolean isWired()
	{
		if (getInstance().getTopicSourceUrl() == null)
			return false;
		return true;
	}

	public TopicToTopicSourceUrl getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
