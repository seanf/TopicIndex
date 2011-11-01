package com.redhat.topicindex.rest.sharedinterface;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.redhat.topicindex.rest.entities.TopicV1;

@Path("/1")
public interface TopicRESTInterfaceV1<T>
{
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces("application/json")
	public T getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/json/{id}")
	@Produces("application/xml")
	public T getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes({"application/json", "text/plain"})
	public void updateJSONTopic(@PathParam("id") final Integer id, final T dataObject);
}
