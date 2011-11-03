package com.redhat.topicindex.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.TagV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.CategoryV1Factory;
import com.redhat.topicindex.rest.factory.ProjectV1Factory;
import com.redhat.topicindex.rest.factory.TagV1Factory;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.rest.sharedinterface.RESTInterfaceV1;
import com.redhat.topicindex.utils.topicrenderer.TopicRenderer;

@Path("/1")
public class TopicRESTv1 extends RESTv1 implements RESTInterfaceV1
{
	/* SYSTEM FUNCTIONS */
	@PUT
	@Path("/settings/rerenderTopic")
	@Consumes(
	{ "*" })
	public void setRerenderTopic(@QueryParam("enabled") final Boolean enalbed)
	{
		System.setProperty(TopicRenderer.ENABLE_RENDERING_PROPERTY, enalbed == null ? null : enalbed.toString());
	}

	/* TOPIC FUNCTIONS */
	@GET
	@Path("/topic/get/json")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public BaseRestCollectionV1<TopicV1> getJSONTopics(@QueryParam("expand") final String expand)
	{
		return getJSONResources(Topic.class, new TopicV1Factory(), TOPICS_EXPANSION_NAME, expand);
	}

	@GET
	@Path("/topic/get/json/{id}")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public TopicV1 getJSONTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Topic.class, new TopicV1Factory(), id, expand);
	}

	@GET
	@Path("/topic/get/xml/{id}")
	@Produces("application/xml")
	@Consumes(
	{ "*" })
	public TopicV1 getXMLTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand);
	}

	@GET
	@Path("/topic/get/xml/{id}/xml")
	@Produces("application/xml")
	@Consumes(
	{ "*" })
	public String getXMLTopicXML(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXml();
	}

	@GET
	@Path("/topic/get/xml/{id}/xmlContainedIn")
	@Produces("application/xml")
	@Consumes(
	{ "*" })
	public String getXMLTopicXMLContained(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("container") final String containerName)
	{
		assert id != null : "The id parameter can not be null";
		assert containerName != null : "The containerName parameter can not be null";

		return getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNewContainer(containerName);
	}

	@GET
	@Path("/topic/get/xml/{id}/xmlNoContainer")
	@Produces("text/plain")
	@Consumes(
	{ "*" })
	public String getXMLTopicXMLNoContainer(@PathParam("id") final Integer id, @QueryParam("expand") final String expand, @QueryParam("includeTitle") final Boolean includeTitle)
	{
		assert id != null : "The id parameter can not be null";

		final String retValue = getXMLResource(Topic.class, new TopicV1Factory(), id, expand).getXMLWithNoContainer(includeTitle);
		return retValue;
	}

	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes(
	{ "application/json" })
	public void updateJSONTopic(@PathParam("id") final Integer id, final TopicV1 dataObject)
	{
		updateEntity(Topic.class, dataObject, new TopicV1Factory(), id);
	}

	@PUT
	@Path("/topic/put/xml/{id}")
	@Consumes(
	{ "application/xml" })
	public void updateXMLTopic(@PathParam("id") final Integer id, final TopicV1 dataObject)
	{
		updateEntity(Topic.class, dataObject, new TopicV1Factory(), id);
	}

	/* TAG FUNCTIONS */
	@GET
	@Path("/tag/get/json")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public BaseRestCollectionV1<TagV1> getJSONTags(@QueryParam("expand") final String expand)
	{
		return getJSONResources(Tag.class, new TagV1Factory(), TAGS_EXPANSION_NAME, expand);
	}

	@GET
	@Path("/tag/get/json/{id}")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public TagV1 getJSONTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Tag.class, new TagV1Factory(), id, expand);
	}

	@GET
	@Path("/tag/get/xml/{id}")
	@Produces("application/xml")
	@Consumes(
	{ "*" })
	public TagV1 getXMLTag(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Tag.class, new TagV1Factory(), id, expand);
	}
	
	@PUT
	@Path("/tag/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONTag(@PathParam("id") final Integer id, final TagV1 dataObject)
	{
		updateEntity(Tag.class, dataObject, new TagV1Factory(), id);
	}
	
	@PUT
	@Path("/tag/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLTag(@PathParam("id") final Integer id, final TagV1 dataObject)
	{
		updateEntity(Tag.class, dataObject, new TagV1Factory(), id);
	}

	/* CATEGORY FUNCTIONS */
	@GET
	@Path("/category/get/json")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public BaseRestCollectionV1<CategoryV1> getJSONCategories(@QueryParam("expand") final String expand)
	{
		return getJSONResources(Category.class, new CategoryV1Factory(), CATEGORIES_EXPANSION_NAME, expand);
	}

	@GET
	@Path("/category/get/json/{id}")
	@Produces("application/json")
	@Consumes(
	{ "*" })
	public CategoryV1 getJSONCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Category.class, new CategoryV1Factory(), id, expand);
	}

	@GET
	@Path("/category/get/xml/{id}")
	@Produces("application/xml")
	@Consumes(
	{ "*" })
	public CategoryV1 getXMLCategory(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Category.class, new CategoryV1Factory(), id, expand);
	}
	
	@PUT
	@Path("/category/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject)
	{
		updateEntity(Category.class, dataObject, new CategoryV1Factory(), id);
	}
	
	@PUT
	@Path("/category/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLCategory(@PathParam("id") final Integer id, final CategoryV1 dataObject)
	{
		updateEntity(Category.class, dataObject, new CategoryV1Factory(), id);
	}
	
	/* PROJECT FUNCTIONS */
	@GET
	@Path("/project/get/json")	
	@Produces("application/json")
	@Consumes({"*"})
	public BaseRestCollectionV1<ProjectV1> getJSONProjects(@QueryParam("expand") final String expand)
	{
		return getJSONResources(Project.class, new ProjectV1Factory(), TAGS_EXPANSION_NAME, expand);
	}
	
	@GET
	@Path("/project/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	public ProjectV1 getJSONProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getJSONResource(Project.class, new ProjectV1Factory(), id, expand);
	}
	
	@GET
	@Path("/project/get/xml/{id}")
	@Produces("application/xml")
	@Consumes({"*"})
	public ProjectV1 getXMLProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getXMLResource(Project.class, new ProjectV1Factory(), id, expand);
	}
	
	@PUT
	@Path("/project/put/json/{id}")
	@Consumes({"application/json"})
	public void updateJSONProject(@PathParam("id") final Integer id, final ProjectV1 dataObject)
	{
		updateEntity(Project.class, dataObject, new ProjectV1Factory(), id);
	}
	
	@PUT
	@Path("/project/put/xml/{id}")
	@Consumes({"application/xml"})
	public void updateXMLProject(@PathParam("id") final Integer id, final ProjectV1 dataObject)
	{
		updateEntity(Project.class, dataObject, new ProjectV1Factory(), id);
	}

}
