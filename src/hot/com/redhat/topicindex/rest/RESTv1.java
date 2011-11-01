package com.redhat.topicindex.rest;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.InternalServerErrorException;
import org.jboss.seam.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.RESTDataObjectFactory;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.utils.Constants;

public class RESTv1
{
	public static final String TOPICS_EXPANSION_NAME = "topics";
	public static final String TAGS_EXPANSION_NAME = "tags";
	public static final String CATEGORIES_EXPANSION_NAME = "categories";
	public static final String TOPIC_INCOMING_RELATIONSHIPS_EXPANSION_NAME = "incomingRelationships";
	public static final String TOPIC_OUTGOING_RELATIONSHIPS_EXPANSION_NAME = "outgoingRelationships";
	public static final String TOPIC_TWO_WAY_RELATIONSHIPS_EXPANSION_NAME = "twoWayRelationships";

	public static final String TOPIC_URL_NAME = "topic";
	public static final String TAG_URL_NAME = "tag";
	public static final String CATEGORY_URL_NAME = "category";

	public static final String JSON_URL = "json";
	public static final String XML_URL = "json";

	private @Context
	UriInfo uriInfo;

	protected String getBaseUrl()
	{
		final String fullPath = uriInfo.getAbsolutePath().toString();
		final int index = fullPath.indexOf(Constants.BASE_REST_PATH);
		if (index != -1)
			return fullPath.substring(0, index + Constants.BASE_REST_PATH.length());

		return null;
	}
	
	protected <T, U> void updateEntity(final Class<U> type, final T dataObject, final RESTDataObjectFactory<T, U> factory, final Object id)
	{
		assert id != null : "The id parameter can not be null";
		assert dataObject != null : "The dataObject parameter can not be null";

		TransactionManager transactionManager = null;
		EntityManager entityManager = null;
		
		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			if (transactionManager == null)
				throw new InternalServerErrorException("Could not find the TransactionManager");
			
			assert transactionManager != null : "transactionManager should not be null";
			assert entityManagerFactory != null : "entityManagerFactory should not be null";

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			assert entityManager != null : "entityManager should not be null";

			final U entity = entityManager.find(type, id);
			if (entity == null)
				throw new BadRequestException("No entity was found with the primary key " + id);

			assert entity != null : "entity should not be null";

			factory.sync(entity, dataObject);
			entityManager.persist(entity);
			entityManager.flush();
			transactionManager.commit();
		}
		catch (final Failure ex)
		{
			ExceptionUtilities.handleException(ex);
			throw ex;
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
			
			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				ExceptionUtilities.handleException(ex2);
			}

			throw new InternalServerErrorException("There was an error saving the entity");
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}

	protected <T, U> T getJSONResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String expand)
	{
		return getResource(type, dataObjectFactory, id, expand, JSON_URL);
	}
	
	protected <T, U> T getXMLResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String expand)
	{
		return getResource(type, dataObjectFactory, id, expand, XML_URL);
	}
	
	private <T, U> T getResource(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final Object id, final String expand, final String dataType)
	{
		assert type != null : "The type parameter can not be null";
		assert id != null : "The id parameter can not be null";
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");

			final U entity = entityManager.find(type, id);
			if (entity == null)
				throw new BadRequestException("No entity was found with the primary key " + id);

			/* create the REST representation of the topic */
			final T restRepresentation = dataObjectFactory.create(entity, this.getBaseUrl(), dataType, expand);

			return restRepresentation;
		}
		catch (final NamingException ex)
		{
			throw new InternalServerErrorException("Could not find the EntityManagerFactory");
		}
	}
}
