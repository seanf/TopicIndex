package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("tagToCategoryList")
public class TagToCategoryList extends EntityQuery<TagToCategory> 
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 206877149207489900L;

	private static final String[] RESTRICTIONS = {};

	private TagToCategory tagToCategory = new TagToCategory();

	public TagToCategoryList() 
	{
		construct(25);
	}
	
	public TagToCategoryList(final int limit) 
	{
		construct(limit);
	}
	
	protected void construct(final int limit)
	{
		setEjbql(TagToCategory.SELECT_ALL_QUERY);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(limit);
	}

	public TagToCategory getTagToCategory() {
		return tagToCategory;
	}
}
