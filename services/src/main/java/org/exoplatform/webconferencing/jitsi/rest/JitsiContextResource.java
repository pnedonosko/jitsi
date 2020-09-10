package org.exoplatform.webconferencing.jitsi.rest;

import static org.exoplatform.webconferencing.Utils.getCurrentContext;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gatein.portal.controller.resource.ResourceRequestHandler;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webconferencing.ContextInfo;
import org.exoplatform.webconferencing.IdentityStateException;
import org.exoplatform.webconferencing.UserInfo;
import org.exoplatform.webconferencing.WebConferencingService;

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
  @Path("/context")
  public Response context(@Context HttpServletRequest request) {
    // TODO: return context info to init comet
    String userId = null;
    UserInfo userInfo = null;
    ConversationState state = ConversationState.getCurrent();
    ContextInfo context = null;
    if (state != null) {
      userId = state.getIdentity().getUserId();
      try {
        userInfo = webconferencing.getUserInfo(userId);
        context = getCurrentContext(userId, request.getLocale());
      } catch (IdentityStateException e) {
        LOG.warn("Cannot find identity with id: {}", userId);
      }
    }
    return Response.status(Status.OK).entity(new InitContext(userId, userInfo, context)).type(MediaType.APPLICATION_JSON).build();
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
    return Response.status(Status.OK)
                   .entity(ResourceRequestHandler.VERSION)
                   .build();
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
