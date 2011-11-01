package com.redhat.topicindex.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.redhat.topicindex.entity.Project;
import com.redhat.topicindex.rest.entities.ProjectV1;
import com.redhat.topicindex.rest.factory.ProjectV1Factory;

@Path("/1")
public class ProjectRESTv1 extends RESTv1
{
	@GET
	@Path("/project/get/json/{id}")
	@Produces("application/json")
	public ProjectV1 getProject(@PathParam("id") final Integer id, @QueryParam("expand") final String expand)
	{
		assert id != null : "The id parameter can not be null";
						
		return getJSONResource(Project.class, new ProjectV1Factory(), id, expand);
	}
}
