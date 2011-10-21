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
	private String cvsPkgOption = "JBoss_Enterprise_Application_Platform-6-web-__LANG__";

	public void setProcessRelatedTopics(final Boolean processRelatedTopics)
	{
		this.processRelatedTopics = processRelatedTopics;
	}

	public Boolean getProcessRelatedTopics()
	{
		return processRelatedTopics;
	}

	public void setPublicanShowRemarks(final Boolean publicanShowRemarks)
	{
		this.publicanShowRemarks = publicanShowRemarks;
	}

	public Boolean getPublicanShowRemarks()
	{
		return publicanShowRemarks;
	}

	public void setEnableDynamicTreeToc(final Boolean enableDynamicTreeToc)
	{
		this.enableDynamicTreeToc = enableDynamicTreeToc;
	}

	public Boolean getEnableDynamicTreeToc()
	{
		return enableDynamicTreeToc;
	}

	public Boolean getBuildNarrative()
	{
		return buildNarrative;
	}

	public void setBuildNarrative(final Boolean buildNarrative)
	{
		this.buildNarrative = buildNarrative;
	}

	public Boolean getIgnoreMissingCustomInjections()
	{
		return ignoreMissingCustomInjections;
	}

	public void setIgnoreMissingCustomInjections(final Boolean ignoreMissingCustomInjections)
	{
		this.ignoreMissingCustomInjections = ignoreMissingCustomInjections;
	}

	public Boolean getSuppressErrorsPage()
	{
		return suppressErrorsPage;
	}

	public void setSuppressErrorsPage(final Boolean suppressErrorsPage)
	{
		this.suppressErrorsPage = suppressErrorsPage;
	}

	public Boolean getTaskAndOverviewOnly()
	{
		return taskAndOverviewOnly;
	}

	public void setTaskAndOverviewOnly(final Boolean taskAndOverviewOnly)
	{
		this.taskAndOverviewOnly = taskAndOverviewOnly;
	}

	public Boolean getInsertSurveyLink()
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
		retValue.add(Constants.DOCBOOK_BUILDING_OPTION_CVS_PKG);

		return retValue;
	}

	public String getFieldValue(final String fieldName)
	{
		if (fieldName == null)
			return null;

		final String fixedFieldName = fieldName.trim();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_BUILD_NARRATIVE))
			return this.getBuildNarrative() == null ? null : this.getBuildNarrative().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_ENABLE_DYNAMIC_TOC))
			return this.getEnableDynamicTreeToc() == null ? null : this.getEnableDynamicTreeToc().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_IGNORE_MISSING_CUSTOM_INJECTIONS))
			return this.getIgnoreMissingCustomInjections() == null ? null : this.getIgnoreMissingCustomInjections().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_INSERT_SURVEY_LINK))
			return this.getInsertSurveyLink() == null ? null : this.getInsertSurveyLink().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_PROCESS_RELATED_TOPICS))
			return this.getProcessRelatedTopics() == null ? null : this.getProcessRelatedTopics().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SHOW_REMARKS))
			return this.getPublicanShowRemarks() == null ? null : this.getPublicanShowRemarks().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_SUPPRESS_ERROR_PAGE))
			return this.getSuppressErrorsPage() == null ? null : this.getSuppressErrorsPage().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_TASK_AND_OVERVIEW_ONLY))
			return this.getTaskAndOverviewOnly() == null ? null : this.getTaskAndOverviewOnly().toString();

		if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_CVS_PKG))
			return this.getCvsPkgOption() == null ? null : this.getCvsPkgOption();

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

			if (fixedFieldName.equalsIgnoreCase(Constants.DOCBOOK_BUILDING_OPTION_CVS_PKG))
				this.setCvsPkgOption(fieldValue);

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

	public String getCvsPkgOption()
	{
		return cvsPkgOption;
	}

	public void setCvsPkgOption(final String cvsPkgOption)
	{
		this.cvsPkgOption = cvsPkgOption;
	}

}
