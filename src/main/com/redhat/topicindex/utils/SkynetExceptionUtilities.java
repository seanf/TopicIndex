package com.redhat.topicindex.utils;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
		
		System.out.println(ex.toString());
		System.out.println("The above Exception was " + (isExpected ? "expected" : "unexptected!!!"));
		if (explaination != null)
			System.out.println(explaination);

		try
		{
			final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
			
			final SkynetException exception = new SkynetException();
			exception.setDescription(explaination);
			exception.setDetails(ex.toString());
			exception.setExpected(isExpected);
			entityManager.persist(exception);
			entityManager.flush();
		}
		catch (final Exception ex1)
		{
			System.out.println("Oh the irony - the Exception handler has thrown an exception");
		}
	}
}
