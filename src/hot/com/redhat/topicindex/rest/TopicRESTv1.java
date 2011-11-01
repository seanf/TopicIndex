package com.redhat.topicindex.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.rest.sharedinterface.TopicRESTInterfaceV1;

@Path("/1")
public class TopicRESTv1 extends RESTv1 implements TopicRESTInterfaceV1
{
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public TopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Topic.class, new TopicV1Factory(), id, expand);
	}
	
	@GET
	@Path("/topic/get/xml/{id}")	
	@Produces("application/xml")
	@Consumes({"*"})
	public TopicV1 getXMLTopic(Integer id, String expand)
	{
		assert id != null : "The id parameter can not be null";
		
		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand);
	}

	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONTopic(@PathParam("id") final Integer id, final TopicV1 dataObject)
	{
		updateEntity(Topic.class, dataObject, new TopicV1Factory(), id);
	}

	@PUT
	@Path("/topic/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLTopic(@PathParam("id") final Integer id, final TopicV1 dataObject)
	{
		updateEntity(Topic.class, dataObject, new TopicV1Factory(), id);
	}
	
}
