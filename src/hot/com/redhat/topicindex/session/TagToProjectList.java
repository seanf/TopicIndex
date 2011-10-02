package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("tagToProjectList")
public class TagToProjectList extends EntityQuery<TagToProject> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -5909975403916916300L;

	private static final String EJBQL = "select tagToProject from TagToProject tagToProject";

	private static final String[] RESTRICTIONS = {};

	private TagToProject tagToProject = new TagToProject();

	public TagToProjectList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TagToProject getTagToProject() {
		return tagToProject;
	}
}
