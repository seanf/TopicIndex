package com.redhat.topicindex.rest.collections;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.ExpandDataIndexes;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.entities.BaseRestV1;

public class BaseRestCollectionV1<T extends BaseRestV1<U>, U>
{
	private int size = 0;
	private String expand = null;
	private List<T> items = null;

	public int getSize()
	{
		return size;
	}

	public void setSize(final int size)
	{
		this.size = size;
	}

	public String getExpand()
	{
		return expand;
	}

	public void setExpand(final String expand)
	{
		this.expand = expand;
	}

	public List<T> getItems()
	{
		return items;
	}

	public void setItems(final List<T> items)
	{
		this.items = items;
	}

	public void initialize(final Class<T> classType, final List<U> entities, final String expandName)
	{
		initialize(classType, entities, expandName, null, null);
	}

	public void initialize(final Class<T> classType, final List<U> entities, final String expandName, final ExpandData expand, final String baseUrl)
	{
		assert entities != null : "Parameter entities can not be null";

		this.setSize(entities.size());
		this.setExpand(expandName);

		try
		{
			if (expand != null)
			{
				assert baseUrl != null : "Parameter baseUrl can not be null if parameter expand is not null";

				final ExpandDataIndexes indexes = expand.getExpandDataIndexes(expandName);
				final ExpandData secondLevelExpandData = expand.getNextLevel(expandName);
								
				int start = 0;
				if (!indexes.isImpliedStartAtBegining())
				{
					final int startIndex = indexes.getStartIndex();
					if (startIndex < 0)
					{
						start = Math.max(0, entities.size() - startIndex);
					}
					else
					{
						start = Math.min(startIndex, entities.size() - 1);
					}
				}

				int end = entities.size() - 1;
				if (!indexes.isImpliedFinishAtEnd())
				{
					final int endIndex = indexes.getEndIndex();
					if (endIndex < 0)
					{
						end = Math.max(0, entities.size() - endIndex);
					}
					else
					{
						end = Math.min(endIndex, entities.size() - 1);
					}
				}
				
				final int fixedStart = Math.min(start, end);
				final int fixedEnd = Math.max(start, end);
				
				final List<T> restEntityArray = new ArrayList<T>();
				
				for (int i = fixedStart; i <= fixedEnd; ++i)
				{
					final U dbEntity = entities.get(i);
					final T restEntity = classType.newInstance();
					restEntity.initialize(dbEntity, baseUrl, secondLevelExpandData);
					restEntityArray.add(restEntity);
				}

				this.setItems(restEntityArray);
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}
}
