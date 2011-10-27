package com.redhat.topicindex.rest;

public interface RestRepresentation<T>
{
	void initialize(final T entity);
}
