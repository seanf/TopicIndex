package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tagToTagRelationshipHome")
public class TagToTagRelationshipHome extends EntityHome<TagToTagRelationship> 
{


	public void setTagToTagRelationshipTagToTagRelationshipType(Integer id) {
		setId(id);
	}

	public Integer getTagToTagRelationshipTagToTagRelationshipType() {
		return (Integer) getId();
	}

	@Override
	protected TagToTagRelationship createInstance() {
		TagToTagRelationship tagToTagRelationship = new TagToTagRelationship();
		return tagToTagRelationship;
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

	public TagToTagRelationship getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<TagToTag> getTagToTags() {
		return getInstance() == null ? null : new ArrayList<TagToTag>(
				getInstance().getTagToTags());
	}

}
