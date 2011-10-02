package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("topicToTopicList")
public class TopicToTopicList extends EntityQuery<TopicToTopic>
{

	private static final String EJBQL = "select topicToTopic from TopicToTopic topicToTopic";

	private static final String[] RESTRICTIONS = {};

	private TopicToTopic topicToTopic = new TopicToTopic();

	public TopicToTopicList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TopicToTopic getTopicToTopic()
	{
		return topicToTopic;
	}
}
