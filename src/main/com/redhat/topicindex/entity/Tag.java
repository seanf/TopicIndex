package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.UniqueConstraint;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static javax.persistence.GenerationType.IDENTITY;
import static org.hamcrest.Matchers.equalTo;

import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.Component;

@Entity
@Audited
@Table(name = "Tag", catalog = "Skynet", uniqueConstraints = @UniqueConstraint(columnNames = {"TagName" })) 
public class Tag implements java.io.Serializable
{
	public static final String SELECT_ALL_QUERY = "select tag from Tag tag";
	private static final long serialVersionUID = 2841080567638275194L;
	private Integer tagId;
	private String tagName;
	private String tagDescription;
	private Set<TopicToTag> topicToTags = new HashSet<TopicToTag>(0);
	private Set<Tag> excludedTags = new HashSet<Tag>(0);
	private Set<TagToCategory> tagToCategories = new HashSet<TagToCategory>(0);
	private Set<TagToTag> childrenTagToTags = new HashSet<TagToTag>(0);
	private Set<TagToTag> parentTagToTags = new HashSet<TagToTag>(0);
	private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);

	public Tag()
	{
	}

	public Tag(String tagName)
	{
		this.tagName = tagName;
	}

	public Tag(final String tagName, final String tagDescription, final Set<TopicToTag> topicToTags, final Set<TagToCategory> tagToCategories)
	{
		this.tagName = tagName;
		this.tagDescription = tagDescription;
		this.topicToTags = topicToTags;
		this.tagToCategories = tagToCategories;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToProject> getTagToProjects()
	{
		return this.tagToProjects;
	}

	public void setTagToProjects(final Set<TagToProject> tagToProjects)
	{
		this.tagToProjects = tagToProjects;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "secondaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToTag> getParentTagToTags()
	{
		return this.parentTagToTags;
	}

	public void setParentTagToTags(final Set<TagToTag> parentTagToTags)
	{
		this.parentTagToTags = parentTagToTags;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "primaryTag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToTag> getChildrenTagToTags()
	{
		return this.childrenTagToTags;
	}

	public void setChildrenTagToTags(final Set<TagToTag> childrenTagToTags)
	{
		this.childrenTagToTags = childrenTagToTags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
	public Set<TagToCategory> getTagToCategories()
	{
		return this.tagToCategories;
	}

	public void setTagToCategories(final Set<TagToCategory> tagToCategories)
	{
		this.tagToCategories = tagToCategories;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "TagID", unique = true, nullable = false)
	public Integer getTagId()
	{
		return this.tagId;
	}

	public void setTagId(Integer tagId)
	{
		this.tagId = tagId;
	}

	@Column(name = "TagName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getTagName()
	{
		return this.tagName;
	}

	public void setTagName(String tagName)
	{
		this.tagName = tagName;
	}

	// @Column(name = "TagDescription", length = 65535)
	@Column(name = "TagDescription", columnDefinition = "TEXT")
	@Length(max = 65535)
	public String getTagDescription()
	{
		return this.tagDescription;
	}

	public void setTagDescription(String tagDescription)
	{
		this.tagDescription = tagDescription;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag", cascade = CascadeType.ALL)
	public Set<TopicToTag> getTopicToTags()
	{
		return this.topicToTags;
	}

	public void setTopicToTags(final Set<TopicToTag> topicToTags)
	{
		this.topicToTags = topicToTags;
	}

	@Transient
	public String getCategoriesList()
	{
		String categoriesList = "";
		for (final TagToCategory category : this.getTagToCategories())
		{
			if (categoriesList.length() != 0)
				categoriesList += " ";
			categoriesList += category.getCategory().getCategoryName();
		}
		return categoriesList;
	}

	@Transient
	public String getChildrenList()
	{
		String retValue = "";
		for (final TagToTag tag : this.getChildrenTagToTags())
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += tag.getSecondaryTag().getTagName();
		}
		return retValue;
	}

	@Transient
	public String getParentList()
	{
		String retValue = "";
		for (final TagToTag tag : this.getParentTagToTags())
		{
			if (retValue.length() != 0)
				retValue += ", ";
			retValue += tag.getPrimaryTag().getTagName();
		}
		return retValue;
	}

	@Transient
	public ArrayList<Integer> getCategoriesIDList()
	{
		ArrayList<Integer> categoriesList = new ArrayList<Integer>();
		for (final TagToCategory category : this.getTagToCategories())
			categoriesList.add(category.getCategory().getCategoryId());
		return categoriesList;
	}

	@Transient
	public ArrayList<Integer> getExclusionTagIDs()
	{
		ArrayList<Integer> tagList = new ArrayList<Integer>();
		for (final Tag tag : this.getExcludedTags())
			tagList.add(tag.getTagId());
		return tagList;
	}

	@Transient
	public boolean isInCategory(final Integer categoryId)
	{
		for (final TagToCategory category : this.getTagToCategories())
			if (categoryId.equals(category.getCategory().getCategoryId()))
				return true;

		return false;
	}

	@Transient
	public boolean isInCategory(final Category category)
	{
		for (final TagToCategory myCategory : this.getTagToCategories())
			if (myCategory.getCategory().equals(category))
				return true;

		return false;
	}
	
	@Transient
	public TagToCategory getCategory(final Integer categoryId)
	{
		for (final TagToCategory category : this.getTagToCategories())
			if (categoryId.equals(category.getCategory().getCategoryId()))
				return category;

		return null;
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "TagExclusion", catalog = "Skynet", joinColumns =
	{ @JoinColumn(name = "Tag1ID", nullable = false, updatable = false) }, inverseJoinColumns =
	{ @JoinColumn(name = "Tag2ID", nullable = false, updatable = false) })
	public Set<Tag> getExcludedTags()
	{
		return this.excludedTags;
	}

	public void setExcludedTags(Set<Tag> excludedTags)
	{
		this.excludedTags = excludedTags;
	}

	/**
	 * @return Returns the list of revision numbers for this entity, as
	 *         maintained by Envers
	 */
	@Transient
	public List<Number> getRevisions()
	{
		final EntityManager entityManager = (EntityManager) Component.getInstance("entityManager");
		final AuditReader reader = AuditReaderFactory.get(entityManager);
		final List<Number> retValue = reader.getRevisions(Tag.class, this.tagId);
		Collections.sort(retValue, Collections.reverseOrder());
		return retValue;
	}

	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		if (!(o instanceof Tag))
			return false;

		final Tag that = (Tag) o;

		if (tagId != null ? !tagId.equals(that.tagId) : that.tagId != null)
			return false;

		return true;
	}

	public int hashCode()
	{
		int result;
		result = (tagId != null ? tagId.hashCode() : 0);
		return result;
	}

	@PrePersist
	protected void onCreate()
	{
		validate();
	}

	@PreUpdate
	protected void onUpdate()
	{
		validate();
	}

	protected void validate()
	{
		// a tag can be a parent, or be a child, but not both
		if (this.getChildrenTagToTags().size() != 0)
			this.getParentTagToTags().clear();
	}

	public boolean removeTagRelationship(final Tag childTag)
	{
		final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), this.getChildrenTagToTags());
		for (final TagToTag child : children)
		{
			this.getChildrenTagToTags().remove(child);
			childTag.getParentTagToTags().remove(child);
		}

		return children.size() != 0;
	}

	public boolean addTagRelationship(final Tag childTag)
	{
		final List<TagToTag> children = filter(having(on(TagToTag.class).getSecondaryTag(), equalTo(childTag)), this.getChildrenTagToTags());
		if (children.size() == 0)
		{
			final TagToTag tagToTag = new TagToTag(new TagToTagRelationship(1), this, childTag);
			this.getChildrenTagToTags().add(tagToTag);
			childTag.getParentTagToTags().add(tagToTag);
			return true;
		}

		return false;
	}

	public boolean removeProjectRelationship(final Project project)
	{
		final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), this.getTagToProjects());
		for (final TagToProject child : children)
		{
			this.getTagToProjects().remove(child);
			child.getProject().getTagToProjects().remove(child);
		}

		return children.size() != 0;
	}

	public boolean addProjectRelationship(final Project project)
	{
		final List<TagToProject> children = filter(having(on(TagToProject.class).getProject(), equalTo(project)), this.getTagToProjects());
		if (children.size() == 0)
		{
			final TagToProject tagToProject = new TagToProject(project, this);
			this.getTagToProjects().add(tagToProject);
			project.getTagToProjects().add(tagToProject);
			return true;
		}

		return false;
	}

	@Transient
	/**
	 * @param project Is the project to test the tags association with. A null project 
	 * 		test that there is no association with any project, or in other words, to test 
	 * 		if this is a common tag
	 * @return true if this tag has been assigned to the project, and false otherwise
	 */
	public boolean isInProject(final Project project)
	{
		if (this.tagToProjects.size() == 0 && project == null)
			return true;

		for (final TagToProject tagToProject : this.tagToProjects)
		{
			if (tagToProject.getProject().equals(project))
				return true;
		}

		return false;
	}

	@Transient
	public List<Tag> getTags()
	{
		final List<Tag> retValue = new ArrayList<Tag>();
		for (final TagToTag tag : this.childrenTagToTags)
			retValue.add(tag.getSecondaryTag());

		return retValue;
	}
}
