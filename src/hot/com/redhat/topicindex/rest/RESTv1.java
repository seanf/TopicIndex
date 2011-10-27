package com.redhat.topicindex.rest;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.drools.WorkingMemory;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuilder;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

@Path("/1")
public class RESTv1
{
	private <T> Response getResource(final Class<T> type, final RestRepresentation<T> restRepresentation, final Object id, final String mimeType, final String fileName)
	{
		assert type != null : "The type parameter can not be null";
		assert id != null : "The id parameter can not be null";
		assert mimeType != null : "The mimeType parameter can not be null";
		assert restRepresentation != null : "The restRepresentation parameter can not be null";
		
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final T entity = entityManager.find(type, id);
		
		if (entity != null)
		{
			/* create a prety printing Gson object */
			final Gson gson = new GsonBuilder().setPrettyPrinting().create();
			/* create the REST representation of the topic */
			restRepresentation.initialize(entity);
			/* Convert the REST representation to a JSON string */
			final String json = gson.toJson(restRepresentation);
			/* build a response */
			final ResponseBuilder response = Response.ok(json);
			response.type(mimeType);
			response.header("Content-Disposition", "attachment; filename=" + fileName);
			return response.build();
		}
		
		/* topic was not found, so return a 404 error */
		throw new WebApplicationException(404);		
	}
	
	@GET
	@Path("/topic/{topicId}")
	public Response getTopic(@PathParam("topicId") final Integer id)
	{
		assert id != null : "The id parameter can not be null";
		
		return getResource(Topic.class, new TopicV1(), id, "application/json", "Topic" + id + ".json");
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
			final ResponseBuilder response = Response.ok(zipFile);
			response.type("application/zip");
			response.header("Content-Disposition", "attachment; filename=Book.zip");
			return response.build();
		}
		
		/* topic was not found, so return a 404 error */
		throw new WebApplicationException(404);
	}
}
