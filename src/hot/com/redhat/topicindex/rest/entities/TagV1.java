package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.RESTv1;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;

public class TagV1 extends BaseRestV1<Tag> 
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private BaseRestCollectionV1<CategoryV1, Category> tags = new BaseRestCollectionV1<CategoryV1, Category>();
	
	@Override
	public void initialize(final Tag entity, final String baseUrl, final ExpandData expand)
	{
		this.id = entity.getTagId();
		this.name = entity.getTagName();
		this.description = entity.getTagDescription();
		this.setExpand(new String[] {RESTv1.CATEGORIES_EXPANSION_NAME});
		
		if (expand.contains(RESTv1.CATEGORIES_EXPANSION_NAME))
		{
			final ExpandData secondLevelExpandData = expand.getNextLevel(RESTv1.CATEGORIES_EXPANSION_NAME);
			tags.initialize(CategoryV1.class, entity.getCategories(), RESTv1.CATEGORIES_EXPANSION_NAME, secondLevelExpandData, baseUrl);
		}
		else
		{
			tags.initialize(CategoryV1.class, entity.getCategories(), RESTv1.CATEGORIES_EXPANSION_NAME);
		}
		
		super.setLinks(baseUrl, RESTv1.TAG_URL_NAME, this.id);
	}
}
