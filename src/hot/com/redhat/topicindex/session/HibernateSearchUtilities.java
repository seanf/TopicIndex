package com.redhat.topicindex.session;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

@Name("hibernateSearchUtilities")
public class HibernateSearchUtilities 
{
	public void indexEntireDatabase()
	{
		try
		{
			final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
			final Session session = (Session)entityManager.getDelegate();
			final FullTextSession fullTextSession = Search.getFullTextSession(session);
			fullTextSession.createIndexer(Topic.class).startAndWait();
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}
}
