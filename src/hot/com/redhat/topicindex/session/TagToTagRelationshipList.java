package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("tagToTagRelationshipList")
public class TagToTagRelationshipList extends EntityQuery<TagToTagRelationship> 
{



	private static final String EJBQL = "select tagToTagRelationship from TagToTagRelationship tagToTagRelationship";

	private static final String[] RESTRICTIONS = { "lower(tagToTagRelationship.tagToTagRelationshipDescription) like lower(concat(#{tagToTagRelationshipList.tagToTagRelationship.tagToTagRelationshipDescription},'%'))", };

	private TagToTagRelationship tagToTagRelationship = new TagToTagRelationship();

	public TagToTagRelationshipList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TagToTagRelationship getTagToTagRelationship() {
		return tagToTagRelationship;
	}
}
