package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.factory.TagV1Factory;

@Path("/1")
public class TagRESTv1 extends RESTv1
{
	@GET
	@Path("/tag/get/json/{id}")
	public Response getTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getResource(Tag.class, new TagV1Factory(), id, "application/json", expand);
	}
}
