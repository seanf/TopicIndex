package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("filterCategoryList")
public class FilterCategoryList extends EntityQuery<FilterCategory> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -5384923196608337309L;

	private static final String EJBQL = "select filterCategory from FilterCategory filterCategory";

	private static final String[] RESTRICTIONS = {};

	private FilterCategory filterCategory = new FilterCategory();

	public FilterCategoryList() {
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public FilterCategory getFilterCategory() {
		return filterCategory;
	}
}
