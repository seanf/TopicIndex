package com.redhat.topicindex.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.seam.Component;

import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuilder;
import com.redhat.topicindex.utils.docbookbuilding.DocbookBuildingOptions;

public class FilterRESTv1 extends RESTv1
{
	@GET
	@Path("/filter/get/zip/{filterId}/docbookZip")
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
