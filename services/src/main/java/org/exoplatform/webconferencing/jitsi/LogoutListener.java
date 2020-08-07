package org.exoplatform.webconferencing.jitsi;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.webconferencing.WebConferencingService;

/**
 * The listener interface for receiving logout events.
 * The class that is interested in processing a logout
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addLogoutListener<code> method. When
 * the logout event occurs, that object's appropriate
 * method is invoked.
 *
 * @see LogoutEvent
 */
public class LogoutListener extends Listener<ConversationRegistry, ConversationState> {

  /** The Constant LOG. */
  protected static final Log  LOG              = ExoLogger.getLogger(LogoutListener.class);

  /** The Constant CLIENT_ID_COOKIE. */
  private static final String CLIENT_ID_COOKIE = "jitsi-client-id";

  /**
   * On event.
   *
   * @param event the event
   * @throws Exception the exception
   */
  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {

    ExoContainer container = ExoContainerContext.getCurrentContainer();
    WebConferencingService webConferencing = container.getComponentInstanceOfType(WebConferencingService.class);
    try {
      JitsiProvider provider = (JitsiProvider) webConferencing.getProvider(JitsiProvider.TYPE);
      PortalRequestContext prContext = Util.getPortalRequestContext();
      HttpServletRequest req = prContext.getRequest();
      String clientId = getClientIdCookie(req);
      LOG.info("Logout listener. User: " + event.getData().getIdentity().getUserId() + " ClientId: " + clientId);
      provider.removeClient(clientId);

      Cookie cookie = new Cookie(CLIENT_ID_COOKIE, "");
      cookie.setPath(req.getContextPath());
      cookie.setMaxAge(0);
      prContext.getResponse().addCookie(cookie);

    } catch (ClassCastException e) {
      LOG.error("Provider " + JitsiProvider.TYPE + " isn't an instance of " + JitsiProvider.class.getName(), e);
    }
  }

  /**
   * Gets the token cookie.
   *
   * @param req the req
   * @return the token cookie
   */
  private String getClientIdCookie(HttpServletRequest req) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (CLIENT_ID_COOKIE.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
}
