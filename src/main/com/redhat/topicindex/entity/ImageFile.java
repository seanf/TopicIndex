package com.redhat.topicindex.entity;

import static javax.persistence.GenerationType.IDENTITY;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.ImageIcon;

import org.apache.commons.codec.binary.Base64;
import org.hibernate.envers.Audited;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

import com.redhat.ecs.commonutils.ExceptionUtilities;

@Entity
@Audited
@Table(name = "ImageFile", catalog = "Skynet")
public class ImageFile implements java.io.Serializable 
{
	private static final int THUMBNAIL_SIZE = 64;
	private static final long serialVersionUID = -3885332582642450795L;
	private Integer imageFileId;
	private String originalFileName;
	private byte[] imageData;
	private byte[] thumbnail;
	private byte[] imageDataBase64;
	private String description;

	public ImageFile() 
	{
	}

	public ImageFile(
			final Integer imageFileId, 
			final String originalFileName, 
			final byte[] imageData) 
	{
		this.imageFileId = imageFileId;
		this.originalFileName = originalFileName;
		this.imageData = imageData;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "ImageFileID", unique = true, nullable = false)
	public Integer getImageFileId() 
	{
		return this.imageFileId;
	}

	public void setImageFileId(final Integer imageFileId) 
	{
		this.imageFileId = imageFileId;
	}

	@Column(name = "OriginalFileName", nullable = false, length = 512)
	@NotNull
	@Length(max = 512)
	public String getOriginalFileName() 
	{
		return this.originalFileName;
	}

	public void setOriginalFileName(final String originalFileName) 
	{
		this.originalFileName = originalFileName;
	}
	
	@Column(name = "ImageDataBase64", nullable = false)
	@NotNull
	public byte[] getImageDataBase64() 
	{
		return this.imageDataBase64;
	}

	public void setImageDataBase64(final byte[] imageDataBase64) 
	{
		this.imageDataBase64 = imageDataBase64;
	}
	
	@Transient
	public String getImageDataBase64String()
	{
		return this.imageDataBase64 == null ? "" : new String(this.imageDataBase64);
	}

	@Column(name = "ImageData", nullable = false)
	@NotNull
	public byte[] getImageData() 
	{
		return this.imageData;
	}

	public void setImageData(final byte[] imageData) 
	{
		this.imageData = imageData;
	}
	
	@Column(name = "ThumbnailData", nullable = false)
	@NotNull
	public byte[] getThumbnailData() 
	{
		return this.thumbnail;
	}

	public void setThumbnailData(final byte[] thumbnail) 
	{
		this.thumbnail = thumbnail;
	}
	
	@Transient
	public String getThumbnailDataString()
	{
		return this.thumbnail == null ? "" : new String(this.thumbnail);
	}
	
	@PrePersist
	@PreUpdate
	private void updateImageData()
	{
		this.thumbnail = createImage(true);
		this.imageDataBase64 = createImage(false);
	}
	
	//@Column(name = "Description", length = 65535)
	@Column(name = "Description", columnDefinition="TEXT")
	@Length(max = 65535)
	public String getDescription() 
	{
		return this.description;
	}
	
	public void setDescription(final String description)
	{
		this.description = description;
	}
	
	private byte[] createImage(final boolean resize) 
	{
	    try 
	    {
	        final ImageIcon imageIcon = new ImageIcon(this.imageData);
	        final Image inImage = imageIcon.getImage();
	        
	        double scale = 1.0d;
	        if (resize)
	        {
		        // the final image will be at most THUMBNAIL_SIZE pixels high and/or wide
		        final double heightScale = (double) THUMBNAIL_SIZE / (double) inImage.getHeight(null);
		        final double widthScale = (double) THUMBNAIL_SIZE / (double) inImage.getWidth(null);	        
		        scale = Math.min(heightScale, widthScale);		       
	        }
	        
	        final int scaledW = (int) (scale * inImage.getWidth(null));
	        final int scaledH = (int) (scale * inImage.getHeight(null));
	        
	        final AffineTransform tx = new AffineTransform();
	        final BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);

	        if (scale < 1.0d)
	            tx.scale(scale, scale);

	        final Graphics2D g2d = outImage.createGraphics();
	        g2d.drawImage(inImage, tx, null);
	        g2d.dispose();  

	        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        ImageIO.write(outImage, "JPG", baos);
	        final byte[] bytesOut = baos.toByteArray();
	        
	        return Base64.encodeBase64(bytesOut);
	    } 
	    catch (final Exception ex) 
	    {
	    	ExceptionUtilities.handleException(ex);
	    }
	    
	    return null;
	}

	@Transient
	public String getDocbookFileName() 
	{
		if (this.originalFileName != null && this.imageFileId != null)
		{
			final int extensionIndex = this.originalFileName.lastIndexOf(".");
			if (extensionIndex != -1)
				return this.imageFileId +  this.originalFileName.substring(extensionIndex);
		}
		
		return "";
	}
	
	@Transient
	public String getMimeType() 
	{
		final int lastPeriodIndex = this.originalFileName.lastIndexOf(".");
		if (lastPeriodIndex != -1 && lastPeriodIndex < this.originalFileName.length() - 1)
		{
			final String extension = this.originalFileName.substring(lastPeriodIndex + 1);
			if (extension.equalsIgnoreCase("JPG"))
				return "image/jpeg";
			if (extension.equalsIgnoreCase("GIF"))
				return "image/gif";
			if (extension.equalsIgnoreCase("PNG"))
				return "image/png";
			if (extension.equalsIgnoreCase("SVG"))
				return "image/svg+xml";
		}
			
			
		return "application/octet-stream";
	}

}
