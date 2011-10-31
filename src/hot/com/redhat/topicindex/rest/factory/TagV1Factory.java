package com.redhat.topicindex.rest.factory;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.TagV1;

public class TagV1Factory implements RESTDataObjectFactory<TagV1, Tag>
{

	@Override
	public TagV1 create(final Tag entity, final String baseUrl, final String dataType, final String expand)
	{
		/* account for the fact that expand could be null */
		final String fixedExpand = expand == null ? "" : expand;
		
		return this.create(entity, baseUrl, dataType, new ExpandData(fixedExpand));
	}

	@Override
	public TagV1 create(final Tag entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final TagV1 retValue = new TagV1();
		
		retValue.setId(entity.getTagId());
		retValue.setName(entity.getTagName());
		retValue.setDescription(entity.getTagDescription());
		retValue.setExpand(new String[] {RESTv1.CATEGORIES_EXPANSION_NAME});
		
		if (expand.contains(RESTv1.CATEGORIES_EXPANSION_NAME))
		{
			retValue.setCategories(new RESTDataObjectCollectionFactory<CategoryV1, Category>().create(new CategoryV1Factory(), entity.getCategories(), RESTv1.CATEGORIES_EXPANSION_NAME, dataType, expand, baseUrl));
		}
		else
		{
			retValue.setCategories(new RESTDataObjectCollectionFactory<CategoryV1, Category>().create(new CategoryV1Factory(), entity.getCategories(), RESTv1.CATEGORIES_EXPANSION_NAME, dataType));
		}
		
		retValue.setLinks(baseUrl, RESTv1.TAG_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	public void sync(Tag entity, TagV1 dataObject)
	{
		// TODO Auto-generated method stub
		
	}

}
