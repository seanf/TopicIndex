package com.redhat.topicindex.rest.factory;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.entities.CategoryV1;
import com.redhat.topicindex.rest.entities.TagV1;

public class CategoryV1Factory implements RESTDataObjectFactory<CategoryV1, Category>
{

	@Override
	public CategoryV1 create(final Category entity, final String baseUrl, final String dataType, final String expand)
	{
		/* account for the fact that expand could be null */
		final String fixedExpand = expand == null ? "" : expand;
		
		return this.create(entity, baseUrl, dataType, new ExpandData(fixedExpand));
	}

	@Override
	public CategoryV1 create(final Category entity, final String baseUrl, final String dataType, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		final CategoryV1 retValue = new CategoryV1();
		
		retValue.setId(entity.getCategoryId());
		retValue.setName(entity.getCategoryName());
		retValue.setDescription(entity.getCategoryDescription());
		retValue.setMutuallyExclusive(entity.isMutuallyExclusive());
		retValue.setSort(entity.getCategorySort());
		retValue.setExpand(new String[]	{ RESTv1.TAGS_EXPANSION_NAME });
		
		if (expand.contains(RESTv1.TAGS_EXPANSION_NAME))
		{
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl));
		}
		else
		{
			retValue.setTags(new RESTDataObjectCollectionFactory<TagV1, Tag>().create(new TagV1Factory(), entity.getTags(), RESTv1.TAGS_EXPANSION_NAME, dataType));
		}
		
		retValue.setLinks(baseUrl, RESTv1.CATEGORY_URL_NAME, dataType, retValue.getId());
		
		return retValue;
	}

	@Override
	public void sync(Category entity, CategoryV1 dataObject)
	{
		// TODO Auto-generated method stub
		
	}

}
