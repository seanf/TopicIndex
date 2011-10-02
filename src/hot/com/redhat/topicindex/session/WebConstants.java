package com.redhat.topicindex.session;

import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.utils.Constants;

@Name("webConstants")
public class WebConstants 
{
	public String getBuild() {return Constants.BUILD;}
}
