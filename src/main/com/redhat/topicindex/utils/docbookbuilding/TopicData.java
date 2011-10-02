package com.redhat.topicindex.utils.docbookbuilding;

import java.util.HashMap;

import com.redhat.topicindex.entity.Topic;

/**
 * This class is used to hold the xml that will make up a chapter, along with
 * the data that represents the individual topics that go into that chapter.
 */
class TopicData
{
	/** Topic id mapped to a topic */
	public HashMap<Integer, Topic> map;
	/** The XML that represents a chapter */
	public String finalXML;

	/** Default constructor */
	public TopicData()
	{
		this.map = new HashMap<Integer, Topic>();
		this.finalXML = "";
	}
}