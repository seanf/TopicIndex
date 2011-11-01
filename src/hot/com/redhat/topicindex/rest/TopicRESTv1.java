package com.redhat.topicindex.rest;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.Failure;
import org.jboss.resteasy.spi.InternalServerErrorException;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.rest.entities.TopicV1;
import com.redhat.topicindex.rest.factory.TopicV1Factory;
import com.redhat.topicindex.rest.marshal.Pretty;
import com.redhat.topicindex.rest.sharedinterface.TopicRESTInterfaceV1;

@Path("/1")
public class TopicRESTv1 extends RESTv1 implements TopicRESTInterfaceV1<TopicV1>
{
	@GET
	@Path("/topic/get/json/{id}")	
	@Produces("application/json")
	@Consumes({"*"})
	@Pretty
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

	public void updateJSONTopic(@PathParam("id") final Integer id, final TopicV1 dataObject)
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

			final Topic entity = entityManager.find(Topic.class, id);
			if (entity == null)
				throw new BadRequestException("No entity was found with the primary key " + id);

			assert entity != null : "entity should not be null";

			new TopicV1Factory().sync(entity, dataObject);
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

	
}
