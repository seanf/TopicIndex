package com.redhat.topicindex.rest.sharedinterface;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.rest.entities.TopicV1;

@Path("/1")
public interface RESTInterfaceV1<T>
{
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public T getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public T getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONTopic(@PathParam("id") final Integer id, final T dataObject);
	
	@PUT
	@Path("/topic/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLTopic(@PathParam("id") final Integer id, final T dataObject);
}
