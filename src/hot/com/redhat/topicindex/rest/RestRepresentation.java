package com.redhat.topicindex.rest;

/**
 * Provides a way to initialize a simple object designed for transfer over the
 * REST interface from a JPA entity.
 */
public interface RestRepresentation<T>
{
	void initialize(final T entity, final String baseUrl, final String restBasePath);
}
