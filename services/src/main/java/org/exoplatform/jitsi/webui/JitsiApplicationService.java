/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.jitsi.webui;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Jitsi WebUI applications coordinator.<br>
 * 
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: JitsiApplicationService.java 00000 Mar 10, 2016 pnedonosko $
 * 
 */
@Deprecated // TODO not used
public class JitsiApplicationService {

  public static final String  INIT_SCRIPT = "JitsiInitScript".intern();

  protected static final Log  LOG         = ExoLogger.getLogger(JitsiApplicationService.class);

  protected final Set<String> targets     = new LinkedHashSet<String>();

  /**
   * 
   */
  public JitsiApplicationService() {
    // predefined Web app targets:
    targets.add("ChatPortlet");
    targets.add("UISpaceActivityStreamPortlet");
    targets.add("UIUserActivityStreamPortlet");
    targets.add("UIProfilePortlet");
    targets.add("UIAllPeoplePortlet");
    targets.add("UIMembersPortlet");
    targets.add("UIConnectionsPortlet");
  }

  public void activate(UIApplication app) {
    if (true || isMatch(app)) {
      try {
        WebuiRequestContext requestContext = (WebuiRequestContext) WebuiRequestContext.getCurrentInstance();
        JavascriptManager jsManager = requestContext.getJavascriptManager();
        RequireJS requireJS = jsManager.getRequireJS();

        Object obj = requestContext.getAttribute(INIT_SCRIPT);
        if (obj == null) {
          String userName = requestContext.getRemoteUser();
          if (LOG.isDebugEnabled()) {
            LOG.debug(">> Activating app for Jitsi Meet: " + app + ", user " + userName);
          }

          OrganizationService orgService = requestContext.getApplication()
                                                         .getApplicationServiceContainer()
                                                         .getComponentInstanceOfType(OrganizationService.class);
          User exoUser = orgService.getUserHandler().findUserByName(userName);
          String userTitle;
          if (exoUser != null) {
            userTitle = exoUser.getFirstName() + " " + exoUser.getLastName();
          } else {
            userTitle = userName;
          }

          requireJS.require("SHARED/jitsiMeet", "jitsiMeet");
          requireJS.addScripts("jitsiMeet.initUser(\"" + userName + "\", \"" + userTitle + "\");");
          requireJS.addScripts("jitsiMeet.initButtons('" + app.getId() + "');");
          requestContext.setAttribute(INIT_SCRIPT, userName);
        } else {
          if (LOG.isDebugEnabled()) {
            LOG.debug("<< Application already activated for Jitsi Meet: " + app + ", user " + obj);
          }
        }
      } catch (Exception e) {
        LOG.error("Error activating app for Meet Button: " + e.getMessage(), e);
      }
    }
  }

  public void deactivate(UIApplication app) {
    if (isMatch(app)) {
      // TODO

    }
  }

  protected boolean isMatch(UIApplication app) {
    String appId = app.getId();
    return targets.contains(appId);
  }

}
