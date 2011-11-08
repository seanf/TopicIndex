package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;

@Audited
@Entity
@Table(name = "TopicSecondOrderData")
public class TopicSecondOrderData implements java.io.Serializable
{
	private static final long serialVersionUID = 3393132758855818345L;
	private Integer topicSecondOrderDataID;
	private Topic topic;
	private String topicHTMLView;
	private String topicXMLErrors;
	
	public TopicSecondOrderData()
	{
	}
	
	public TopicSecondOrderData(final Topic topic)
	{
		this.topic = topic;
	}
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TopicSecondOrderDataID", unique = true, nullable = false)
	public Integer getTopicSecondOrderDataID()
	{
		return this.topicSecondOrderDataID;
	}

	public void setTopicSecondOrderDataID(final Integer topicSecondOrderDataID)
	{
		this.topicSecondOrderDataID = topicSecondOrderDataID;
	}
	
	@OneToOne
	@JoinColumn(name="TopicID")
    public Topic getTopic()
	{
		return this.topic;
	}
	
	public void setTopic(final Topic topic)
	{
		this.topic = topic;
	}
	
	// @Column(name = "TopicHTMLView", length = 65535)
	@Column(name = "TopicHTMLView", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicHTMLView()
	{
		return this.topicHTMLView;
	}

	public void setTopicHTMLView(final String topicHTMLView)
	{
		this.topicHTMLView = topicHTMLView;
	}
	
	// @Column(name = "TopicXMLErrors", length = 65535)
	@Column(name = "TopicXMLErrors", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTopicXMLErrors()
	{
		return this.topicXMLErrors;
	}

	public void setTopicXMLErrors(final String topicXMLErrors)
	{
		this.topicXMLErrors = topicXMLErrors;
	}
}
