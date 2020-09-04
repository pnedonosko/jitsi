package org.exoplatform.webconferencing.jitsi.rest;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.web.AbstractFilter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.web.filter.Filter;
import org.exoplatform.webconferencing.WebConferencingService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

public class WebconferencingSessionFilter extends AbstractFilter implements Filter {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(WebconferencingSessionFilter.class);

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
    // TODO: read cookie, parse token, set convo state, do chain, clear convo state
    // TODO: configure this filter in .xml
    /*
    if (req.getRemoteUser() == null) {
    String webconfToken = getCookie(req, WebConferencingService.SESSION_TOKEN_COOKIE);
    if (webconfToken == null || webconfToken.trim().isEmpty()) {
      // Forward to login page
    } else {
      Claims claims = getClaims(webconfToken);
      if (claims != null && claims.containsKey("username")) {
        String username = String.valueOf(claims.get("username"));
        if (username != null) {
          ConversationState state = createState(username);
          ConversationState.setCurrent(state);
          SessionProviderService sessionProviders =
                                                  (SessionProviderService) getContainer().getComponentInstanceOfType(SessionProviderService.class);
    
          SessionProvider userProvider = new SessionProvider(state);
          sessionProviders.setSessionProvider(null, userProvider);
          // Do forwarding
         
          try {
            ConversationState.setCurrent(null);
          } catch (Exception e) {
            LOG.warn("An error occured while cleaning the ThreadLocal", e);
          }
        }
      }
    }
    } else {
    forwardInternally(req, resp);
    }
     */
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
  private Claims getClaims(String token) {
    WebConferencingService webConferencing =
                                           (WebConferencingService) getContainer().getComponentInstanceOfType(WebConferencingService.class);
    try {
      Jws<Claims> jws = Jwts.parser()
                            .setSigningKey(Keys.hmacShaKeyFor(webConferencing.getSecretKey().getBytes()))
                            .parseClaimsJws(token);

      return jws.getBody();
    } catch (Exception e) {
      LOG.warn("Couldn't validate the token: {} : {}", token, e.getMessage());
      throw new IllegalArgumentException("The provided token is not valid");
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
}
