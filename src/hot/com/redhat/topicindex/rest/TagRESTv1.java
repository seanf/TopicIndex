package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.factory.TagV1Factory;

@Path("/1")
public class TagRESTv1 extends RESTv1
{
	@GET
	@Path("/tag/get/json/{id}")
	@Produces("application/json")
	public TagV1 getTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getJSONResource(Tag.class, new TagV1Factory(), id, expand);
	}
}
