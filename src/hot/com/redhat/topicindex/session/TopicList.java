package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.filter.TopicFilter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import java.util.Arrays;

@Name("topicList")
public class TopicList extends EntityQuery<Topic>
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4132574852922621495L;

	private static final String EJBQL = "select topic from Topic topic";

	private static final String[] RESTRICTIONS =
	{
			// "topic.topicId like concat('%', #{topicList.topic.topicId}, '%')",
			"topic.topicId in (#{topicList.topic.topicIds})", "lower(topic.topicTitle) like lower(concat('%', #{topicList.topic.topicTitle}, '%'))", "lower(topic.topicText) like lower(concat('%', #{topicList.topic.topicText}, '%'))",
			"lower(topic.topicAddedBy) like lower(concat('%', #{topicList.topic.topicAddedBy}, '%'))", "lower(topic.topicProduct) like lower(concat('%', #{topicList.topic.topicProduct}, '%'))", };

	protected TopicFilter topic = new TopicFilter();

	public TopicList(int limit, final String constructedEJBQL, final TopicFilter topic)
	{
		construct(limit, constructedEJBQL, topic);
	}

	public TopicList(int limit, final String constructedEJBQL)
	{
		construct(limit, constructedEJBQL, null);
	}

	public TopicList(int limit)
	{
		construct(limit, null, null);
	}

	public TopicList()
	{
		construct(25, null, null);
	}

	protected void construct(int limit, final String constructedEJBQL, final TopicFilter topic)
	{
		setEjbql(EJBQL);

		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(25);
	}

	public TopicFilter getTopic()
	{
		return topic;
	}
}
