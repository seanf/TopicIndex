package com.redhat.topicindex.rest.collections;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.rest.ExpandData;
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

				final List<T> restEntityArray = new ArrayList<T>();

				for (final U dbEntity : entities)
				{
					final T restEntity = classType.newInstance();
					restEntity.initialize(dbEntity, baseUrl, expand);
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
