package com.redhat.topicindex.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.seam.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuilder;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

@Path("/1")
public class RESTv1
{
	@Context UriInfo uriInfo;
	
	private String getBaseUrl()
	{
		final String fullPath = uriInfo.getAbsolutePath().toString();
		final int index = fullPath.indexOf(Constants.BASE_REST_PATH);
		if (index != -1)
			return fullPath.substring(0, index + Constants.BASE_REST_PATH.length());
		
		return null;
	}
	
	private <T> Response getResource(final Class<T> type, final RestRepresentation<T> restRepresentation, final Object id, final String mimeType, final String baseUri)
	{
		return getResource(type, restRepresentation, id, mimeType, baseUri, null);
	}
	
	private <T> Response getResource(final Class<T> type, final RestRepresentation<T> restRepresentation, final Object id, final String mimeType, final String baseUri, final String fileName)
	{
		assert type != null : "The type parameter can not be null";
		assert id != null : "The id parameter can not be null";
		assert mimeType != null : "The mimeType parameter can not be null";
		assert restRepresentation != null : "The restRepresentation parameter can not be null";
		assert baseUri != null : "The restRepresentation parameter can not be null";
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final T entity = entityManager.find(type, id);
		
		if (entity != null)
		{
			/* create a pretty printing Gson object */
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			/* create the REST representation of the topic */
			restRepresentation.initialize(entity, this.getBaseUrl(), baseUri);
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
	
	@GET
	@Path("/topic/get/{topicId}")
	public Response getTopic(@PathParam("topicId") final Integer id)
	{
		assert id != null : "The id parameter can not be null";
						
		return getResource(Topic.class, new TopicV1(), id, "application/json", "topic");
	}
	
	@GET
	@Path("/filter/{filterId}/docbookZip")
	public Response getFilterDocbookZip(@PathParam("filterId") final Integer id)
	{
		assert id != null : "The id parameter can not be null";
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final Filter entity = entityManager.find(Filter.class, id);
		
		if (entity != null)
		{
			final DocbookBuildingOptions docbookBuildingOptions = new DocbookBuildingOptions();
			docbookBuildingOptions.syncWithFilter(entity);
			
			final DocbookBuilder builder = new DocbookBuilder();
			final byte[] zipFile = builder.buildDocbookZipFile(entity, docbookBuildingOptions);
						
			/* build a response */
			final ResponseBuilder response = Response.ok(zipFile, "application/zip");
			response.header("Content-Disposition", "attachment; filename=Book.zip");
			return response.build();
		}
		
		/* topic was not found, so return a 404 error */
		throw new WebApplicationException(404);
	}
}
