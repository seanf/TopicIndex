package com.redhat.topicindex.utils.structures;

import java.util.ArrayList;
import java.util.List;

import com.redhat.topicindex.entity.Topic;

/**
 * Provides a central location for storing and adding messages that are
 * generated while compiling to docbook.
 */
public class TopicErrorDatabase
{
	public static final Integer ERROR = 1;
	public static final Integer WARNING = 5;

	private List<TopicErrorData> errors = new ArrayList<TopicErrorData>();

	public int getErrorCount()
	{
		return errors.size();
	}

	public boolean hasItems()
	{
		return errors.size() != 0;
	}

	public void addError(final Topic topic, final String error)
	{
		addItem(topic, error, ERROR);
	}

	public void addWarning(final Topic topic, final String error)
	{
		addItem(topic, error, WARNING);
	}

	private void addItem(final Topic topic, final String item, final Integer level)
	{
		final TopicErrorData topicErrorData = addOrGetTopicErrorData(topic);
		topicErrorData.addError(item, level);
	}

	private TopicErrorData getErrorData(final Topic topic)
	{
		for (final TopicErrorData topicErrorData : errors)
			if (topicErrorData.getTopic().equals(topic))
				return topicErrorData;
		return null;
	}

	private TopicErrorData addOrGetTopicErrorData(final Topic topic)
	{
		TopicErrorData topicErrorData = getErrorData(topic);
		if (topicErrorData == null)
		{
			topicErrorData = new TopicErrorData();
			topicErrorData.setTopic(topic);
			errors.add(topicErrorData);
		}
		return topicErrorData;
	}

	public List<TopicErrorData> getErrors()
	{
		return errors;
	}

	public void setErrors(List<TopicErrorData> errors)
	{
		this.errors = errors;
	}
}
