/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.webconferencing.jitsi.portlet;

import static org.exoplatform.webconferencing.Utils.asJSON;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webconferencing.WebConferencingService;
import org.exoplatform.webconferencing.jitsi.JitsiProvider;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * This portlet loads Javascript module of this connector and register its provider(s) in the Web
 * Conferencing core. By doing this we add the Jitsi connector to call buttons on the page.
 */
public class JitsiPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log       LOG              = ExoLogger.getLogger(JitsiPortlet.class);

  /** The web conferencing. */
  private WebConferencingService webConferencing;

  /** The provider. */
  private JitsiProvider          provider;

  /** The Constant CLIENT_ID_COOKIE. */
  private static final String    CLIENT_ID_COOKIE = "jitsi-client-id";

  /**
   * {@inheritDoc}
   */
  @Override
  public void init() throws PortletException {
    super.init();
    // Get eXo container and Web Conferencing service once per portlet initialization
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.webConferencing = container.getComponentInstanceOfType(WebConferencingService.class);
    try {
      this.provider = (JitsiProvider) webConferencing.getProvider(JitsiProvider.TYPE);
    } catch (ClassCastException e) {
      LOG.error("Provider " + JitsiProvider.TYPE + " isn't an instance of " + JitsiProvider.class.getName(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doView(final RenderRequest request, final RenderResponse response) throws PortletException, IOException {
    if (this.provider != null) {
      try {
        PortalRequestContext prContext = Util.getPortalRequestContext();
        String clientId = getClientIdCookie(prContext.getRequest());
        if (clientId == null || clientId.isEmpty()) {
          // TODO: fix add cookie
          clientId = String.valueOf(System.currentTimeMillis());
          Cookie clientIdCookie = new Cookie(CLIENT_ID_COOKIE, clientId);
          clientIdCookie.setPath("/");
          clientIdCookie.setMaxAge(3600);
          prContext.getResponse().addCookie(clientIdCookie);
                 provider.addClient(clientId, request.getRemoteUser());
          LOG.info("SET CLIENT ID. User: " + request.getRemoteUser() + " Set clientId: " + clientId);
        }
        String token = provider.getAuthToken(clientId);
        String url = provider.getSettings().getUrl();
        // If we have settings to send to a client side
        String settingsJson = asJSON(provider.getSettings());
        JavascriptManager js = ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
        // first load Web Conferencing itself,
        js.require("SHARED/webConferencing", "webConferencing")
          // load our connector module to myProvider variable
          .require("SHARED/webConferencing_jitsi", "jitsi")
          // check if the variable contains an object to ensure the provider was loaded successfully
          .addScripts("if (jitsi) { " + "jitsi.initStorage('" + clientId + "', '" + token + "', '" + url + "');"
          // optionally configure the provider with settings (from the server-side)
              + "jitsi.configure(" + settingsJson + "); "
              // then add an instance of the provider to the Web Conferencing client
              + "webConferencing.addProvider(jitsi); "
              // and force Web Conferencing client update (to update call buttons and related stuff)
              + "webConferencing.update(); " + "}");
      } catch (Exception e) {
        LOG.error("Error processing Jitsi calls portlet for user " + request.getRemoteUser(), e);
      }
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
