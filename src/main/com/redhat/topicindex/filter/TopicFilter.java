package com.redhat.topicindex.filter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.EntityUtilities;

/**
 * This class represents the options used by the objects that extend the
 * ExtendedTopicList class to filter a query to retrieve Topic entities.
 */
public class TopicFilter
{
	private ArrayList<Integer> topicIds;
	private Integer topicRelatedTo;
	private Integer topicRelatedFrom;
	private String topicTitle;
	private String topicText;
	private String topicAddedBy;
	private DateTime startCreateDate;
	private DateTime endCreateDate;
	private String topicRendered;
	private String topicXML;
	private String topicProduct;
	private String logic;
	private Boolean hasRelationships;
	private Integer minimumRelationshipCount;
	private Boolean hasIncomingRelationships;
	private Integer minimumIncomingRelationshipCount;
	private String topicTextSearch;

	public List<Integer> getRelatedTopicIDs()
	{
		final List<Integer> retValue = EntityUtilities.getRelatedTopicIDs(this.topicRelatedFrom);
		// The EntityQuery class treats an empty list as null, and will not
		// attempt to
		// apply a restriction against it. If there are no topics returned, add
		// -1 to the
		// list to ensure that no topics are matched, but the list is not empty
		// and therefore
		// counted as a restriction.
		if (retValue != null && retValue.size() == 0)
			retValue.add(-1);
		return retValue;
	}

	public List<Integer> getIncomingRelatedTopicIDs()
	{
		final List<Integer> retValue = EntityUtilities.getIncomingRelatedTopicIDs(this.topicRelatedTo);
		if (retValue != null && retValue.size() == 0)
			retValue.add(-1);
		return retValue;
	}

	public void setTopicRelatedTo(final Integer topicRelatedTo)
	{
		this.topicRelatedTo = topicRelatedTo;
	}

	public Integer getTopicRelatedTo()
	{
		return topicRelatedTo;
	}

	public void setHasRelationships(final Boolean hasRelationships)
	{
		if (hasRelationships == null || !hasRelationships)
		{
			this.minimumRelationshipCount = null;
			this.hasRelationships = null;
		}
		else
		{
			this.minimumRelationshipCount = 1;
			this.hasRelationships = true;
		}
	}

	public Boolean getHasRelationships()
	{
		return hasRelationships;
	}

	public void setMinimumRelationshipCount(Integer minimumRelationshipCount)
	{
		this.minimumRelationshipCount = minimumRelationshipCount;
	}

	public Integer getMinimumRelationshipCount()
	{
		return minimumRelationshipCount;
	}

	public void setTopicTitle(final String topicTitle)
	{
		this.topicTitle = topicTitle;
	}

	public String getTopicTitle()
	{
		return topicTitle;
	}

	public void setTopicText(final String topicText)
	{
		this.topicText = topicText;
	}

	public String getTopicText()
	{
		return topicText;
	}

	public void setTopicAddedBy(final String topicAddedBy)
	{
		this.topicAddedBy = topicAddedBy;
	}

	public String getTopicAddedBy()
	{
		return topicAddedBy;
	}

	public void setStartCreateDate(final DateTime startCreateDate)
	{
		this.startCreateDate = startCreateDate;
	}

	public DateTime getStartCreateDate()
	{
		return startCreateDate;
	}

	public void setEndCreateDate(final DateTime endCreateDate)
	{
		this.endCreateDate = endCreateDate;
	}

	public DateTime getEndCreateDate()
	{
		return endCreateDate;
	}

	public void setTopicRendered(final String topicRendered)
	{
		this.topicRendered = topicRendered;
	}

	public String getTopicRendered()
	{
		return topicRendered;
	}

	public void setTopicXML(final String topicXML)
	{
		this.topicXML = topicXML;
	}

	public String getTopicXML()
	{
		return topicXML;
	}

	public void setTopicProduct(final String topicProduct)
	{
		this.topicProduct = topicProduct;
	}

	public String getTopicProduct()
	{
		return topicProduct;
	}

	public void setTopicIds(final ArrayList<Integer> topicIds)
	{
		this.topicIds = topicIds;
	}

	public ArrayList<Integer> getTopicIds()
	{
		return topicIds;
	}

	public void setTopicIdsString(final String topicIdsString)
	{
		this.topicIds = new ArrayList<Integer>();
		if (topicIdsString != null)
		{
			final String[] topicIdsArray = topicIdsString.split(",");
			for (final String topicId : topicIdsArray)
			{
				try
				{
					if (topicId.trim().length() != 0)
						this.topicIds.add(Integer.parseInt(topicId.trim()));
				}
				catch (final Exception ex)
				{
					ExceptionUtilities.handleException(ex);
				}
			}
		}
	}

	public String getTopicIdsString()
	{
		if (this.topicIds != null && this.topicIds.size() != 0)
		{
			String retValue = "";
			for (final Integer topicId : this.topicIds)
			{
				if (retValue.length() != 0)
					retValue += ",";
				retValue += topicId.toString();
			}
			return retValue;
		}

		return null;
	}

	public void setStartCreateDateString(final String startDate)
	{
		try
		{
			if (startDate == null || startDate.length() == 0)
				this.startCreateDate = null;
			else
				this.startCreateDate = ISODateTimeFormat.dateTime().parseDateTime(startDate);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	public String getStartCreateDateString()
	{
		return this.startCreateDate == null ? null : ISODateTimeFormat.dateTime().print(this.startCreateDate);
	}

	public void setEndCreateDateString(final String endDate)
	{
		try
		{
			if (endDate == null || endDate.length() == 0)
				this.endCreateDate = null;
			else
				this.endCreateDate = ISODateTimeFormat.dateTime().parseDateTime(endDate);
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	public String getEndCreateDateString()
	{
		return this.endCreateDate == null ? null : ISODateTimeFormat.dateTime().print(this.endCreateDate);
	}

	public void setLogic(String logic)
	{
		this.logic = logic;
	}

	public String getLogic()
	{
		return logic;
	}

	public void setStartCreateDatePlain(final Date startCreateDate)
	{
		this.startCreateDate = startCreateDate == null ? null : new DateTime(startCreateDate);
	}

	public Date getStartCreateDatePlain()
	{
		return this.startCreateDate == null ? null : this.startCreateDate.toDate();
	}

	public void setEndCreateDatePlain(final Date endCreateDate)
	{
		this.endCreateDate = endCreateDate == null ? null : new DateTime(endCreateDate);
	}

	public Date getEndCreateDatePlain()
	{
		return this.endCreateDate == null ? null : this.endCreateDate.toDate();
	}

	public String getFieldValue(final String fieldName)
	{
		if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
			return this.getTopicIdsString();
		else if (fieldName.equals(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR))
			return this.getTopicTextSearch();
		else if (fieldName.equals(Constants.TOPIC_ADDED_BY_FILTER_VAR))
			return this.getTopicAddedBy();
		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
			return this.getTopicXML();
		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
			return this.getTopicTitle();
		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
			return this.getTopicText();
		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
			return this.getStartCreateDateString();
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
			return this.getEndCreateDateString();
		else if (fieldName.equals(Constants.TOPIC_LOGIC_FILTER_VAR))
			return this.getLogic();
		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
			return this.hasRelationships == null ? null : this.hasRelationships.toString();
		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
			return this.hasIncomingRelationships == null ? null : this.hasIncomingRelationships.toString();
		else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
			return this.topicRelatedTo == null ? null : this.topicRelatedTo.toString();
		else if (fieldName.equals(Constants.TOPIC_RELATED_FROM))
			return this.topicRelatedFrom == null ? null : this.topicRelatedFrom.toString();

		return null;
	}

	public void setFieldValue(final String fieldName, final String fieldValue)
	{
		if (fieldName.equals(Constants.TOPIC_IDS_FILTER_VAR))
			this.setTopicIdsString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR))
			this.setTopicXML(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_XML_FILTER_VAR))
			this.setTopicText(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_ADDED_BY_FILTER_VAR))
			this.setTopicAddedBy(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_TITLE_FILTER_VAR))
			this.setTopicTitle(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_DESCRIPTION_FILTER_VAR))
			this.setTopicText(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_STARTDATE_FILTER_VAR))
			this.setStartCreateDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_ENDDATE_FILTER_VAR))
			this.setEndCreateDateString(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_LOGIC_FILTER_VAR))
			this.setLogic(fieldValue);
		else if (fieldName.equals(Constants.TOPIC_HAS_RELATIONSHIPS))
			this.setHasRelationships(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS))
			this.setHasIncomingRelationships(fieldValue == null ? null : fieldValue.equalsIgnoreCase("true"));
		else if (fieldName.equals(Constants.TOPIC_RELATED_TO))
		{
			try
			{				
				this.setTopicRelatedTo(fieldValue == null ? null : Integer.parseInt(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				ExceptionUtilities.handleException(ex);
			}
		}
		else if (fieldName.equals(Constants.TOPIC_RELATED_FROM))
		{
			try
			{
				this.setTopicRelatedFrom(fieldValue == null ? null : Integer.parseInt(fieldValue));
			}
			catch (final Exception ex)
			{
				// could not parse integer, so silently fail
				ExceptionUtilities.handleException(ex);
			}
		}
	}

	public Map<String, String> getFilterValues()
	{
		final Map<String, String> retValue = new HashMap<String, String>();
		retValue.put(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR, this.getTopicTextSearch());
		retValue.put(Constants.TOPIC_IDS_FILTER_VAR, this.getTopicIdsString());
		retValue.put(Constants.TOPIC_ADDED_BY_FILTER_VAR, this.getTopicAddedBy());
		retValue.put(Constants.TOPIC_TITLE_FILTER_VAR, this.getTopicTitle());
		retValue.put(Constants.TOPIC_XML_FILTER_VAR, this.getTopicXML());
		retValue.put(Constants.TOPIC_DESCRIPTION_FILTER_VAR, this.getTopicText());
		retValue.put(Constants.TOPIC_STARTDATE_FILTER_VAR, this.getStartCreateDateString());
		retValue.put(Constants.TOPIC_ENDDATE_FILTER_VAR, this.getEndCreateDateString());
		retValue.put(Constants.TOPIC_LOGIC_FILTER_VAR, this.getLogic());
		retValue.put(Constants.TOPIC_HAS_RELATIONSHIPS, this.getHasRelationships() == null ? "" : this.getHasRelationships().toString());
		retValue.put(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS, this.getHasIncomingRelationships() == null ? "" : this.getHasIncomingRelationships().toString());
		retValue.put(Constants.TOPIC_RELATED_TO, this.getTopicRelatedTo() == null ? "" : this.getTopicRelatedTo().toString());
		retValue.put(Constants.TOPIC_RELATED_FROM, this.getTopicRelatedFrom() == null ? "" : this.getTopicRelatedFrom().toString());
		return retValue;
	}

	public static Map<String, String> getFilterNames()
	{
		final Map<String, String> retValue = new HashMap<String, String>();
		retValue.put(Constants.TOPIC_TEXT_SEARCH_FILTER_VAR, Constants.TOPIC_TEXT_SEARCH_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_IDS_FILTER_VAR, Constants.TOPIC_IDS_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_XML_FILTER_VAR, Constants.TOPIC_XML_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_ADDED_BY_FILTER_VAR, Constants.TOPIC_ADDED_BY_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_TITLE_FILTER_VAR, Constants.TOPIC_TITLE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_DESCRIPTION_FILTER_VAR, Constants.TOPIC_DESCRIPTION_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_STARTDATE_FILTER_VAR, Constants.TOPIC_STARTDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_ENDDATE_FILTER_VAR, Constants.TOPIC_ENDDATE_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_LOGIC_FILTER_VAR, Constants.TOPIC_LOGIC_FILTER_VAR_DESC);
		retValue.put(Constants.TOPIC_HAS_RELATIONSHIPS, Constants.TOPIC_HAS_RELATIONSHIPS_DESC);
		retValue.put(Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS, Constants.TOPIC_HAS_INCOMING_RELATIONSHIPS_DESC);
		retValue.put(Constants.TOPIC_RELATED_TO, Constants.TOPIC_RELATED_TO_DESC);
		retValue.put(Constants.TOPIC_RELATED_FROM, Constants.TOPIC_RELATED_FROM_DESC);
		return retValue;
	}

	public Integer getTopicRelatedFrom()
	{
		return topicRelatedFrom;
	}

	public void setTopicRelatedFrom(final Integer topicRelatedFrom)
	{
		this.topicRelatedFrom = topicRelatedFrom;
	}

	public Boolean getHasIncomingRelationships()
	{
		return hasIncomingRelationships;
	}

	public void setHasIncomingRelationships(final Boolean hasIncomingRelationships)
	{
		if (hasIncomingRelationships == null || !hasIncomingRelationships)
		{
			this.minimumIncomingRelationshipCount = null;
			this.hasIncomingRelationships = null;
		}
		else
		{
			this.minimumIncomingRelationshipCount = 1;
			this.hasIncomingRelationships = true;
		}
	}

	public Integer getMinimumIncomingRelationshipCount()
	{
		return minimumIncomingRelationshipCount;
	}

	public void setMinimumIncomingRelationshipCount(final Integer minimumIncomingRelationshipCount)
	{
		this.minimumIncomingRelationshipCount = minimumIncomingRelationshipCount;
	}

	public String getTopicTextSearch()
	{
		return topicTextSearch;
	}

	public void setTopicTextSearch(final String topicTextSearch)
	{
		this.topicTextSearch = topicTextSearch.trim();
	}

	public List<Integer> getTopicTextSearchIDs()
	{
		if (this.topicTextSearch == null || this.topicTextSearch.length() == 0)
			return null;

		return EntityUtilities.getTextSearchTopicMatch(this.topicTextSearch);
	}

}
