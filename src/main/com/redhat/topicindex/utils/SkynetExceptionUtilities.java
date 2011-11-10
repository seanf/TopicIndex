package com.redhat.topicindex.utils;

public class SkynetExceptionUtilities
{
	/**
	 * A standard function to deal with exceptions
	 * @param ex
	 */
	public static void handleException(final Exception ex, final boolean isExpected, final String explaination)
	{
		System.out.println(ex.toString());
		System.out.println("The above Exception was " + (isExpected ? "expected" : "unexptected!!!"));
		System.out.println(explaination);
	}
}
