package com.redhat.topicindex.utils.structures;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ecs.commonstructures.Pair;
import com.redhat.topicindex.entity.Topic;

public class GenericInjectionPointDatabase
{
	private List<GenericInjectionPoint> injectionPoints = new ArrayList<GenericInjectionPoint>();
	
	public GenericInjectionPoint getInjectionPoint(final Pair<Integer, String> tagDetails)
	{
		return getInjectionPoint(tagDetails.getFirst());
	}
	
	public GenericInjectionPoint getInjectionPoint(final Integer tagId)
	{
		for (final GenericInjectionPoint genericInjectionPoint : injectionPoints)
		{
			if (genericInjectionPoint.getCategoryIDAndName().getFirst().equals(tagId))
				return genericInjectionPoint;
		}
		
		return null;
	}
	
	public void addInjectionTopic(final Pair<Integer, String> tagDetails, final Topic topic)
	{
		GenericInjectionPoint genericInjectionPoint = getInjectionPoint(tagDetails);
		if (genericInjectionPoint == null)
		{
			genericInjectionPoint = new GenericInjectionPoint(tagDetails);
			injectionPoints.add(genericInjectionPoint);
		}
		genericInjectionPoint.addTopic(topic);
	}

	public List<GenericInjectionPoint> getInjectionPoints()
	{
		return injectionPoints;
	}

	public void setInjectionPoints(List<GenericInjectionPoint> injectionPoints)
	{
		this.injectionPoints = injectionPoints;
	}
}
