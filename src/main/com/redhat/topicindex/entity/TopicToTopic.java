package com.redhat.topicindex.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonthread.WorkQueue;
import com.redhat.topicindex.utils.topicrenderer.TopicRenderer;

@Audited
@Entity
@Table(name = "TopicToTopic", uniqueConstraints = @UniqueConstraint(columnNames =
{ "MainTopicID", "RelatedTopicID" }))
public class TopicToTopic implements java.io.Serializable
{
	private static final long serialVersionUID = -589601408520832737L;
	private Integer topicToTopicId;
	private Topic mainTopic;
	private Topic relatedTopic;

	public TopicToTopic()
	{
	}

	public TopicToTopic(final Topic mainTopic, final Topic relatedTopic)
	{
		this.mainTopic = mainTopic;
		this.relatedTopic = relatedTopic;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicToTopicID", unique = true, nullable = false)
	public Integer getTopicToTopicId()
	{
		return this.topicToTopicId;
	}

	public void setTopicToTopicId(final Integer topicToTopicId)
	{
		this.topicToTopicId = topicToTopicId;
	}

	@ManyToOne
	@JoinColumn(name = "MainTopicID", nullable = false)
	@NotNull
	public Topic getMainTopic()
	{
		return this.mainTopic;
	}

	public void setMainTopic(final Topic mainTopic)
	{
		this.mainTopic = mainTopic;
	}

	@ManyToOne
	@JoinColumn(name = "RelatedTopicID", nullable = false)
	@NotNull
	public Topic getRelatedTopic()
	{
		return this.relatedTopic;
	}

	public void setRelatedTopic(final Topic relatedTopic)
	{
		this.relatedTopic = relatedTopic;
	}

	/**
	 * When a TopicToTopic mapping is created or destroyed, we rerender the main
	 * topic to update the generic injection lists.
	 */
	@PostPersist
	@PostRemove
	private void reRender()
	{
		if (this.mainTopic != null)
		{
			WorkQueue.getInstance().execute(TopicRenderer.createNewInstance(mainTopic.getTopicId()));
		}
	}

}
