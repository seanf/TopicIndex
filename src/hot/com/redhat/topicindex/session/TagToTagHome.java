package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tagToTagHome")
public class TagToTagHome extends EntityHome<TagToTag> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 4086243626005519499L;
	@In(create = true)
	TagToTagRelationshipHome tagToTagRelationshipHome;

	public void setTagToTagTagToTagId(Integer id) {
		setId(id);
	}

	public Integer getTagToTagTagToTagId() {
		return (Integer) getId();
	}

	@Override
	protected TagToTag createInstance() {
		TagToTag tagToTag = new TagToTag();
		return tagToTag;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		TagToTagRelationship tagToTagRelationship = tagToTagRelationshipHome
				.getDefinedInstance();
		if (tagToTagRelationship != null) {
			getInstance().setTagToTagRelationship(tagToTagRelationship);
		}
	}

	public boolean isWired() {
		if (getInstance().getTagToTagRelationship() == null)
			return false;
		return true;
	}

	public TagToTag getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
