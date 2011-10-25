package com.redhat.topicindex.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Lifecycle;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

public class TopicRenderer implements Runnable
{
	private Integer topicId = null;

	public TopicRenderer(final Integer topicId)
	{
		this.topicId = topicId;
	}

	@Override
	public void run()
	{
		EntityTransaction tx = null;
		EntityManager entityManager = null;

		try
		{
			Lifecycle.beginCall();
			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) Component.getInstance("EntityManagerFactory");
			entityManager = entityManagerFactory.createEntityManager();
			final Topic topic = entityManager.find(Topic.class, this.topicId);

			if (topic != null)
			{
				/*
				 * Do this outside a transaction, because we don't particularly
				 * want to lock the topic in a transaction while we transform
				 * the XML.
				 */
				topic.renderXML();

				tx = entityManager.getTransaction();
				tx.begin();

				entityManager.persist(topic);

				tx.commit();
			}
		}
		catch (final Exception ex)
		{
			if (tx != null && tx.isActive())
				tx.rollback();

			ExceptionUtilities.handleException(ex);
		}
		finally
		{
			try
			{
				if (entityManager != null)
					entityManager.close();
				Lifecycle.endCall();
			}
			catch (final Exception ex)
			{
				ExceptionUtilities.handleException(ex);
			}
		}
	}
}
