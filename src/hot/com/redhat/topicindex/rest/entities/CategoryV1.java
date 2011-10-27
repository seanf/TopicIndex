package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class CategoryV1 extends BaseRestV1<Category>
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private boolean mutuallyExclusive = false;
	private Integer sort = null;
	private BaseRestCollectionV1<TagV1, Tag> tags = new BaseRestCollectionV1<TagV1, Tag>();
	
	@Override
	public void initialize(final Category entity, final String baseUrl, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		
		this.id = entity.getCategoryId();
		this.name = entity.getCategoryName();
		this.description = entity.getCategoryDescription();
		this.mutuallyExclusive = entity.isMutuallyExclusive();
		this.sort = entity.getCategorySort();
		this.setExpand(new String[]	{ RESTv1.CATEGORIES_EXPANSION_NAME });
		
		if (expand.contains(RESTv1.CATEGORIES_EXPANSION_NAME))
		{
			final ExpandData secondLevelExpandData = expand.getNextLevel(RESTv1.CATEGORIES_EXPANSION_NAME);
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.CATEGORIES_EXPANSION_NAME, secondLevelExpandData, baseUrl);
		}
		else
		{
			tags.initialize(TagV1.class, entity.getTags(), RESTv1.CATEGORIES_EXPANSION_NAME);
		}
		
		super.setLinks(baseUrl, RESTv1.CATEGORY_URL_NAME, this.id);
	}
	
}
