package com.redhat.topicindex.rest.sharedinterface;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.redhat.topicindex.rest.entities.TopicV1;

@Path("/1")
public interface TopicRESTInterfaceV1<T>
{
	@GET
	@Path("/topic/get/json/{id}")	
	public T getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes({"application/json", "text/plain"})
	public void updateJSONTopic(@PathParam("id") final Integer id, final T dataObject);
}
