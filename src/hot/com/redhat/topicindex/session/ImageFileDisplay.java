package com.redhat.topicindex.session;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.web.RequestParameter;

import com.redhat.ecs.commonutils.ExceptionUtilities;
import com.redhat.topicindex.entity.ImageFile;

@Name("imageFileDisplay")
public class ImageFileDisplay
{
	@In
	private EntityManager entityManager;

	@In(value = "#{facesContext.externalContext}")
	private ExternalContext extCtx;

	@In(value = "#{facesContext}")
	private FacesContext facesContext;

	//@RequestParameter
	private String imageFileId;

	public void download()
	{
		try
		{
			final Integer imageFileIdInteger = Integer.parseInt(imageFileId.trim());
			
			final ImageFile file = entityManager.find(ImageFile.class, imageFileIdInteger);
			
			final HttpServletResponse response = (HttpServletResponse)extCtx.getResponse();
			
			response.setContentType(file.getMimeType());
			//response.addHeader("Content-disposition", "attachment; filename=\"" + file.getName() +"\"");
		
		
			final ServletOutputStream os = response.getOutputStream();
			os.write(file.getImageData());
			os.flush();
			os.close();
			facesContext.responseComplete();
		}
		catch (final Exception ex)
		{
			ExceptionUtilities.handleException(ex);
		}
	}

	public String getImageFileId()
	{
		return imageFileId;
	}

	public void setImageFileId(final String imageFileId)
	{
		this.imageFileId = imageFileId;
	}

}
