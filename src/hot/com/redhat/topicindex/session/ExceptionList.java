package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("exceptionList")
public class ExceptionList extends EntityQuery<Exception>
{

	private static final String EJBQL = "select exception from Exception exception";

	private static final String[] RESTRICTIONS =
	{ "lower(exception.details) like lower(concat(#{exceptionList.exception.details},'%'))", "lower(exception.description) like lower(concat(#{exceptionList.exception.description},'%'))", };

	private Exception exception = new Exception();

	public ExceptionList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public Exception getException()
	{
		return exception;
	}
}
