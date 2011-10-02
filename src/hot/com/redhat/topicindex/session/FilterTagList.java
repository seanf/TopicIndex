package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("filterTagList")
public class FilterTagList extends EntityQuery<FilterTag> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4434050058827706025L;

	private static final String EJBQL = "select filterTag from FilterTag filterTag";

	private static final String[] RESTRICTIONS = {};

	private FilterTag filterTag = new FilterTag();

	public FilterTagList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public FilterTag getFilterTag() {
		return filterTag;
	}
}
