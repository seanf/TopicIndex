package com.redhat.topicindex.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.jboss.seam.Component;

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

		final Writer result = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(result);
		ex.printStackTrace(printWriter);

		System.out.println(result.toString());
		System.out.println("The above Exception was " + (isExpected ? "expected" : "unexptected!!!"));
		if (explaination != null)
			System.out.println(explaination);

		/*
		 * This function could be called in response to something as simple as a
		 * string that failed to be parsed as an integer, in which case the Seam
		 * transaction is still in effect. Or it could be called in response to
		 * an exception being thrown because of a persistence error, or from a
		 * thread that is not managed by Seam at all.
		 * 
		 * An easy way to deal with these situations is to spawn a new thread to
		 * save the exception data. Because it is a new thread, we can be sure
		 * that Seam won't try to manage the transactions, and we can safely
		 * manage our own.
		 */

		new Thread(new Runnable()
		{
			public void run()
			{
				TransactionManager transactionManager = null;
				EntityManager entityManager = null;

				try
				{
					final InitialContext initCtx = new InitialContext();
					transactionManager = (TransactionManager) initCtx.lookup("java:jboss/TransactionManager");
					final EntityManagerFactory entityManagerFactory = (EntityManagerFactory) initCtx.lookup("java:jboss/EntityManagerFactory");

					transactionManager.begin();

					entityManager = entityManagerFactory.createEntityManager();

					final SkynetException exception = new SkynetException();
					exception.setDescription(explaination);
					exception.setDetails(result.toString());
					exception.setExpected(isExpected);
					entityManager.persist(exception);
					entityManager.flush();

					transactionManager.commit();
				}
				catch (final Exception ex)
				{
					ex.printStackTrace();
					
					try
					{
						transactionManager.rollback();
					}
					catch(final Exception ex2)
					{
						ex2.printStackTrace();
					}
				}
				finally
				{
					if (entityManager != null)
						entityManager.close();
				}

			}
		}).start();
	}
}
