package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("topicToTopicSourceUrlList")
public class TopicToTopicSourceUrlList extends EntityQuery<TopicToTopicSourceUrl>
{

	private static final String EJBQL = "select topicToTopicSourceUrl from TopicToTopicSourceUrl topicToTopicSourceUrl";

	private static final String[] RESTRICTIONS = {};

	private TopicToTopicSourceUrl topicToTopicSourceUrl = new TopicToTopicSourceUrl();

	public TopicToTopicSourceUrlList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TopicToTopicSourceUrl getTopicToTopicSourceUrl()
	{
		return topicToTopicSourceUrl;
	}
}
