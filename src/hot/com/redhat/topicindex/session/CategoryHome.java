package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("categoryHome")
public class CategoryHome extends EntityHome<Category> 
{
	private static final long serialVersionUID = 6943432713479412363L;

	public void setCategoryCategoryId(Integer id) {
		setId(id);
	}

	public Integer getCategoryCategoryId() {
		return (Integer) getId();
	}

	@Override
	protected Category createInstance() {
		Category category = new Category();
		return category;
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

	public Category getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
