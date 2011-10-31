package com.redhat.topicindex.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;

import com.google.gson.Gson;
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

	@PUT
	@Path("/topic/put/json/{id}")
	public Response updateTopic(@PathParam("id") final Integer id, final String json)
	{
		assert id != null : "The id parameter can not be null";
		assert json != null : "The json parameter can not be null";

		Topic entity = null;
		TopicV1 topicV1 = null;
		EntityManager entityManager = null;
		
		try
		{
			entityManager = (EntityManager) Component.getInstance("entityManager");
			if (entityManager == null)
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		catch (final Exception ex)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
		try
		{
			entity = entityManager.find(Topic.class, id);
			if (entity == null)
				return Response.status(Response.Status.NOT_FOUND).build();
		}
		catch (final Exception ex)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
		try
		{
			topicV1 = new Gson().fromJson(json, TopicV1.class);
			if (topicV1 == null)
				return Response.status(Response.Status.BAD_REQUEST).build();
		}
		catch (final Exception ex)
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
		
		assert entityManager != null : "entityManager should nto be null";
		assert entity != null : "entity should nto be null";
		assert topicV1 != null : "topicV1 should nto be null";
		
		try
		{
			topicV1.sync(entity);
			entityManager.persist(entity);
			entityManager.flush();
		}
		catch (final Exception ex)
		{
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		
		return Response.ok().build();

	}
}
