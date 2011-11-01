package com.redhat.topicindex.rest.marshal;


import org.jboss.resteasy.spi.interception.DecoratorProcessor;

import com.redhat.ecs.commonutils.ExceptionUtilities;

import javax.xml.bind.Marshaller;
import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;

public class PrettyProcessor implements DecoratorProcessor<Marshaller, Pretty>
 {
	@Override
	public Marshaller decorate(final Marshaller target, final Pretty annotation, final Class type, final Annotation[] annotations, final MediaType meditaType)
	{
		try
		{
			target.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
		
		return target;
	}

}
