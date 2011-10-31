package com.redhat.topicindex.rest.sharedinterface;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/1")
public interface TopicRESTInterfaceV1
{
	@GET
	@Path("/topic/get/json/{id}")
	public Response getTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/topic/put/json/{id}")
	public Response updateTopic(@PathParam("id") final Integer id, final String json);
}
