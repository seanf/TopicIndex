package com.redhat.topicindex.rest.factory;

import javax.persistence.EntityManager;

import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.BaseRESTv1;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.entities.TagV1;

public class ProjectV1Factory implements RESTDataObjectFactory<ProjectV1, Project>
{

	@Override
	public ProjectV1 create(final Project entity, final String baseUrl, final String dataType, final String expand)
	{
		/* account for the fact that expand could be null */
		final String fixedExpand = expand == null ? "" : expand;
		
		return this.create(entity, baseUrl, dataType, new ExpandData(fixedExpand));
	}

	@Override
	public ProjectV1 create(final Project entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final ProjectV1 retValue = new ProjectV1();

		retValue.setId(entity.getProjectId());
		retValue.setDescription(entity.getProjectDescription());
		retValue.setName(entity.getProjectName());
		retValue.setExpand(new String[]	{ BaseRESTv1.TAGS_EXPANSION_NAME });

		if (expand.contains(BaseRESTv1.TAGS_EXPANSION_NAME))
		{			
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));
		}
		else
		{
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), BaseRESTv1.TAGS_EXPANSION_NAME, dataType));
		}

		retValue.setLinks(baseUrl, BaseRESTv1.TOPIC_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	public void sync(final EntityManager entityManager, final Project entity, final ProjectV1 dataObject)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Project create(EntityManager entityManager, ProjectV1 dataObject)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
