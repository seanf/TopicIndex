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

				entityManager = this.entityManagerFactory.createEntityManager();
				final Topic topic = entityManager.find(Topic.class, this.topicId);

				if (topic != null)
				{
					topic.setRerenderRelatedTopics(false);
					topic.renderXML(entityManager);
					entityManager.persist(topic);
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
