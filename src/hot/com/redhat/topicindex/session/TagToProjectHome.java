package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("tagToProjectHome")
public class TagToProjectHome extends EntityHome<TagToProject> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 2347238814490831215L;
	@In(create = true)
	ProjectHome projectHome;

	public void setTagToProjectTagToProjectId(Integer id) {
		setId(id);
	}

	public Integer getTagToProjectTagToProjectId() {
		return (Integer) getId();
	}

	@Override
	protected TagToProject createInstance() {
		TagToProject tagToProject = new TagToProject();
		return tagToProject;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		Project project = projectHome.getDefinedInstance();
		if (project != null) {
			getInstance().setProject(project);
		}
	}

	public boolean isWired() {
		if (getInstance().getProject() == null)
			return false;
		return true;
	}

	public TagToProject getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
