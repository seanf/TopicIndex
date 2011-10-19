package com.redhat.topicindex.utils.structures;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.topicindex.entity.Topic;

/**
 * This class represents the topics that will be injected into a topic for a
 * given category.
 * 
 * Note that we are not using the actual Category or Tag object here. This is
 * because the category details need to be supplied in a @PreUpdate or @PrePersist
 * function, which prevents the Category entities from being loaded from the
 * database at runtime (to quote the documentation:
 * "A callback method must not invoke EntityManager or Query methods!"). So a
 * simple Pair will hold the Tag id and name.
 * 
 * We don't have the same problem with the Topics, as the Topic being persisted
 * already has the listed of related topics available to it in a child
 * collection.
 */
public class GenericInjectionPoint
{
	/** The details of the topic type tag */
	private Pair<Integer, String> categoryIDAndName;
	/** The topics to be linked to */
	private List<Topic> topics;

	public Pair<Integer, String> getCategoryIDAndName()
	{
		return categoryIDAndName;
	}

	public void setCategoryIDAndName(Pair<Integer, String> categoryIDAndName)
	{
		this.categoryIDAndName = categoryIDAndName;
	}

	public List<Topic> getTopics()
	{
		return topics;
	}

	public void setTopics(List<Topic> topics)
	{
		this.topics = topics;
	}

	public GenericInjectionPoint(final Pair<Integer, String> categoryIDAndName, final List<Topic> topics)
	{
		this.categoryIDAndName = categoryIDAndName;
		this.topics = topics;
	}
	
	public GenericInjectionPoint(final Pair<Integer, String> categoryIDAndName)
	{
		this.categoryIDAndName = categoryIDAndName;
		this.topics = new ArrayList<Topic>();
	}
	
	public void addTopic(final Topic topic)
	{
		this.topics.add(topic);
	}
}
