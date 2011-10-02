package com.redhat.topicindex.sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.docbookbuilding.InjectionTopicData;

public class TopicTitleSorter implements ExternalMapSort<Integer, Topic, InjectionTopicData>
{
	 public void sort(final Map<Integer, Topic> map, final List<InjectionTopicData> list) 
	    {
	        if (map == null || list == null)
	        	return;
		 
		 	Collections.sort(list, new Comparator<InjectionTopicData>() 
	        {
	            public int compare(final InjectionTopicData o1, final InjectionTopicData o2)
	            {
	            	final boolean v1Exists = map.containsKey(o1.topicId);
	            	final boolean v2Exists = map.containsKey(o2.topicId);
	            	
	            	if (!v1Exists && !v2Exists)
	            		return 0;
	            	if (!v1Exists)
	            		return -1;
	            	if (!v2Exists)
	            		return 1;
	            	
	            	final Topic v1 = map.get(o1.topicId);
	            	final Topic v2 = map.get(o2.topicId);
	            	
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

