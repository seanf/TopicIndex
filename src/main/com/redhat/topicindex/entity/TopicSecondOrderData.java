package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;

@Audited
@Entity
@Table(name = "TopicSecondOrderData")
public class TopicSecondOrderData implements java.io.Serializable
{
	private static final long serialVersionUID = 3393132758855818345L;
	private Integer topicId;
	private String topicHTMLView;
	private String topicXMLErrors;
	
	public TopicSecondOrderData()
	{
	}
	
	public TopicSecondOrderData(final Integer topicId)
	{
		this.topicId = topicId;
	}
	
	@Id
	@Column(name = "TopicID")
    public Integer getTopicID()
	{
		return this.topicId;
	}
	
	public void setTopicID(final Integer topicId)
	{
		this.topicId = topicId;
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
