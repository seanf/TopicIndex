package com.redhat.topicindex.utils;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import com.redhat.topicindex.entity.SkynetException;

public class SkynetExceptionUtilities
{
	/**
	 * A standard function to deal with exceptions
	 * 
	 * @param ex
	 */
	public static void handleException(final Exception ex, final boolean isExpected, final String explaination)
	{
		if (ex == null)
			return;
		
		System.out.println(ex.toString());
		System.out.println("The above Exception was " + (isExpected ? "expected" : "unexptected!!!"));
		if (explaination != null)
			System.out.println(explaination);

		/*
		 * Save the exception details in the database. We don't know if this is
		 * going to be called from a Seam managed component, or a thread, so get
		 * a new EntityManagerfactory just in case.
		 */
		TransactionManager transactionManager = null;
		EntityManager entityManager = null;

		try
		{
			final InitialContext initCtx = new InitialContext();
			final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");
			transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");

			transactionManager.begin();

			entityManager = entityManagerFactory.createEntityManager();
			
			final SkynetException exception = new SkynetException();
			exception.setDescription(explaination);
			exception.setDetails(ex.toString());
			exception.setExpected(isExpected);
			entityManager.persist(exception);

			transactionManager.commit();
		}
		catch (final Exception ex1)
		{
			System.out.println("Oh the irony - the Exception handler has thrown an exception");

			try
			{
				transactionManager.rollback();
			}
			catch (final Exception ex2)
			{
				System.out.println("Oh the irony - the Exception handler has thrown an exception in response to an exception");
			}
		}
		finally
		{
			if (entityManager != null)
				entityManager.close();
		}
	}
}
