package org.exoplatform.webconferencing.jitsi.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

@Path("/jitsi")
public class JitsiContextResource implements ResourceContainer {

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
  @Path("/context")
  public Response me(@Context HttpServletRequest request) {
    // TODO: return context info to init comet
    String userId = null;
    ConversationState state = ConversationState.getCurrent();
    
    if (state != null) {
      userId = state.getIdentity().getUserId();
    }

    return Response.status(Status.OK)
                   .entity("{\"username\": \"" + userId + "\"}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();

  }
}
