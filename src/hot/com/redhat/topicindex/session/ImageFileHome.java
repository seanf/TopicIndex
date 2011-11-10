package com.redhat.topicindex.session;

import com.redhat.topicindex.entity.*;
import com.redhat.topicindex.utils.Constants;
import com.redhat.topicindex.utils.SkynetExceptionUtilities;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityHome;

@Name("imageFileHome")
public class ImageFileHome extends EntityHome<ImageFile> implements DisplayMessageInterface
{

	/** Serializable version identifier */
	private static final long serialVersionUID = 554234315046093282L;
	/** The message to be displayed to the user */
	private String displayMessage;

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

	public String getDisplayMessage()
	{
		return displayMessage;
	}

	public void setDisplayMessage(String displayMessage)
	{
		this.displayMessage = displayMessage;
	}
	
	@Override
	public String persist()
	{
		try
		{
			return super.persist();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error persisting a ImageFile entity");
			this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
		}
		
		return null;
	}
	
	@Override
	public String update()
	{
		try
		{
			return super.update();
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error persisting a ImageFile entity");
			this.setDisplayMessage("The image could not be saved. " + Constants.GENERIC_ERROR_INSTRUCTIONS);
		}
		
		return null;
	}

}
