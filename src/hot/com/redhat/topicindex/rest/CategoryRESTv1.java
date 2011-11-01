package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.factory.CategoryV1Factory;

@Path("/1")
public class CategoryRESTv1 extends RESTv1
{
	@GET
	@Path("/category/get/json/{id}")
	@Produces("application/json")
	public CategoryV1 getCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getJSONResource(Category.class, new CategoryV1Factory(), id, expand);
	}
}
