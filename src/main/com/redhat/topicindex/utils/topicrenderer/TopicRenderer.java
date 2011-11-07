package com.redhat.topicindex.utils.topicrenderer;

import java.util.ArrayList;
import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.w3c.dom.Document;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.ecs.commonutils.XMLUtilities;
import com.redhat.topicindex.entity.Topic;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.docbookbuilding.XMLPreProcessor;

public class TopicRenderer implements Runnable
{

	/**
	 * The maximum amount of time (in seconds) to wait for the entity
	 * transaction to finish
	 */
	private static final int MAX_WAIT = 5;
	/** The ID of the topic to rerender */
	private Integer topicId = null;
	private EntityManagerFactory entityManagerFactory = null;
	private TransactionManager transactionManager = null;
	private Transaction transaction = null;

	public static TopicRenderer createNewInstance(final Integer topicId)
	{
		return createNewInstance(topicId, null);
	}

	public static TopicRenderer createNewInstance(final Integer topicId, final Transaction transaction)
	{
		try
		{
			final InitialContext initCtx = new InitialContext();
			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			final TransactionManager transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
			return new TopicRenderer(topicId, entityManagerFactory, transactionManager, transaction);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}

		return null;
	}

	public static String renderXML(final EntityManager entityManager, final Topic topic)
	{
		System.out.println("START Topic.renderXML() ID: " + topic.getTopicId());

		try
		{
			final Document doc = XMLUtilities.convertStringToDocument(topic.getTopicXML());
			if (doc != null)
			{
				/*
				 * create a collection of the tags that make up the topics types
				 * that will be included in generic injection points
				 */
				final List<Pair<Integer, String>> topicTypeTagDetails = new ArrayList<Pair<Integer, String>>();
				topicTypeTagDetails.add(Pair.newPair(Constants.TASK_TAG_ID, Constants.TASK_TAG_NAME));
				topicTypeTagDetails.add(Pair.newPair(Constants.REFERENCE_TAG_ID, Constants.REFERENCE_TAG_NAME));
				topicTypeTagDetails.add(Pair.newPair(Constants.CONCEPT_TAG_ID, Constants.CONCEPT_TAG_NAME));
				topicTypeTagDetails.add(Pair.newPair(Constants.CONCEPTUALOVERVIEW_TAG_ID, Constants.CONCEPTUALOVERVIEW_TAG_NAME));

				final ArrayList<Integer> customInjectionIds = new ArrayList<Integer>();
				XMLPreProcessor.processInjections(true, topic, customInjectionIds, doc, null, null);
				XMLPreProcessor.processGenericInjections(true, topic, doc, customInjectionIds, topicTypeTagDetails, null, null);
				XMLPreProcessor.processInternalImageFiles(doc);

				XMLPreProcessor.processTopicContentFragments(topic, doc, null);
				XMLPreProcessor.processTopicTitleFragments(topic, doc, null);

				/* render the topic html */
				final String processedXML = XMLUtilities.convertDocumentToString(doc, Constants.XML_ENCODING);
				final String processedXMLWithDocType = XMLPreProcessor.processDocumentType(processedXML);
				final String retValue = (XMLRenderer.transformDocbook(entityManager, processedXMLWithDocType));
				return retValue;
			}
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		finally
		{
			System.out.println("FINISH Topic.renderXML() ID: " + topic.getTopicId());
		}

		return Constants.XSL_ERROR_TEMPLATE;
	}

	public TopicRenderer(final Integer topicId, final EntityManagerFactory entityManagerFactory, final TransactionManager transactionManager, final Transaction transaction)
	{
		this.topicId = topicId;
		this.entityManagerFactory = entityManagerFactory;
		this.transactionManager = transactionManager;
		this.transaction = transaction;
	}

	@Override
	public void run()
	{
		final String enableRendering = System.getProperty(Constants.ENABLE_RENDERING_PROPERTY);

		if (enableRendering == null || "true".equalsIgnoreCase(enableRendering))
		{
			/*
			 * Wait for the transaction associated with the main entity to
			 * finish
			 */
			boolean renderTopic = transaction != null ? false : true;
			try
			{
				if (transaction != null)
				{
					for (int i = 0; i < MAX_WAIT; ++i)
					{
						final int status = transaction.getStatus();

						if (status == Status.STATUS_COMMITTED)
						{
							renderTopic = true;
							break;
						}

						if (status == Status.STATUS_NO_TRANSACTION || status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_UNKNOWN)
						{
							return;
						}

						Thread.sleep(1000);
					}
				}
			}
			catch (final Exception ex)
			{
				ExceptionUtilities.handleException(ex);
			}

			/*
			 * There was a problem with the transaction, so don't renrender the
			 * topic
			 */
			if (!renderTopic)
				return;

			EntityManager entityManager = null;

			try
			{
				transactionManager.begin();

				/* get the state of the topic now */
				entityManager = this.entityManagerFactory.createEntityManager();
				String renderedXML = null;
				final Topic initialTopic = entityManager.find(Topic.class, this.topicId);
				if (initialTopic != null)
				{
					/*
					 * detach the entity - it will not be saved to the database
					 */
					entityManager.detach(initialTopic);
					/* render the html view */
					renderedXML = renderXML(entityManager, initialTopic);
				}

				/*
				 * And now get the state of the topic after it has been
				 * rendered. Rendering can be time consuming, so we will only
				 * update the TopicRendered field (and leave the others alone)
				 * with what we have just rendered to lessen the impact of
				 * database race conditions (such as when the topic has been
				 * edited since when we started the rendering process and when
				 * we finished).
				 */
				if (renderedXML != null)
				{
					final Topic updateTopic = entityManager.find(Topic.class, this.topicId);
					updateTopic.setRerenderRelatedTopics(false);
					updateTopic.setTopicRendered(renderedXML);
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
