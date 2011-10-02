package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("topicToTagList")
public class TopicToTagList extends EntityQuery<TopicToTag> {

	

	private static final String EJBQL = "select topicToTag from TopicToTag topicToTag";

	private static final String[] RESTRICTIONS = {};

	private TopicToTag topicToTag = new TopicToTag();

	public TopicToTagList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TopicToTag getTopicToTag() {
		return topicToTag;
	}
}
