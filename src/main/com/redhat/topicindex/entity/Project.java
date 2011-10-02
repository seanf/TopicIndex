package com.redhat.topicindex.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.topicindex.utils.structures.NameIDSortMap;

@Audited
@Entity
@Table(name = "Project", catalog = "Skynet")
public class Project implements java.io.Serializable 
{
	public static final String SELECT_ALL_QUERY = "select project from Project project";
	private static final long serialVersionUID = 7468160102030564523L;
	private Integer projectId;
	private String projectName;
	private String projectDescription;
	private Set<TagToProject> tagToProjects = new HashSet<TagToProject>(0);

	public Project() {
	}

	public Project(String projectName) {
		this.projectName = projectName;
	}

	public Project(String projectName, String projectDescription,
			Set<TagToProject> tagToProjects) {
		this.projectName = projectName;
		this.projectDescription = projectDescription;
		this.tagToProjects = tagToProjects;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ProjectID", unique = true, nullable = false)
	public Integer getProjectId() {
		return this.projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
	}

	@Column(name = "ProjectName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getProjectName() {
		return this.projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	//@Column(name = "ProjectDescription", length = 65535)
	@Column(name = "ProjectDescription", columnDefinition="TEXT")
	@Length(max = 65535)
	public String getProjectDescription() {
		return this.projectDescription;
	}

	public void setProjectDescription(String projectDescription) {
		this.projectDescription = projectDescription;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "project", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<TagToProject> getTagToProjects() {
		return this.tagToProjects;
	}

	public void setTagToProjects(Set<TagToProject> tagToProjects) {
		this.tagToProjects = tagToProjects;
	}
	
	@Transient
	public String getTagsList()
	{
		return getTagsList(true);
	}

	/**
	 * Generates a HTML formatted and categorized list of the tags that are associated with this topic 
	 * @return A HTML String to display in a table
	 */
	@Transient
	public String getTagsList(final boolean brLineBreak)
	{
		// define the line breaks for html and for tooltips
		final String lineBreak = brLineBreak?"<br/>":"\n";
		final String boldStart = brLineBreak?"<b>":"";
		final String boldEnd = brLineBreak?"</b>":"";
		
		TreeMap<NameIDSortMap, ArrayList<String>> tags = new TreeMap<NameIDSortMap, ArrayList<String>>();
		
		for (final TagToProject tagToProject :  this.tagToProjects)
		{
			final Tag tag = tagToProject.getTag();
			final Set<TagToCategory> tagToCategories = tag.getTagToCategories();
			
			if (tagToCategories.size() == 0)
			{
				NameIDSortMap categoryDetails = new NameIDSortMap("Uncatagorised", -1, 0);
				
				if (!tags.containsKey(categoryDetails))
					tags.put(categoryDetails, new ArrayList<String>());
				
				tags.get(categoryDetails).add(tag.getTagName());
			}
			else
			{
				for (final TagToCategory category : tagToCategories)
				{			
					NameIDSortMap categoryDetails = new NameIDSortMap(
							category.getCategory().getCategoryName(), 
							category.getCategory().getCategoryId(), 
							category.getCategory().getCategorySort()==null?0:category.getCategory().getCategorySort());
					
					if (!tags.containsKey(categoryDetails))
						tags.put(categoryDetails, new ArrayList<String>());
					
					tags.get(categoryDetails).add(tag.getTagName());
				}
			}
		}
		
		String tagsList = "";		
		for (final NameIDSortMap key : tags.keySet())
		{
			// sort alphabetically
			Collections.sort(tags.get(key));
			
			if (tagsList.length() != 0)
				tagsList += lineBreak;
			
			tagsList += boldStart + key.getName() + boldEnd + ": ";
			
			String thisTagList = "";
			
			for (final String tag : tags.get(key))
			{
				if (thisTagList.length() != 0)
					thisTagList += ", ";
				
				thisTagList += tag;
			}
			
			tagsList += thisTagList + " ";
		}

		return tagsList;
	}
	
    @Transient
    public List<Tag> getTags()
    {
    	final List<Tag> retValue = new ArrayList<Tag>();
    	for (final TagToProject tag : this.tagToProjects)
    		retValue.add(tag.getTag());
    	
    	return retValue;
    }

}
