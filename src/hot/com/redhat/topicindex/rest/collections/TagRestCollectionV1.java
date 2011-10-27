package com.redhat.topicindex.rest.collections;

import java.util.List;

import com.redhat.topicindex.entity.Tag;
import com.redhat.topicindex.rest.ExpandData;
import com.redhat.topicindex.rest.entities.RESTv1;
import com.redhat.topicindex.rest.entities.TagV1;

public class TagRestCollectionV1 extends BaseRestCollectionV1<TagV1, Tag>
{

	@Override
	public void initialize(final List<Tag> entities, final ExpandData expand, final String baseUrl)
	{
		assert entities != null : "Parameter entities can not be null";

		this.setSize(entities.size());
		this.setExpand(RESTv1.TAGS_EXPANSION_NAME);

		if (expand != null)
		{
			assert baseUrl != null : "Parameter baseUrl can not be null if parameter expand is not null";
			
			final TagV1[] tagsArray = new TagV1[entities.size()];

			int i = 0;
			for (final Tag tag : entities)
			{
				final TagV1 tagV1 = new TagV1();
				tagV1.initialize(tag, baseUrl, "tag", expand);
				tagsArray[i] = tagV1;
				++i;
			}

			this.setItems(tagsArray);
		}
	}

}
