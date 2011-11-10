package com.redhat.topicindex.session;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.entity.Topic;

@Scope(ScopeType.CONVERSATION)
@Name("temporaryTopicList")
public class TemporaryTopicList
{
	/** A temporary list of topics that can be used for special processing tasks */ 	
	private List<Topic> tempList = new ArrayList<Topic>();
	
	/** used by the action links */
	private Integer actionTopicID;

	public List<Topic> getTempList()
	{
		return tempList;
	}

	public void setTempList(final List<Topic> tempList)
	{
		this.tempList = tempList;
	}
	
	public void addToTempList()
	{
		try
		{
			if (actionTopicID != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, actionTopicID);

				if (!this.tempList.contains(topic))
					this.tempList.add(topic);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Topic");
		}
		finally
		{
			actionTopicID = null;
		}
	}

	public void removeFromTempList(final Integer topicId)
	{
		try
		{
			if (topicId != null)
			{
				final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
				final Topic topic = entityManager.find(Topic.class, topicId);

				if (this.tempList.contains(topic))
					this.tempList.remove(topic);
			}
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error retrieving a Topic");
		}
	}

	public String getTempTopicListURLParam()
	{
		String topicIds = "";
		for (final Topic topic : this.tempList)
		{
			if (topicIds.length() != 0)
				topicIds += ",";
			topicIds += topic.getTopicId();
		}

		return topicIds;
	}

	public Integer getActionTopicID()
	{
		return actionTopicID;
	}

	public void setActionTopicID(Integer actionTopicID)
	{
		this.actionTopicID = actionTopicID;
	}
}
