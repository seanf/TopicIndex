package com.redhat.topicindex.rest.factory;

import com.redhat.topicindex.rest.ExpandData;

public interface RESTDataObjectFactory<T, U>
{
	public T create(final U entity, final String baseUrl, final String dataType, final String expand);
	public T create(final U entity, final String baseUrl, final String dataType, final ExpandData expand);
}
