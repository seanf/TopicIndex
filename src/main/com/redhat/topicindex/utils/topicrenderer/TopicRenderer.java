package com.redhat.topicindex.utils.topicrenderer;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

public class TopicRenderer implements Runnable
{
	public static final String ENABLE_RENDERING_PROPERTY = "topicindex.rerenderTopic";
	private Integer topicId = null;
	private EntityManagerFactory entityManagerFactory = null;
	private TransactionManager transactionManager = null;

	public static TopicRenderer createNewInstance(final Integer topicId)
	{
		try
		{
			final InitialContext initCtx = new InitialContext();
			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			return new TopicRenderer(topicId, entityManagerFactory, transactionManager);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}

		return null;
	}

	public TopicRenderer(final Integer topicId, final EntityManagerFactory entityManagerFactory, final TransactionManager transactionManager)
	{
		this.topicId = topicId;
		this.entityManagerFactory = entityManagerFactory;
		this.transactionManager = transactionManager;
	}

	@Override
	public void run()
	{
		final String enableRendering = System.getProperty(ENABLE_RENDERING_PROPERTY);
		
		if (enableRendering == null || "true".equalsIgnoreCase(enableRendering))
		{
			EntityManager entityManager = null;
			
			try
			{			
				transactionManager.begin();
				
				/* get the state of the topic now */
				entityManager = this.entityManagerFactory.createEntityManager();
				final Topic initialTopic = entityManager.find(Topic.class, this.topicId);
				if (initialTopic != null)
				{
					/* detach the entity - it will not be saved to the database */
					entityManager.detach(initialTopic);
					/* render the html view */
					initialTopic.setRerenderRelatedTopics(false);
					initialTopic.renderXML(entityManager);
				}

				if (initialTopic != null)
				{
					final Topic updateTopic = entityManager.find(Topic.class, this.topicId);
					updateTopic.setTopicRendered(initialTopic.getTopicRendered());					
					entityManager.persist(updateTopic);
					entityManager.flush();
				}

				transactionManager.commit();
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
			}
			finally
			{
				if (entityManager != null)
					entityManager.close();
			}
		}
	}
}
