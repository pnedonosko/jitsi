package org.exoplatform.webconferencing.jitsi.rest;

import static org.exoplatform.webconferencing.Utils.asJSON;
import static org.exoplatform.webconferencing.Utils.getCurrentContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gatein.portal.controller.resource.ResourceRequestHandler;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.webconferencing.ContextInfo;
import org.exoplatform.webconferencing.IdentityStateException;
import org.exoplatform.webconferencing.UserInfo;
import org.exoplatform.webconferencing.WebConferencingService;
import org.exoplatform.webconferencing.jitsi.JitsiProvider;

/**
 * The Class JitsiContextResource.
 */
@Path("/jitsi")
public class JitsiContextResource implements ResourceContainer {

  /** The Constant LOG. */
  private static final Log             LOG = ExoLogger.getLogger(JitsiContextResource.class);

  /** The Constant webconferencing. */
  private final WebConferencingService webconferencing;

  /**
   * Instantiates a new jitsi context resource.
   *
   * @param webconferencing the webconferencing
   */
  public JitsiContextResource(WebConferencingService webconferencing) {
    this.webconferencing = webconferencing;
  }

  /**
   * Content.
   *
   * @param request the request
   * @return the response
   */
  @GET
  @Path("/context/{userId}")
  public Response context(@Context HttpServletRequest request, @PathParam("userId") String userId) {
    // TODO: return context info to init comet
    ContextInfo context = getCurrentContext(userId, request.getLocale());
    return Response.status(Status.OK).entity(context).type(MediaType.APPLICATION_JSON).build();
  }

  @GET
  @Path("/settings")
  public Response settings() {
    try {
      JitsiProvider provider = (JitsiProvider) webconferencing.getProvider(JitsiProvider.TYPE);
      if (provider != null) {
        return Response.status(Status.OK).entity(provider.getSettings()).type(MediaType.APPLICATION_JSON).build();
      }
    } catch (ClassCastException e) {
      LOG.error("Provider " + JitsiProvider.TYPE + " isn't an instance of " + JitsiProvider.class.getName(), e);
    }
    return Response.status(Status.INTERNAL_SERVER_ERROR)
                   .entity("{\"error\":\"Cannot find valid Jitsi provider \"}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();

  }

  // TODO: replace by default one
  /**
   * Content.
   *
   * @param request the request
   * @return the response
   */
  @GET
  @Path("/userinfo")
  public Response userInfo(@Context HttpServletRequest request) {
    ConversationState state = ConversationState.getCurrent();
    if (state != null && !state.getIdentity().getUserId().equals(IdentityConstants.ANONIM)) {
      String userId = state.getIdentity().getUserId();
      try {
        UserInfo userInfo = webconferencing.getUserInfo(userId);
        return Response.status(Status.OK).entity(userInfo).type(MediaType.APPLICATION_JSON).build();
      } catch (IdentityStateException e) {
        LOG.warn("Cannot find identity with id: {}", userId);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\":\"Cannot find identity with id: " + userId + "\"}")
                       .type(MediaType.APPLICATION_JSON)
                       .build();
      }
    }
    return Response.status(Status.UNAUTHORIZED)
                   .entity("{\"error\":\"Current user is not authorized\"}")
                   .type(MediaType.APPLICATION_JSON)
                   .build();

  }

  /**
   * Content.
   *
   * @param request the request
   * @return the response
   */
  @GET
  @Path("/resources/version")
  public Response resourcesVersion() {
    return Response.status(Status.OK).entity(ResourceRequestHandler.VERSION).build();
  }

  /**
   * The Class Context.
   */
  public class InitContext {

    /** The username. */
    private final String      username;

    /** The user info. */
    private final UserInfo    userInfo;

    /** The context. */
    private final ContextInfo context;

    /**
     * Instantiates a new context.
     *
     * @param username the username
     * @param userInfo the user info
     * @param context the context
     */
    public InitContext(String username, UserInfo userInfo, ContextInfo context) {
      this.username = username;
      this.userInfo = userInfo;
      this.context = context;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
      return username;
    }

    /**
     * Gets the user info.
     *
     * @return the user info
     */
    public UserInfo getUserInfo() {
      return userInfo;
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public ContextInfo getContext() {
      return context;
    }

  }
}
