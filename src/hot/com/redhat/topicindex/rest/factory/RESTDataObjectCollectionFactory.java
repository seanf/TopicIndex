package com.redhat.topicindex.rest.factory;

import java.util.ArrayList;
import java.util.List;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.ExpandDataIndexes;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class RESTDataObjectCollectionFactory<T, U>
{
	public BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType)
	{
		return create(dataObjectFactory, entities, expandName, dataType, null, null);
	}

	public BaseRestCollectionV1<T> create(final RESTDataObjectFactory<T, U> dataObjectFactory, final List<U> entities, final String expandName, final String dataType, final ExpandData expand, final String baseUrl)
	{
		assert dataObjectFactory != null : "Parameter dataObjectFactory can not be null";
		assert entities != null : "Parameter entities can not be null";

		final BaseRestCollectionV1<T> retValue = new BaseRestCollectionV1<T>();

		retValue.setSize(entities.size());
		retValue.setExpand(expandName);

		try
		{
			if (expand != null)
			{
				assert baseUrl != null : "Parameter baseUrl can not be null if parameter expand is not null";

				final ExpandDataIndexes indexes = expand.getExpandDataIndexes(expandName);
				final ExpandData secondLevelExpandData = expand.getNextLevel(expandName);

				if (indexes != null)
				{
					int start = 0;
					if (!indexes.isStartAtBegining())
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
					if (!indexes.isFinishAtEnd())
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

					if (indexes != null)
					{
						if (indexes.isDefinedStart())
							retValue.setStartExpandIndex(fixedStart);

						if (indexes.isDefinedFinsh())
							retValue.setEndExpandIndex(fixedEnd);
					}

					final List<T> restEntityArray = new ArrayList<T>();

					for (int i = fixedStart; i <= fixedEnd; ++i)
					{
						final U dbEntity = entities.get(i);
						final T restEntity = dataObjectFactory.create(dbEntity, baseUrl, dataType, secondLevelExpandData);
						restEntityArray.add(restEntity);
					}

					retValue.setItems(restEntityArray);
				}
			}

		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex);
		}

		return retValue;
	}
}
