package com.redhat.topicindex.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.redhat.ecs.commonutils.CollectionUtilities;

public class ExpandData
{
	private Map<ExpandDataIndexes, ArrayList<ExpandDataIndexes>> expandOptions = new HashMap<ExpandDataIndexes, ArrayList<ExpandDataIndexes>>();

	public ExpandData()
	{

	}

	public ExpandData(final ExpandDataIndexes topLevel, final ArrayList<ExpandDataIndexes> subLevels)
	{
		assert topLevel != null : "The topLevel parameter can not be null";
		assert subLevels != null : "The subLevels parameter can not be null";

		expandOptions.put(topLevel, subLevels);
	}

	public ExpandData(final String expand)
	{
		if (expand != null)
		{
			final String[] expandElements = expand.split(",");

			assert expandElements.length >= 1 : "Expected at least 1 element in expandElements";

			for (final String expandElement : expandElements)
			{
				final ArrayList<String> expandSubElements = CollectionUtilities.toArrayList(expandElement.split("\\."));

				assert expandSubElements.size() >= 1 : "Expected at least 1 element in expandSubElements";

				final ExpandDataIndexes topLevelExpandOption = new ExpandDataIndexes(expandSubElements.remove(0));

				boolean allChildrenAreValid = true;
				final ArrayList<ExpandDataIndexes> childExpandOptions = new ArrayList<ExpandDataIndexes>();
				for (final String expandSubElement : expandSubElements)
				{
					final ExpandDataIndexes childExpandOption = new ExpandDataIndexes(expandSubElement);
					if (!childExpandOption.isValid())
					{
						allChildrenAreValid = false;
						break;
					}
					childExpandOptions.add(childExpandOption);
				}

				if (topLevelExpandOption.isValid() && allChildrenAreValid)
					expandOptions.put(topLevelExpandOption, childExpandOptions);

			}
		}
	}

	public ExpandData getNextLevel(final String topLevel)
	{
		assert topLevel != null : "The topLevel parameter can not be null";
		assert expandOptions != null : "The expandOptions instance variable can not be null";

		final ArrayList<ExpandDataIndexes> expandSubElements = this.getSubExpandIndexes(topLevel);

		if (expandSubElements == null || expandSubElements.size() == 0)
		{
			return new ExpandData();
		}
		else
		{
			final ExpandDataIndexes newTopLevelExpand = expandSubElements.remove(0);
			return new ExpandData(newTopLevelExpand, expandSubElements);
		}
	}

	public boolean contains(final String topLevel)
	{
		return getExpandDataIndexes(topLevel) != null;
	}

	public ArrayList<ExpandDataIndexes> getSubExpandIndexes(final String topLevel)
	{
		assert topLevel != null : "The topLevel instance variable can not be null";
		assert expandOptions != null : "The expandOptions instance variable can not be null";

		for (final ExpandDataIndexes index : expandOptions.keySet())
			if (index.getExpandLevel().equals(topLevel))
				return expandOptions.get(index);

		return null;
	}

	public ExpandDataIndexes getExpandDataIndexes(final String topLevel)
	{
		assert topLevel != null : "The topLevel instance variable can not be null";
		assert expandOptions != null : "The expandOptions instance variable can not be null";

		for (final ExpandDataIndexes index : expandOptions.keySet())
			if (index.getExpandLevel().equals(topLevel))
				return index;

		return null;
	}
}
