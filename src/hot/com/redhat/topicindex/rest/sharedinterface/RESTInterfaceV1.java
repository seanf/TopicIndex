package com.redhat.topicindex.rest.sharedinterface;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;

import org.jboss.resteasy.plugins.providers.atom.Feed;

import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.formatter.DateFormat;

@Path("/1")
public interface RESTInterfaceV1
{
	/* SYSTEM FUNCTIONS */
	@PUT
	@Path("/settings/rerenderTopic")
	@Consumes({"*"})
	public void setRerenderTopic(@QueryParam("enabled") final Boolean enalbed);
	
	/* TOPIC FUNCTIONS */
	@GET
	@Path("/topics/get/json/all")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopics(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/atom/{query}")
	@Produces(MediaType.APPLICATION_ATOM_XML)
	@Consumes(
	{ "*" })
	public Feed getATOMTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/json/{query}")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/xml/all")	
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getXMLTopics(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public TopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public TopicV1 getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}/xml")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}/xmlContainedIn")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("container") final String containerName);
	
	@GET
	@Path("/topic/get/xml/{id}/xmlNoContainer")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes({"*"})
	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("includeTitle") final Boolean includeTitle);
	
	@PUT
	@Path("/topic/put/json/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({MediaType.APPLICATION_JSON})
	public TopicV1 updateJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, final TopicV1 dataObject);
	
	@PUT
	@Path("/topic/put/xml/{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({MediaType.APPLICATION_XML})
	public TopicV1 updateXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, final TopicV1 dataObject);
	
	/* TAG FUNCTIONS */
	@GET
	@Path("/tag/get/json")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public BaseRestCollectionV1<TagV1> getJSONTags(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/tag/get/json/{id}")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public TagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/tag/get/xml/{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public TagV1 getXMLTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/tag/put/json/{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	public void updateJSONTag(@PathParam("id") final Integer id, final TagV1 dataObject);
	
	@PUT
	@Path("/tag/put/xml/{id}")
	@Consumes({MediaType.APPLICATION_XML})
	public void updateXMLTag(@PathParam("id") final Integer id, final TagV1 dataObject);
	
	/* CATEGORY FUNCTIONS */
	@GET
	@Path("/category/get/json")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public BaseRestCollectionV1<CategoryV1> getJSONCategories(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/category/get/json/{id}")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public CategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/category/get/xml/{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public CategoryV1 getXMLCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/category/put/json/{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	public void updateJSONCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject);
	
	@PUT
	@Path("/category/put/xml/{id}")
	@Consumes({MediaType.APPLICATION_XML})
	public void updateXMLCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject);
	
	/* PROJECT FUNCTIONS */
	@GET
	@Path("/project/get/json")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public BaseRestCollectionV1<ProjectV1> getJSONProjects(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/project/get/json/{id}")	
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes({"*"})
	public ProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/project/get/xml/{id}")
	@Produces(MediaType.APPLICATION_XML)
	@Consumes({"*"})
	public ProjectV1 getXMLProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/project/put/json/{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	public void updateJSONProject(@PathParam("id") final Integer id, final ProjectV1 dataObject);
	
	@PUT
	@Path("/project/put/xml/{id}")
	@Consumes({MediaType.APPLICATION_XML})
	public void updateXMLProject(@PathParam("id") final Integer id, final ProjectV1 dataObject);
}
