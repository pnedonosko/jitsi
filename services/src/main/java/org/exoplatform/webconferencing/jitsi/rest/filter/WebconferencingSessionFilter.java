package org.exoplatform.webconferencing.jitsi.rest.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.webconferencing.WebConferencingService;
import org.exoplatform.webconferencing.jitsi.JitsiProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class WebconferencingSessionFilter extends AbstractFilter implements Filter {

  /** The Constant LOG. */
  private static final Log    LOG                           = ExoLogger.getLogger(WebconferencingSessionFilter.class);

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String INTERNAL_AUTH_TOKEN_ATTRIBUTE = "X-Exoplatform-Internal-Auth";

  /** The Constant INTERNAL_AUTH. */
  private static final String INTERNAL_AUTH                 = "internal-auth";

  /**
   * Do filter.
   *
   * @param request the request
   * @param response the response
   * @param chain the chain
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    WebConferencingService webConferencing =
                                           (WebConferencingService) getContainer().getComponentInstanceOfType(WebConferencingService.class);
    if (validAuthToken(req)) {
      String webconfToken = getCookie(req, WebConferencingService.SESSION_TOKEN_COOKIE);
      Claims claims = getClaims(webconfToken, webConferencing.getSecretKey());
      if (claims != null && claims.containsKey("username")) {
        try {
          String username = String.valueOf(claims.get("username"));
          ExoContainer container = getContainer();
          ExoContainerContext.setCurrentContainer(container);
          ConversationState state = createState(username);
          ConversationState.setCurrent(state);
          SessionProviderService sessionProviders =
                                                  (SessionProviderService) getContainer().getComponentInstanceOfType(SessionProviderService.class);

          SessionProvider userProvider = new SessionProvider(state);
          sessionProviders.setSessionProvider(null, userProvider);
          chain.doFilter(request, response);
          try {
            ConversationState.setCurrent(null);
          } catch (Exception e) {
            LOG.warn("An error occured while cleaning the ConversationState", e);
          }
          try {
            ExoContainerContext.setCurrentContainer(null);
          } catch (Exception e) {
            LOG.warn("An error occured while cleaning the ThreadLocal", e);
          }
        } catch (Exception e) {
          LOG.warn("Cannot set ConversationState based on provided token", e);
          chain.doFilter(request, response);
        }
      } else {
        chain.doFilter(request, response);
      }
    } else {
      LOG.warn("The request doesn't contain valid access token for internal auth");
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write("{\"error\":\"The request is not authorized\"}");
    }

  }

  /**
   * Destroy.
   */
  @Override
  public void destroy() {
    // TODO Auto-generated method stub
  }

  /**
   * Gets the cookie.
   *
   * @param request the request
   * @param name the name
   * @return the cookie
   */
  private String getCookie(HttpServletRequest request, String name) {
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(name)) {
        return cookie.getValue();
      }
    }
    return null;
  }

  /**
   * Gets the claims.
   *
   * @param token the token
   * @return the claims
   */
  @SuppressWarnings("unchecked")
  private Claims getClaims(String token, String secret) {
    if (token == null || token.trim().isEmpty()) {
      return null;
    }
    try {
      Jws<Claims> jws = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes())).parseClaimsJws(token);
      return jws.getBody();
    } catch (Exception e) {
      LOG.warn("Couldn't validate the token: {} : {}", token, e.getMessage());
      return null;
    }
  }

  /**
   * Creates the state.
   *
   * @param userId the user id
   * @return the conversation state
   */
  private ConversationState createState(String userId) {
    Identity userIdentity = userIdentity(userId);
    if (userIdentity != null) {
      ConversationState state = new ConversationState(userIdentity);
      // Keep subject as attribute in ConversationState.
      state.setAttribute(ConversationState.SUBJECT, userIdentity.getSubject());
      return state;
    }
    LOG.warn("User identity not found " + userId + " for setting conversation state");
    return null;
  }

  /**
   * Find or create user identity.
   *
   * @param userId the user id
   * @return the identity can be null if not found and cannot be created via
   *         current authenticator
   */
  protected Identity userIdentity(String userId) {
    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer().getComponentInstanceOfType(IdentityRegistry.class);
    Authenticator authenticator = (Authenticator) getContainer().getComponentInstanceOfType(Authenticator.class);
    Identity userIdentity = identityRegistry.getIdentity(userId);
    if (userIdentity == null) {
      // We create user identity by authenticator, but not register it in the
      // registry
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("User identity not registered, trying to create it for: " + userId);
        }
        userIdentity = authenticator.createIdentity(userId);
      } catch (Exception e) {
        LOG.warn("Failed to create user identity: " + userId, e);
      }
    }
    return userIdentity;
  }

  protected boolean validAuthToken(HttpServletRequest request) {
    if (request.getAttribute(INTERNAL_AUTH_TOKEN_ATTRIBUTE) != null) {
      JitsiProvider jitsiProvider = (JitsiProvider) getContainer().getComponentInstanceOfType(JitsiProvider.class);
      String token = String.valueOf(request.getAttribute(INTERNAL_AUTH_TOKEN_ATTRIBUTE));
      Map<String, Object> claims = getClaims(token, jitsiProvider.getInternalAuthSecret());
      if (claims != null && claims.containsKey("action")) {
        String action = String.valueOf(claims.get("action"));
        if (INTERNAL_AUTH.equals(action)) {
          return true;
        }
      }
    }

    return false;
  }
}
