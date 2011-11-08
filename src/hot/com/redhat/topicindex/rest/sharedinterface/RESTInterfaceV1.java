package com.redhat.topicindex.rest.sharedinterface;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;

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
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopics(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/json/editedSince")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopicsEditedSince(@QueryParam("date") @DateFormat("dd-MMM-yyyy") final Date date, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/json/{query}")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getJSONTopicsWithQuery(@PathParam("query") PathSegment query, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topics/get/xml/all")	
	@Produces("application/xml")
	@Consumes({"*"})
	public BaseRestCollectionV1<TopicV1> getXMLTopics(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public TopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public TopicV1 getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}/xml")
	@Produces("application/xml")
	@Consumes({"*"})
	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/topic/get/xml/{id}/xmlContainedIn")
	@Produces("application/xml")
	@Consumes({"*"})
	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("container") final String containerName);
	
	@GET
	@Path("/topic/get/xml/{id}/xmlNoContainer")
	@Produces("text/plain")
	@Consumes({"*"})
	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("includeTitle") final Boolean includeTitle);
	
	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONTopic(@PathParam("id") final Integer id, final TopicV1 dataObject);
	
	@PUT
	@Path("/topic/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLTopic(@PathParam("id") final Integer id, final TopicV1 dataObject);
	
	/* TAG FUNCTIONS */
	@GET
	@Path("/tag/get/json")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<TagV1> getJSONTags(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/tag/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public TagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/tag/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public TagV1 getXMLTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/tag/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONTag(@PathParam("id") final Integer id, final TagV1 dataObject);
	
	@PUT
	@Path("/tag/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLTag(@PathParam("id") final Integer id, final TagV1 dataObject);
	
	/* CATEGORY FUNCTIONS */
	@GET
	@Path("/category/get/json")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<CategoryV1> getJSONCategories(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/category/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public CategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/category/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public CategoryV1 getXMLCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/category/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject);
	
	@PUT
	@Path("/category/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject);
	
	/* PROJECT FUNCTIONS */
	@GET
	@Path("/project/get/json")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<ProjectV1> getJSONProjects(@QueryParam("expand") final String expand);
	
	@GET
	@Path("/project/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public ProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@GET
	@Path("/project/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public ProjectV1 getXMLProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand);
	
	@PUT
	@Path("/project/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONProject(@PathParam("id") final Integer id, final ProjectV1 dataObject);
	
	@PUT
	@Path("/project/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLProject(@PathParam("id") final Integer id, final ProjectV1 dataObject);
}
