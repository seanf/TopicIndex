package com.redhat.topicindex.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.rest.sharedinterface.RESTInterfaceV1;
import com.redhat.topicindex.utils.topicrenderer.TopicRenderer;

@Path("/1")
public class TopicRESTv1 extends RESTv1 implements RESTInterfaceV1
{
	@PUT
	@Path("/settings/rerenderTopic")
	@Consumes({"*"})
	public void setRerenderTopic(@QueryParam("enabled") final Boolean enalbed)
	{
		System.setProperty(TopicRenderer.ENABLE_RENDERING_PROPERTY, enalbed == null ? null : enalbed.toString());
	}
	
	@GET
	@Path("/topic/get/json")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopics(@QueryParam("expand") final String expand)
	{
		return getJSONResources(Topic.class, new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}
	
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
	public TopicV1 getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
		
		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand);
	}
	
	@GET
	@Path("/topic/get/xml/{id}/xml")
	@Produces("application/xml")
	@Consumes({"*"})
	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
		
		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXml();
	}
	
	@GET
	@Path("/topic/get/xml/{id}/xmlContainedIn")
	@Produces("application/xml")
	@Consumes({"*"})
	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("container") final String containerName)
	{
		assert id != null : "The id parameter can not be null";
		assert containerName != null : "The containerName parameter can not be null";
		
		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNewContainer(containerName);
	}
	
	@GET
	@Path("/topic/get/xml/{id}/xmlNoContainer")
	@Produces("text/plain")
	@Consumes({"*"})
	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("includeTitle") final Boolean includeTitle)
	{
		assert id != null : "The id parameter can not be null";
		
		final String retValue = getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNoContainer(includeTitle);
		return retValue;
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
