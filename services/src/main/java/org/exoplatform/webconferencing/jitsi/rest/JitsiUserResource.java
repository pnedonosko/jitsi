package org.exoplatform.webconferencing.jitsi.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/jitsi/user")
public class JitsiUserResource implements ResourceContainer {

  /**
   * Content.
   *
   * @param uriInfo the uri info
   * @param request the request
   * @param context the context
   * @param fileId the file id
   * @return the response
   */
  @GET
  @Path("/me")
  public Response me(@Context HttpServletRequest request) {

    return Response.status(Status.OK)
                   .entity("{\"username\": \"" + request.getRemoteUser() + "\"}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();

  }
}
