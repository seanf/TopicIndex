package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
@Audited
@Table(name = "Category", catalog = "Skynet", uniqueConstraints = @UniqueConstraint(columnNames = {"CategoryName" }))
public class Category implements java.io.Serializable, Comparable<Category>
{
	public static final String SELECT_ALL_QUERY = "select category from Category category";
	
	private static final long serialVersionUID = -8650833773254246211L;
	private Integer categoryId;
	private String categoryName;
	private String categoryDescription;
	private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
	private Integer categorySort;
	private boolean mutuallyExclusive;

	public Category() {
	}

	public Category(String categoryName) {
		this.categoryName = categoryName;
	}

	public Category(
		final String categoryName, 
		final String categoryDescription,
		final Set<TagToCategory> tagToCategories) 
	{
		this.categoryName = categoryName;
		this.categoryDescription = categoryDescription;
		this.tagToCategories = tagToCategories;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy="category", cascade=CascadeType.ALL)
	public Set<TagToCategory> getTagToCategories() 
	{
		return this.tagToCategories;
	}

	public void setTagToCategories(Set<TagToCategory> tagToCategories) 
	{
		this.tagToCategories = tagToCategories;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "CategoryID", unique = true, nullable = false)
	public Integer getCategoryId() {
		return this.categoryId;
	}

	public void setCategoryId(Integer categoryId) {
		this.categoryId = categoryId;
	}

	//@Column(name = "CategoryName", nullable = false, length = 65535)
	@Column(name = "CategoryName", nullable = false, columnDefinition="TEXT")
	@NotNull
	@Length(max = 65535)
	public String getCategoryName() {
		return this.categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	@Column(name = "CategoryDescription", length = 2048)
	@Length(max = 2048)
	public String getCategoryDescription() {
		return this.categoryDescription;
	}

	public void setCategoryDescription(String categoryDescription) {
		this.categoryDescription = categoryDescription;
	}
	
	@Column(name = "CategorySort")
	public Integer getCategorySort() {
		return this.categorySort;
	}

	public void setCategorySort(Integer categorySort) {
		this.categorySort = categorySort;
	}
	
	@Column(name = "MutuallyExclusive", nullable=false)
	@NotNull
	public boolean isMutuallyExclusive() {
		return this.mutuallyExclusive;
	}

	public void setMutuallyExclusive(boolean mutuallyExclusive) {
		this.mutuallyExclusive = mutuallyExclusive;
	}

	public int compareTo(Category o) 
	{
		if (o == null)
			return 1;
		
		if (o.getCategorySort() == null && this.getCategorySort() == null)
			return 0;
		
		if (o.getCategorySort() == null)
			return 1;
		
		if (this.getCategorySort() == null)
			return -1;
		
		return this.getCategorySort().compareTo(o.getCategorySort());
	}
	
	@Transient
	public List<Tag> getTagsInProject(final Project project)
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		
		for (final TagToCategory tagToCategory : this.tagToCategories)
		{
			final Tag tag = tagToCategory.getTag();
			if (tag.isInProject(project))
				retValue.add(tag);
		}
		
		return retValue;
	}

}
