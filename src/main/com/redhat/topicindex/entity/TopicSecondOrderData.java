package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;

@Audited
@Entity
@Table(name = "TopicSecondOrderData", uniqueConstraints = @UniqueConstraint(columnNames = { "TopicID" }))
public class TopicSecondOrderData
{
	private Topic topic;
	private Integer topicSecondOrderDataID;
	private String topicHTMLView;
	private String topicXMLErrors;
	
	public TopicSecondOrderData()
	{
	}
	
	public TopicSecondOrderData(final Topic topic, final Integer topicSecondOrderDataID)
	{
		this.topic = topic;
		this.topicSecondOrderDataID = topicSecondOrderDataID;
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
	
	@OneToOne(mappedBy="TopicID")
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
