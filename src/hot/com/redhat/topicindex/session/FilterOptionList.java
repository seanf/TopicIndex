package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import java.util.Arrays;

@Name("filterOptionList")
public class FilterOptionList extends EntityQuery<FilterOption>
{

	private static final String EJBQL = "select filterOption from FilterOption filterOption";

	private static final String[] RESTRICTIONS =
	{ "lower(filterOption.filterOptionName) like lower(concat(#{filterOptionList.filterOption.filterOptionName},'%'))", "lower(filterOption.filterOptionValue) like lower(concat(#{filterOptionList.filterOption.filterOptionValue},'%'))", };

	private FilterOption filterOption = new FilterOption();

	public FilterOptionList()
	{
		setEjbql(EJBQL);
		setRestrictionExpressionStrings(Arrays.asList(RESTRICTIONS));
		setMaxResults(25);
	}

	public FilterOption getFilterOption()
	{
		return filterOption;
	}
}
