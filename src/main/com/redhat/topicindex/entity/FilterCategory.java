package com.redhat.topicindex.entity;

// Generated Apr 14, 2011 12:17:30 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.NotNull;

/**
 * FilterCategory generated by hbm2java
 */
@Entity
@Audited
@Table(name = "FilterCategory", catalog = "Skynet")
public class FilterCategory implements java.io.Serializable 
{
	private static final long serialVersionUID = -9199839815820171298L;
	private Integer filterCategoryId;
	private Filter filter;
	private Project project;
	private Category category;
	private int categoryState;

	public FilterCategory() {
	}

	public FilterCategory(Filter filter, Category category, int categoryState) {
		this.filter = filter;
		this.category = category;
		this.categoryState = categoryState;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "FilterCategoryID", unique = true, nullable = false)
	public Integer getFilterCategoryId() {
		return this.filterCategoryId;
	}

	public void setFilterCategoryId(Integer filterCategoryId) {
		this.filterCategoryId = filterCategoryId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FilterID", nullable = false)
	@NotNull
	public Filter getFilter() {
		return this.filter;
	}

	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "CategoryID", nullable = false)
	@NotNull
	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	@Column(name = "CategoryState", nullable = false)
	public int getCategoryState() {
		return this.categoryState;
	}

	public void setCategoryState(int categoryState) {
		this.categoryState = categoryState;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ProjectID")
	public Project getProject()
	{
		return project;
	}

	public void setProject(final Project project)
	{
		this.project = project;
	}

}
