package com.redhat.topicindex.session;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;

import com.redhat.topicindex.entity.Topic;

@Name("fileResourceProvider")
@Scope(ScopeType.EVENT)
public class FileResourceProvider
{
   @RequestParameter
   private Integer topicId;
   private String fileName;
   private byte[] data;
   
   public String getFileName() 
   {
	   return fileName;
   }

   public void setFileName(final String fileName) 
   {
	   this.fileName = fileName;
   }

   public byte[] getData() 
   {
	   return data;
   }

   public void setData(final byte[] data) 
   {
	   this.data = data;
   }

   @Create
   public void create()
   {
	   final EntityManager entityManager = (EntityManager)Component.getInstance("entityManager");	 
	   final Topic topic = entityManager.find(Topic.class, topicId);
	   this.fileName = topicId + ".xml";
	   
	   if (topic != null)
	   {		   
		   this.data = topic.getTopicXML().getBytes();
	   }
	   
   }
}