package com.redhat.topicindex.utils.structures;

import com.redhat.topicindex.entity.Topic;

/**
 * This class is used to map an image referenced inside a topic to the topic
 * itself. This is mostly for error reporting purposes.
 */
public class TopicImageData
{
	private Topic topic;
	private String imageName;

	public Topic getTopic()
	{
		return topic;
	}

	public void setTopic(Topic topic)
	{
		this.topic = topic;
	}

	public String getImageName()
	{
		return imageName;
	}

	public void setImageName(String imageName)
	{
		this.imageName = imageName;
	}

	public TopicImageData(final Topic topic, final String imageName)
	{

	}

}
