package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("exceptionHome")
public class ExceptionHome extends EntityHome<Exception>
{

	public void setExceptionExceptionId(Integer id)
	{
		setId(id);
	}

	public Integer getExceptionExceptionId()
	{
		return (Integer) getId();
	}

	@Override
	protected Exception createInstance()
	{
		Exception exception = new Exception();
		return exception;
	}

	public void load()
	{
		if (isIdDefined())
		{
			wire();
		}
	}

	public void wire()
	{
		getInstance();
	}

	public boolean isWired()
	{
		return true;
	}

	public Exception getDefinedInstance()
	{
		return isIdDefined() ? getInstance() : null;
	}

}
