package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("filterCategoryHome")
public class FilterCategoryHome extends EntityHome<FilterCategory> 
{
	/** Serializable version identifier */
	private static final long serialVersionUID = 3046040087402567711L;
	@In(create = true)
	private FilterHome filterHome;

	public void setFilterCategoryFilterCategoryId(Integer id) {
		setId(id);
	}

	public Integer getFilterCategoryFilterCategoryId() {
		return (Integer) getId();
	}

	@Override
	protected FilterCategory createInstance() {
		FilterCategory filterCategory = new FilterCategory();
		return filterCategory;
	}

	public void load() {
		if (isIdDefined()) {
			wire();
		}
	}

	public void wire() {
		getInstance();
		Filter filter = filterHome.getDefinedInstance();
		if (filter != null) {
			getInstance().setFilter(filter);
		}
	}

	public boolean isWired() {
		if (getInstance().getFilter() == null)
			return false;
		return true;
	}

	public FilterCategory getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
