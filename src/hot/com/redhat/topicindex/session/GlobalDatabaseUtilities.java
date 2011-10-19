package com.redhat.topicindex.session;

import java.util.List;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

@Name("globalDatabaseUtilities")
public class GlobalDatabaseUtilities 
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
	
	public void renderAllTopics()
	{
		try
		{
			final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");
			final List<Topic> topics = entityManager.createQuery(Topic.SELECT_ALL_QUERY).getResultList();
			
			final int total = topics.size();
			int current = 0;
			
			for (final Topic topic : topics)
			{
				++current;
				final int progress = (int)((float)current / (float)total * 100.0f);
				System.out.println("Rerender progress: Topic " + current + " of " + total + " (" + progress + "%)");
				
				topic.validate();
				entityManager.persist(topic);
				entityManager.flush();
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}
}
