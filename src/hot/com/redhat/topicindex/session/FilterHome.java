package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import java.util.ArrayList;
import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("filterHome")
public class FilterHome extends EntityHome<Filter> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 3970939888954447920L;

	public void setFilterFilterId(Integer id) {
		setId(id);
	}

	public Integer getFilterFilterId() {
		return (Integer) getId();
	}

	@Override
	protected Filter createInstance() {
		Filter filter = new Filter();
		return filter;
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

	public Filter getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

	public List<FilterTag> getFilterTags() {
		return getInstance() == null ? null : new ArrayList<FilterTag>(
				getInstance().getFilterTags());
	}

	public List<FilterCategory> getFilterCategories() {
		return getInstance() == null ? null : new ArrayList<FilterCategory>(
				getInstance().getFilterCategories());
	}

}
