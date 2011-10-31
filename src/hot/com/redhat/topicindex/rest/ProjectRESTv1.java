package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.rest.entities.ProjectV1;

public class ProjectRESTv1 extends RESTv1
{
	@GET
	@Path("/project/get/json/{id}")
	public Response getProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getResource(Project.class, new ProjectV1(), id, "application/json", expand);
	}
}
