package com.redhat.topicindex.utils.docbookbuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.Filter;
import com.redhat.topicindex.entity.FilterField;
import com.redhat.topicindex.entity.FilterOption;
import com.redhat.topicindex.utils.Constants;

/**
 * This class contains the options associated with building the docbook zip
 * file.
 */
public class DocbookBuildingOptions
{
	private Boolean processRelatedTopics = false;
	private Boolean publicanShowRemarks = false;
	private Boolean enableDynamicTreeToc = true;
	private Boolean buildNarrative = false;
	private Boolean ignoreMissingCustomInjections = true;
	private Boolean suppressErrorsPage = false;
	private Boolean taskAndOverviewOnly = true;
	private Boolean insertSurveyLink = true;

	public void setProcessRelatedTopics(final Boolean processRelatedTopics)
	{
		this.processRelatedTopics = processRelatedTopics;
	}

	public Boolean isProcessRelatedTopics()
	{
		return processRelatedTopics;
	}

	public void setPublicanShowRemarks(final Boolean publicanShowRemarks)
	{
		this.publicanShowRemarks = publicanShowRemarks;
	}

	public Boolean isPublicanShowRemarks()
	{
		return publicanShowRemarks;
	}

	public void setEnableDynamicTreeToc(final Boolean enableDynamicTreeToc)
	{
		this.enableDynamicTreeToc = enableDynamicTreeToc;
	}

	public Boolean isEnableDynamicTreeToc()
	{
		return enableDynamicTreeToc;
	}

	public Boolean isBuildNarrative()
	{
		return buildNarrative;
	}

	public void setBuildNarrative(final Boolean buildNarrative)
	{
		this.buildNarrative = buildNarrative;
	}

	public Boolean isIgnoreMissingCustomInjections()
	{
		return ignoreMissingCustomInjections;
	}

	public void setIgnoreMissingCustomInjections(final Boolean ignoreMissingCustomInjections)
	{
		this.ignoreMissingCustomInjections = ignoreMissingCustomInjections;
	}

	public Boolean isSuppressErrorsPage()
	{
		return suppressErrorsPage;
	}

	public void setSuppressErrorsPage(final Boolean suppressErrorsPage)
	{
		this.suppressErrorsPage = suppressErrorsPage;
	}

	public Boolean isTaskAndOverviewOnly()
	{
		return taskAndOverviewOnly;
	}

	public void setTaskAndOverviewOnly(final Boolean taskAndOverviewOnly)
	{
		this.taskAndOverviewOnly = taskAndOverviewOnly;
	}

	public Boolean isInsertSurveyLink()
	{
		return insertSurveyLink;
	}

	public void setInsertSurveyLink(final Boolean insertSurveyLink)
	{
		this.insertSurveyLink = insertSurveyLink;
	}

	public static List<String> getOptionNames()
	{
		final List<String> retValue = new ArrayList<String>();

		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_BUILD_NARRATIVE);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_ENABLE_DYNAMIC_TOC);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_IGNORE_MISSING_CUSTOM_INJECTIONS);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_INSERT_SURVEY_LINK);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_PROCESS_RELATED_TOPICS);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_SHOW_REMARKS);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_SUPPRESS_ERROR_PAGE);
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_TASK_AND_OVERVIEW_ONLY);

		return retValue;
	}

	public String getFieldValue(final String fieldName)
	{
		if (fieldName == null)
			return null;

		final String fixedFieldName = fieldName.trim();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_BUILD_NARRATIVE))
			return this.isBuildNarrative() == null ? null : this.isBuildNarrative().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_ENABLE_DYNAMIC_TOC))
			return this.isEnableDynamicTreeToc() == null ? null : this.isEnableDynamicTreeToc().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_IGNORE_MISSING_CUSTOM_INJECTIONS))
			return this.isIgnoreMissingCustomInjections() == null ? null : this.isIgnoreMissingCustomInjections().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_INSERT_SURVEY_LINK))
			return this.isInsertSurveyLink() == null ? null : this.isInsertSurveyLink().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_PROCESS_RELATED_TOPICS))
			return this.isProcessRelatedTopics() == null ? null : this.isProcessRelatedTopics().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SHOW_REMARKS))
			return this.isPublicanShowRemarks() == null ? null : this.isPublicanShowRemarks().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SUPPRESS_ERROR_PAGE))
			return this.isSuppressErrorsPage() == null ? null : this.isSuppressErrorsPage().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_TASK_AND_OVERVIEW_ONLY))
			return this.isTaskAndOverviewOnly() == null ? null : this.isTaskAndOverviewOnly().toString();

		return null;
	}

	public void setFieldValue(final String fieldName, final String fieldValue)
	{
		if (fieldName == null)
			return;

		final String fixedFieldName = fieldName.trim();

		try
		{
			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_BUILD_NARRATIVE))
				this.setBuildNarrative(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_ENABLE_DYNAMIC_TOC))
				this.setEnableDynamicTreeToc(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_IGNORE_MISSING_CUSTOM_INJECTIONS))
				this.setIgnoreMissingCustomInjections(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_INSERT_SURVEY_LINK))
				this.setInsertSurveyLink(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_PROCESS_RELATED_TOPICS))
				this.setProcessRelatedTopics(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SHOW_REMARKS))
				this.setPublicanShowRemarks(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SUPPRESS_ERROR_PAGE))
				this.setSuppressErrorsPage(Boolean.parseBoolean(fieldValue));

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_TASK_AND_OVERVIEW_ONLY))
				this.setTaskAndOverviewOnly(Boolean.parseBoolean(fieldValue));
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}
	
	public void syncWithFilter(final Filter filter)
	{
		for (final FilterOption option : filter.getFilterOptions())
			this.setFieldValue(option.getFilterOptionName(), option.getFilterOptionValue());
	}

}
