package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.redhat.ecs.commonutils.CollectionUtilities;

public class ExpandData
{
	private Map<String, ArrayList<String>> expandOptions = new HashMap<String, ArrayList<String>>();
	
	public ExpandData()
	{
		
	}
	
	public ExpandData(final String topLevel, final ArrayList<String> subLevels)
	{
		assert topLevel != null : "The topLevel parameter can not be null";
		assert subLevels != null : "The subLevels parameter can not be null";
		
		expandOptions.put(topLevel, subLevels);
	}
	
	public ExpandData(final String expand)
	{
		assert expand != null : "The expand parameter can not be null";
		
		final String[] expandElements = expand.split(",");
		
		assert expandElements.length >= 1 : "Expected at least 1 element in expandElements";
		
		for (final String expandElement : expandElements)
		{
			final ArrayList<String> expandSubElements = CollectionUtilities.toArrayList(expandElement.split("\\."));
			
			assert expandSubElements.size() >= 1 : "Expected at least 1 element in expandSubElements";
			
			final String topLevelExpandOption = expandSubElements.remove(0);
			
			expandOptions.put(topLevelExpandOption, expandSubElements);
		}
	}
	
	public ExpandData getNextLevel(final String topLevel)
	{
		assert topLevel != null : "The topLevel parameter can not be null";
		assert expandOptions != null : "The expandOptions instance variable can not be null";
		assert expandOptions.containsKey(topLevel) : "The expandOptions map needs to contain the topLevel parameter as a key";
		
		final ArrayList<String> expandSubElements = expandOptions.get(topLevel);
		
		assert expandSubElements != null : "Expected expandSubElements to not be null";
		
		if (expandSubElements.size() == 0)
		{
			return new ExpandData();
		}
		else
		{
			final String newTopLevelExpand = expandSubElements.remove(0);		
			return new ExpandData(newTopLevelExpand, expandSubElements);
		}
	}
	
	public boolean contains(final String topLevelExpand)
	{
		assert expandOptions != null : "The expandOptions instance variable can not be null";
		
		return expandOptions.containsKey(topLevelExpand);
	}
}
