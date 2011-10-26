package com.redhat.topicindex.utils;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Lifecycle;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

public class TopicRenderer implements Runnable
{
	private Integer topicId = null;
	private EntityManagerFactory entityManagerFactory = null;
	private TransactionManager transactionManager = null;

	public TopicRenderer(final Integer topicId, final EntityManagerFactory entityManagerFactory, final TransactionManager transactionManager)
	{
		this.topicId = topicId;
		this.entityManagerFactory = entityManagerFactory;
		this.transactionManager = transactionManager;
	}

	@Override
	public void run()
	{
		try
		{
			transactionManager.begin();
						
			final EntityManager entityManager = this.entityManagerFactory.createEntityManager();
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
	}
}
