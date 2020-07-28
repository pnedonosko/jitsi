package org.exoplatform.jitsi.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.jitsi.JitsiMeetService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

public class JitsiMeetServlet extends HttpServlet {
  /**
   * 
   */
  private static final long serialVersionUID = -2934665039315930129L;

  /** The Constant LOG. */
  private static final Log  LOG              = ExoLogger.getLogger(JitsiMeetServlet.class);

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    if (request.getRemoteUser() != null) {
      JitsiMeetService meetService = PortalContainer.getInstance().getComponentInstanceOfType(JitsiMeetService.class);
      IdentityManager identityManager = PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, request.getRemoteUser());
      String name = identity.getProfile().getFullName();
      String email = identity.getProfile().getEmail();
      String room = request.getParameter("room");
      String domain = meetService.getDomain();

      if (LOG.isDebugEnabled()) {
        LOG.debug("Join meeting: user: {}, room {}, domain: {}", name, room, domain);
      }

      request.setAttribute("room", room);
      request.setAttribute("domain", domain);
      request.setAttribute("name", name);
      request.setAttribute("email", email);
      getServletContext().getRequestDispatcher("/WEB-INF/pages/jitsi.jsp").include(request, response);
    } else {
      response.setStatus(403);
    }

  }

}
