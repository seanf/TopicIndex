package com.redhat.topicindex.rest;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.TransactionManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.jboss.seam.Component;

import com.google.gson.Gson;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.rest.sharedinterface.TopicRESTInterfaceV1;

@Path("/1")
public class TopicRESTv1 extends RESTv1 implements TopicRESTInterfaceV1
{
	@GET
	@Path("/topic/get/json/{id}")
	public Response getTopic(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";

		return getResource(Topic.class, new TopicV1Factory(), id, "application/json", expand);
	}

	@PUT
	@Path("/topic/put/json/{id}")
	@Consumes(
	{ "application/json", "text/plain" })
	public Response updateTopic(@PathParam("id") final Integer id, final String json)
	{
		assert id != null : "The id parameter can not be null";
		assert json != null : "The json parameter can not be null";

		Topic entity = null;
		TopicV1 topicV1 = null;
		EntityManager entityManager = null;
		TransactionManager transactionManager = null;

		try
		{
			entityManager = (EntityManager) Component.getInstance("entityManager");
			if (entityManager == null)
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		try
		{
			final InitialContext initCtx = new InitialContext();
			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}

		try
		{
			try
			{
				entity = entityManager.find(Topic.class, id);
				if (entity == null)
					return Response.status(Response.Status.NOT_FOUND).build();
			}
			catch (final Exception ex)
			{
				ExceptionUtilities.handleException(ex);
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
				ExceptionUtilities.handleException(ex);
				return Response.status(Response.Status.BAD_REQUEST).build();
			}

			assert transactionManager != null : "transactionManager should not be null";
			assert entityManager != null : "entityManager should not be null";
			assert entity != null : "entity should not be null";
			assert topicV1 != null : "topicV1 should not be null";

			try
			{
				new TopicV1Factory().sync(entity, topicV1);
				transactionManager.begin();
				entityManager.persist(entity);
				transactionManager.commit();
			}
			catch (final Exception ex)
			{
				try
				{
					transactionManager.rollback();
				}
				catch (final Exception ex2)
				{
					ExceptionUtilities.handleException(ex2);
				}
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}

			return Response.ok().build();
		}
		finally
		{
			entityManager.close();
		}
	}
}
