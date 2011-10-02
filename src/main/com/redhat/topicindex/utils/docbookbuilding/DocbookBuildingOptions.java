package com.redhat.topicindex.utils.docbookbuilding;

/**
 *	This class contains the options associated with building the docbook
 * 	zip file.
 */
public class DocbookBuildingOptions 
{
	private boolean processRelatedTopics = true;
	private boolean publicanShowRemarks = false;
	private boolean enableDynamicTreeToc = true;
	private boolean buildNarrative = false;
	private boolean ignoreMissingCustomInjections = false;
	private boolean suppressErrorsPage = false;

	public void setProcessRelatedTopics(final boolean processRelatedTopics) {
		this.processRelatedTopics = processRelatedTopics;
	}

	public boolean isProcessRelatedTopics() {
		return processRelatedTopics;
	}

	public void setPublicanShowRemarks(boolean publicanShowRemarks) {
		this.publicanShowRemarks = publicanShowRemarks;
	}

	public boolean isPublicanShowRemarks() {
		return publicanShowRemarks;
	}

	public void setEnableDynamicTreeToc(boolean enableDynamicTreeToc) {
		this.enableDynamicTreeToc = enableDynamicTreeToc;
	}

	public boolean isEnableDynamicTreeToc() {
		return enableDynamicTreeToc;
	}

	public boolean isBuildNarrative() {
		return buildNarrative;
	}

	public void setBuildNarrative(final boolean buildNarrative) {
		this.buildNarrative = buildNarrative;
	}

	public boolean isIgnoreMissingCustomInjections() {
		return ignoreMissingCustomInjections;
	}

	public void setIgnoreMissingCustomInjections(
			boolean ignoreMissingCustomInjections) {
		this.ignoreMissingCustomInjections = ignoreMissingCustomInjections;
	}

	public boolean isSuppressErrorsPage() 
	{
		return suppressErrorsPage;
	}

	public void setSuppressErrorsPage(boolean suppressErrorsPage) 
	{
		this.suppressErrorsPage = suppressErrorsPage;
	} 
}
