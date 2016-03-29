package org.exoplatform.jitsi.portlet.videocallpopup;

import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.request.SecurityContext;
import juzu.template.Template;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.jitsi.JitsiMeetService;
import org.exoplatform.jitsi.portlet.videocall.JitsiMeetApplication;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class JitsiMeetPopupApplication {

  @Inject
  @Path("index.gtmpl")
  Template              index;

  OrganizationService   organizationService;

  SpaceService          spaceService;

  JitsiMeetService      meetService;

  ConversationRegistry  conversationRegistry;

  ContinuationService   continuationService;

  EXoContinuationBayeux exoContinuationBayeux;

  @Inject
  public JitsiMeetPopupApplication(OrganizationService organizationService,
                                   SpaceService spaceService,
                                   JitsiMeetService meetService,
                                   ConversationRegistry conversationRegistry,
                                   ContinuationService continuationService,
                                   EXoContinuationBayeux exoContinuationBayeux) {
    this.organizationService = organizationService;
    this.spaceService = spaceService;
    this.meetService = meetService;
    this.conversationRegistry = conversationRegistry;
    this.continuationService = continuationService;
    this.exoContinuationBayeux = exoContinuationBayeux;
  }

  @View
  public Response.Content index(SecurityContext securityContext) throws Exception {
    HttpServletRequest requestContext = Util.getPortalRequestContext().getRequest();
    String remoteUser = securityContext.getRemoteUser();

    // Process parameters
    String callMode = getParam("mode");
    String callee = getParam("callee");
    String calleeFullName = getFullName(callee);
    String caller = getParam("caller");
    String callerFullName = getFullName(caller);
    boolean hasChatMessage = Boolean.parseBoolean(getParam("hasChatMessage"));
    boolean isSpace = Boolean.parseBoolean(getParam("isSpace"));
    String spaceOrTeamName = getParam("spaceOrTeamName");

    return index.with()
                .set("user", remoteUser)
                .set("mode", callMode)
                .set("callee", callee)
                .set("calleeFullName", calleeFullName)
                .set("caller", caller)
                .set("callerFullName", callerFullName)
                .set("hasChatMessage", hasChatMessage)
                .set("isSpace", isSpace)
                .set("spaceOrTeamName", spaceOrTeamName)
                .set("videoCallVersion", JitsiMeetApplication.VERSION)
                .set("cometdUserToken", continuationService.getUserToken(remoteUser))
                .set("cometdContextName",
                     (exoContinuationBayeux == null ? "cometd" : exoContinuationBayeux.getCometdContextName()))
                .ok();
  }

  private String getParam(String paramName) {
    HttpServletRequest requestContext = Util.getPortalRequestContext().getRequest();
    String paramValue = requestContext.getParameter(paramName);
    if (paramValue == null) {
      paramValue = StringUtils.EMPTY;
    }
    return paramValue;
  }

  private String getFullName(String userId) {
    String fullName = StringUtils.EMPTY;
    try {
      User user = organizationService.getUserHandler().findUserByName(userId);
      if (user != null) {
        fullName = user.getFirstName().concat(" ").concat(user.getLastName());
      }
      return fullName;
    } catch (Exception e) {
      return fullName;
    }
  }
}
