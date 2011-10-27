package com.redhat.topicindex.rest;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;
import com.redhat.ecs.commonutils.ExceptionUtilities;

public class ExpandDataIndexes
{
	private static final String START_INDEX_NAMED_GROUP = "startIndex";
	private static final String END_INDEX_NAMED_GROUP = "endIndex";
	private static final String COLON_NAMED_GROUP = "colon";
	private static final String EXPAND_LEVEL_NAMED_GROUP = "expandLevel";
	private static final String EXPAND_DATA_INDEXES_RE = "^(?<" + EXPAND_LEVEL_NAMED_GROUP + ">\\w+)(\\[(?<" + START_INDEX_NAMED_GROUP + ">-?\\d+)?(?<" + COLON_NAMED_GROUP + ">:)?(?<" + END_INDEX_NAMED_GROUP + ">-?\\d+)?\\])?$";
	private String expandLevel = null;
	private Integer startIndex = null;
	private Integer endIndex = null;
	private boolean impliedStartAtBegining = false;
	private boolean impliedFinishAtEnd = false;
	private boolean valid = false;

	public Integer getStartIndex()
	{
		if (startIndex == null || endIndex == null)
			return startIndex;
		return Math.min(startIndex, endIndex);
	}

	public Integer getEndIndex()
	{
		if (startIndex == null || endIndex == null)
			return endIndex;
		return Math.max(startIndex, endIndex);
	}

	public ExpandDataIndexes(final String expand)
	{
		/* compile the regular expression */
		final NamedPattern sequencePattern = NamedPattern.compile(EXPAND_DATA_INDEXES_RE);
		/* find any matches */
		final NamedMatcher sequenceMatcher = sequencePattern.matcher(expand);

		/* loop over the regular expression matches */
		while (sequenceMatcher.find())
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
				impliedFinishAtEnd = true;
				impliedStartAtBegining = true;
			}

			/* deal with input like: tags[1:] */
			else if (startIndexMatch != null && colon != null && endIndexMatch == null)
			{
				impliedFinishAtEnd = true;
			}

			/* deal with input like: tags[:1] */
			else if (startIndexMatch == null && colon != null && endIndexMatch != null)
			{
				impliedStartAtBegining = true;
			}

			if (startIndexMatch != null)
			{
				try
				{
					startIndex = Integer.parseInt(startIndexMatch);
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
					endIndex = Integer.parseInt(endIndexMatch);
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

	public boolean isImpliedStartAtBegining()
	{
		return impliedStartAtBegining;
	}

	public void setImpliedStartAtBegining(boolean impliedStartAtBegining)
	{
		this.impliedStartAtBegining = impliedStartAtBegining;
	}

	public boolean isImpliedFinishAtEnd()
	{
		return impliedFinishAtEnd;
	}

	public void setImpliedFinishAtEnd(boolean impliedFinishAtEnd)
	{
		this.impliedFinishAtEnd = impliedFinishAtEnd;
	}

	public String getExpandLevel()
	{
		return expandLevel;
	}

	public void setExpandLevel(String expandLevel)
	{
		this.expandLevel = expandLevel;
	}
}
