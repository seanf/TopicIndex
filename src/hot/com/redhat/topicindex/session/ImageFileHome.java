package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("imageFileHome")
public class ImageFileHome extends EntityHome<ImageFile>
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 554234315046093282L;

	public void setImageFileImageFileId(Integer id) {
		setId(id);
	}

	public Integer getImageFileImageFileId() {
		return (Integer) getId();
	}

	@Override
	protected ImageFile createInstance() {
		ImageFile imageFile = new ImageFile();
		return imageFile;
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

	public ImageFile getDefinedInstance() {
		return isIdDefined() ? getInstance() : null;
	}

}
