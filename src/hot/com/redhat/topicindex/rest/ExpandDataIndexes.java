package com.redhat.topicindex.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.redhat.ecs.commonutils.ExceptionUtilities;

public class ExpandDataIndexes
{
	private static final String START_INDEX_NAMED_GROUP = "startIndex";
	private static final String END_INDEX_NAMED_GROUP = "endIndex";
	private static final String COLON_NAMED_GROUP = "colon";
	private static final String EXPAND_LEVEL_NAMED_GROUP = "expandLevel";
	private static final String EXPAND_DATA_INDEXES_RE = "^(?<" + EXPAND_LEVEL_NAMED_GROUP + ">\\w+)(\\[(?<" + START_INDEX_NAMED_GROUP + ">-?\\d+)?(?<" + COLON_NAMED_GROUP + ">:)?(?<" + END_INDEX_NAMED_GROUP + ">-?\\d+)?\\])?$";
	private String expandLevel = null;
	private Integer startExpandedIndex = null;
	private Integer endExpandedIndex = null;
	private boolean definedStart = true;
	private boolean definedFinsh = true;
	private boolean startAtBegining = false;
	private boolean finishAtEnd = false;
	private boolean valid = false;

	public Integer getStartIndex()
	{
		if (startExpandedIndex == null || endExpandedIndex == null)
			return startExpandedIndex;
		return Math.min(startExpandedIndex, endExpandedIndex);
	}

	public Integer getEndIndex()
	{
		if (startExpandedIndex == null || endExpandedIndex == null)
			return endExpandedIndex;
		return Math.max(startExpandedIndex, endExpandedIndex);
	}

	public ExpandDataIndexes(final String expand)
	{
		/* compile the regular expression */
		final Pattern sequencePattern = Pattern.compile(EXPAND_DATA_INDEXES_RE);
		/* find any matches */
		final Matcher sequenceMatcher = sequencePattern.matcher(expand);

		/* loop over the regular expression matches */
		if (sequenceMatcher.matches())
		{
			valid = true;

			/* get the components of the regular expression */
			expandLevel = sequenceMatcher.group(EXPAND_LEVEL_NAMED_GROUP);
			final String colon = sequenceMatcher.group(COLON_NAMED_GROUP);
			final String startIndexMatch = sequenceMatcher.group(START_INDEX_NAMED_GROUP);
			final String endIndexMatch = sequenceMatcher.group(END_INDEX_NAMED_GROUP);

			/* deal with no brackets at all like: tags*/
			if (startIndexMatch == null && colon == null && endIndexMatch == null)
			{
				/* we have not specifically defined the start at the beginning or finish at the ending */
				definedFinsh = false;
				definedStart = false;
				/* but the overall effect is that we will start at the begining and finish at the end */ 
				startAtBegining = true;
				finishAtEnd = true;
			}

			/* deal with input like: tags[1:] */
			else if (startIndexMatch != null && colon != null && endIndexMatch == null)
			{				
				finishAtEnd = true;
			}

			/* deal with input like: tags[:1] */
			else if (startIndexMatch == null && colon != null && endIndexMatch != null)
			{				
				startAtBegining = true;
			}
			
			if (startIndexMatch != null)
			{
				try
				{
					startExpandedIndex = Integer.parseInt(startIndexMatch);
					
					/* deal with input like: tags[1] */
					if (colon == null && endIndexMatch == null)
					{
						endExpandedIndex = startExpandedIndex;
					}
				}
				catch (final Exception ex)
				{
					ExceptionUtilities.handleException(ex);
				}
			}

			if (endIndexMatch != null)
			{
				try
				{
					endExpandedIndex = Integer.parseInt(endIndexMatch);
				}
				catch (final Exception ex)
				{
					ExceptionUtilities.handleException(ex);
				}
			}
		}

	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public boolean isDefinedStart()
	{
		return definedStart;
	}

	public void setDefinedStart(boolean definedStart)
	{
		this.definedStart = definedStart;
	}

	public boolean isDefinedFinsh()
	{
		return definedFinsh;
	}

	public void setDefinedFinsh(boolean definedFinsh)
	{
		this.definedFinsh = definedFinsh;
	}

	public String getExpandLevel()
	{
		return expandLevel;
	}

	public void setExpandLevel(String expandLevel)
	{
		this.expandLevel = expandLevel;
	}

	public boolean isStartAtBegining()
	{
		return startAtBegining;
	}

	public void setStartAtBegining(boolean startAtBegining)
	{
		this.startAtBegining = startAtBegining;
	}

	public boolean isFinishAtEnd()
	{
		return finishAtEnd;
	}

	public void setFinshAtEnd(boolean finishAtEnd)
	{
		this.finishAtEnd = finishAtEnd;
	}
}
