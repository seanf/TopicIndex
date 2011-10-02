package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tagToCategoryHome")
public class TagToCategoryHome extends EntityHome<TagToCategory> 
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 708140383044003828L;

	public void setTagToCategoryTagToCategoryId(Integer id) {
		setId(id);
	}

	public Integer getTagToCategoryTagToCategoryId() {
		return (Integer) getId();
	}

	@Override
	protected TagToCategory createInstance() {
		TagToCategory tagToCategory = new TagToCategory();
		return tagToCategory;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
	}

	public boolean isWired() {
		return true;
	}

	public TagToCategory getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
