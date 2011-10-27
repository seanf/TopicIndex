package com.redhat.topicindex.rest.entities;

import com.redhat.topicindex.entity.Category;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.collections.BaseRestCollectionV1;
import com.redhat.topicindex.rest.collections.TagRestCollectionV1;

public class CategoryV1 extends BaseRestV1<Category>
{
	private Integer id = null;
	private String name = null;
	private String description = null;
	private boolean mutuallyExclusive = false;
	private Integer sort = null;
	private TagRestCollectionV1 tags = new TagRestCollectionV1();
	
	@Override
	public void initialize(final Category entity, final String baseUrl, final String restBasePath, final ExpandData expand)
	{
		assert entity != null : "Parameter topic can not be null";
		assert baseUrl != null : "Parameter baseUrl can not be null";
		assert restBasePath != null : "Parameter restBasePath can not be null";
		
		this.id = entity.getCategoryId();
		this.name = entity.getCategoryName();
		this.description = entity.getCategoryDescription();
		this.mutuallyExclusive = entity.isMutuallyExclusive();
		this.sort = entity.getCategorySort();
		this.setExpand(new String[]	{ RESTv1.TAGS_EXPANSION_NAME });
		
		if (expand.contains(RESTv1.TAGS_EXPANSION_NAME))
		{
			final ExpandData secondLevelExpandData = expand.getNextLevel(RESTv1.TAGS_EXPANSION_NAME);
			tags.initialize(entity.getTags(), secondLevelExpandData, baseUrl);
		}
		else
		{
			tags.initialize(entity.getTags());
		}
	}
	
}
