package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("topicToTagHome")
public class TopicToTagHome extends EntityHome<TopicToTag>
{

	public void setTopicToTagTopicToTagId(Integer id)
	{
		setId(id);
	}

	public Integer getTopicToTagTopicToTagId()
	{
		return (Integer) getId();
	}

	@Override
	protected TopicToTag createInstance()
	{
		TopicToTag topicToTag = new TopicToTag();
		return topicToTag;
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

	public TopicToTag getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
