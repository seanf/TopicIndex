package com.redhat.topicindex.rest.factory;

import javax.persistence.EntityManager;

import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.entities.TopicV1;

public interface RESTDataObjectFactory<T, U>
{
	public T create(final U entity, final String baseUrl, final String dataType, final String expand);
	public T create(final U entity, final String baseUrl, final String dataType, final ExpandData expand);
	public void sync(final EntityManager entityManager, final U entity, final T dataObject);
	public U create (final EntityManager entityManager, final T dataObject);
}
