package com.redhat.topicindex.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.seam.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.topicindex.rest.factory.RESTDataObjectFactory;
import com.redhat.topicindex.utils.Constants;

public class RESTv1
{
	public static final String TOPICS_EXPANSION_NAME = "topics";
	public static final String TAGS_EXPANSION_NAME = "tags";
	public static final String CATEGORIES_EXPANSION_NAME = "categories";
	public static final String TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME = "incomingRelationships";
	public static final String TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME = "outgoingRelationships";
	public static final String TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME = "twoWayRelationships";
	
	public static final String TOPIC_URL_NAME = "topic";
	public static final String TAG_URL_NAME = "tag";
	public static final String CATEGORY_URL_NAME = "category";
	
	public static final String JSON_URL = "json";
	
	private @Context UriInfo uriInfo;
	
	protected String getBaseUrl()
	{
		final String fullPath = uriInfo.getAbsolutePath().toString();
		final int index = fullPath.indexOf(Constants.BASE_REST_PATH);
		if (index != -1)
			return fullPath.substring(0, index + Constants.BASE_REST_PATH.length());
		
		return null;
	}
	
	protected <T, U> Response getResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String mimeType, final String expand)
	{
		return getResource(type, dataObjectFactory, id, mimeType, expand, null);
	}
	
	protected <T, U> Response getResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String mimeType, final String expand, final String fileName)
	{
		assert type != null : "The type parameter can not be null";
		assert id != null : "The id parameter can not be null";
		assert mimeType != null : "The mimeType parameter can not be null";
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final U entity = entityManager.find(type, id);
		
		if (entity != null)
		{
			/* create a pretty printing Gson object */
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			/* create the REST representation of the topic */
			final T restRepresentation = dataObjectFactory.create(entity, this.getBaseUrl(), JSON_URL, expand);
			/* Convert the REST representation to a JSON string */
			final String json = gson.toJson(restRepresentation);
			/* build a response */
			final ResponseBuilder response = Response.ok(json, mimeType);
			/* set the optional file name */
			if (fileName != null)
				response.header("Content-Disposition", "attachment; filename=" + fileName);
			return response.build();
		}
		
		/* topic was not found, so return a 404 error */
		throw new WebApplicationException(404);		
	}
}
