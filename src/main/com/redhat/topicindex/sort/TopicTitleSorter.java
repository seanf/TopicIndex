package com.redhat.topicindex.sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.docbookbuilding.InjectionTopicData;

public class TopicTitleSorter implements ExternalListSort<Integer, Topic, InjectionTopicData>
{
	 public void sort(final List<Topic> topics, final List<InjectionTopicData> list) 
	    {
	        if (topics == null || list == null)
	        	return;
		 
		 	Collections.sort(list, new Comparator<InjectionTopicData>() 
	        {
	            public int compare(final InjectionTopicData o1, final InjectionTopicData o2)
	            {
	            	Topic topic1 = null;
	            	Topic topic2 = null;
	            	
	            	for (final Topic topic : topics)
	            	{
	            		if (topic.getTopicId().equals(o1.topicId))
	            			topic1 = topic;
	            		if (topic.getTopicId().equals(o2.topicId))
	            			topic2 = topic;
	            		
	            		if (topic1 != null && topic2 != null)
	            			break;
	            	}
	            	
	            	final boolean v1Exists = topic1 != null;
	            	final boolean v2Exists = topic2 != null;
	            	
	            	if (!v1Exists && !v2Exists)
	            		return 0;
	            	if (!v1Exists)
	            		return -1;
	            	if (!v2Exists)
	            		return 1;
	            	
	            	final Topic v1 = topic1;
	            	final Topic v2 = topic2;
	            	
	            	if (v1 == null && v2 == null)
	            		return 0;
	            	
	            	if (v1 == null)
	            		return -1;
	            	
	            	if (v2 == null)
	            		return 1;
	            	
	            	if (v1.getTopicTitle() == null && v2.getTopicTitle() == null)
	            		return 0;
	            	
	            	if (v1.getTopicTitle() == null)
	            		return -1;
	            	
	            	if (v2.getTopicTitle() == null)
	            		return 1;
	            	
	            	return v1.getTopicTitle().toLowerCase().compareTo(v2.getTopicTitle().toLowerCase());
	            }
	        });
	    }
}

