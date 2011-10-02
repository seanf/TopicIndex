package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("filterList")
public class FilterList extends EntityQuery<Filter> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = -860805416635943362L;

	private static final String[] RESTRICTIONS = {
			"lower(filter.filterName) like lower(concat('%',#{filterList.filter.filterName},'%'))",
			"lower(filter.filterDescription) like lower(concat('%',#{filterList.filter.filterDescription},'%'))", };

	private Filter filter = new Filter();

	public FilterList() 
	{
		construct(25);
	}
	
	public FilterList(final int limit) 
	{
		construct(limit);
	}

	public Filter getFilter() 
	{
		return filter;
	}
	
	protected void construct(final int limit)
	{
		setEjbql(Filter.SELECT_ALL_QUERY);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		if (limit != -1)
			setMaxResults(25);
	}
}
