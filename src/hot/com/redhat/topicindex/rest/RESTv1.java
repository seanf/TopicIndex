package com.redhat.topicindex.rest;

import java.util.List;

import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.TransactionManager;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.InternalServerErrorException;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.RESTDataObjectCollectionFactory;
import com.redhat.topicindex.rest.factory.RESTDataObjectFactory;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

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
	
	protected <T, U> BaseRestCollectionV1<T> getXMLResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand)
	{
		return getResources(type, dataObjectFactory, expandName, expand, XML_URL);
	}
	
	protected <T, U> BaseRestCollectionV1<T> getJSONResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand)
	{
		return getResources(type, dataObjectFactory, expandName, expand, JSON_URL);
	}
	
	protected <T, U> BaseRestCollectionV1<T> getResources(final Class<U> type, final RESTDataObjectFactory<T, U> dataObjectFactory, final String expandName, final String expand, final String dataType)
	{
		assert type != null : "The type parameter can not be null";
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
			
			final CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();			
			final CriteriaQuery<U> criteriaQuery = criteriaBuilder.createQuery(type);
			
			criteriaQuery.from(type);
			
			final TypedQuery<U> query = entityManager.createQuery(criteriaQuery);
			
			final List<U> result = query.getResultList();

			final BaseRestCollectionV1<T> retValue = new RESTDataObjectCollectionFactory<T, U>().create(dataObjectFactory, result, expandName, dataType, new ExpandData(expand), getBaseUrl());

			return retValue;
		}
		catch (final NamingException ex)
		{
			throw new InternalServerErrorException("Could not find the EntityManagerFactory");
		}
	}
	
	protected BaseRestCollectionV1<TopicV1> getJSONTopicsFromQuery(final MultivaluedMap<String, String> queryParams, final TopicV1Factory dataObjectFactory, final String expandName, final String expand)
	{
		return getTopicsFromQuery(queryParams, dataObjectFactory, expandName, expand, JSON_URL);
	}
	
	protected BaseRestCollectionV1<TopicV1> getTopicsFromQuery(final MultivaluedMap<String, String> queryParams, final TopicV1Factory dataObjectFactory, final String expandName, final String expand, final String dataType)
	{
		assert dataObjectFactory != null : "The dataObjectFactory parameter can not be null";
		assert uriInfo != null : "uriInfo can not be null";

		try
		{
			final InitialContext initCtx = new InitialContext();

			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			if (entityManagerFactory == null)
				throw new InternalServerErrorException("Could not find the EntityManagerFactory");

			final EntityManager entityManager = entityManagerFactory.createEntityManager();
			if (entityManager == null)
				throw new InternalServerErrorException("Could not create an EntityManager");
			
			// build up a Filter object from the URL variables
			final Filter filter = EntityUtilities.populateFilter(
					queryParams,
					Constants.FILTER_ID, 
					Constants.MATCH_TAG, 
					Constants.CATEORY_INTERNAL_LOGIC, 
					Constants.CATEORY_EXTERNAL_LOGIC);	
			
			final String query = filter.buildQuery(true);
			
			final List<Topic> result = entityManager.createQuery(Topic.SELECT_ALL_QUERY + query).getResultList();

			final BaseRestCollectionV1<TopicV1> retValue = new RESTDataObjectCollectionFactory<TopicV1, Topic>().create(dataObjectFactory, result, expandName, dataType, new ExpandData(expand), getBaseUrl());

			return retValue;
		}
		catch (final NamingException ex)
		{
			throw new InternalServerErrorException("Could not find the EntityManagerFactory");
		}
	}
}
