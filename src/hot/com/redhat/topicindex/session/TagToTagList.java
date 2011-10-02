package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("tagToTagList")
public class TagToTagList extends EntityQuery<TagToTag> {

	/** Serializable version identifier */
	private static final long serialVersionUID = 1L;

	private static final String EJBQL = "select tagToTag from TagToTag tagToTag";

	private static final String[] RESTRICTIONS = {};

	private TagToTag tagToTag = new TagToTag();

	public TagToTagList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public TagToTag getTagToTag() {
		return tagToTag;
	}
}
