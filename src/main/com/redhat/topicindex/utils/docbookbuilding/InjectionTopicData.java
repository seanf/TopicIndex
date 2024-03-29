package com.redhat.topicindex.utils.docbookbuilding;
/**
 *  This class represents a topic that was included in a custom injection point.
 */
public class InjectionTopicData
{
	/** The topic ID */
	public Integer topicId;
	/** whether this topic was marked as optional */
	public boolean optional;
	
	public InjectionTopicData(final Integer topicId, final boolean optional)
	{
		this.topicId = topicId;
		this.optional = optional;
	}
}