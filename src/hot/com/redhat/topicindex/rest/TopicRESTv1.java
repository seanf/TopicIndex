package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.entities.TopicV1;

@Path("/1")
public class TopicRESTv1 extends RESTv1
{
	@GET
	@Path("/topic/get/json/{id}")
	public Response getTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getResource(Topic.class, new TopicV1(), id, "application/json", expand);
	}
}
