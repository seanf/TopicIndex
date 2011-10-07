package com.redhat.topicindex.utils.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information on the errors and warnings that were detected in a topic.

 */
public class TopicErrorData
{
	private List<String> errors = new ArrayList<String>();
	private List<String> warnings = new ArrayList<String>();
	public List<String> getErrors()
	{
		return errors;
	}
	public void setErrors(List<String> errors)
	{
		this.errors = errors;
	}
	public List<String> getWarnings()
	{
		return warnings;
	}
	public void setWarnings(List<String> warnings)
	{
		this.warnings = warnings;
	}
}
