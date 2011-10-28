package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class ProjectV1 extends BaseRestV1<Project>
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private BaseRestCollectionV1<TagV1, Tag> tags = new BaseRestCollectionV1<TagV1, Tag>();
	
	@Override
	public void initialize(final Project entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";

		this.id = entity.getProjectId();
		this.description = entity.getProjectDescription();
		this.name = entity.getProjectName();
		
		this.setExpand(new String[]	{ RESTv1.TAGS_EXPANSION_NAME });

		if (expand.contains(RESTv1.TAGS_EXPANSION_NAME))
		{			
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl);
		}
		else
		{
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType);
		}

		super.setLinks(baseUrl, RESTv1.TOPIC_URL_NAME, dataType, this.id);
	}

}
