package org.exoplatform.jitsi.portlet.videocall;

import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.request.ApplicationContext;
import juzu.request.SecurityContext;
import juzu.request.UserContext;
import juzu.template.Template;
import juzu.template.Template.Builder;

import org.exoplatform.jitsi.JitsiMeetService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class JitsiMeetApplication {

  private static final Log   LOG     = ExoLogger.getLogger(JitsiMeetApplication.class);

  public static final String VERSION = "2.0.0-Beta1";                                  // TODO

  @Inject
  @Path("index.gtmpl")
  Template                   index;

  @Inject
  BundleService              bundleService;

  OrganizationService        organizationService;

  SpaceService               spaceService;

  JitsiMeetService           meetService;

  ConversationRegistry       conversationRegistry;

  ContinuationService        continuationService;

  EXoContinuationBayeux      exoContinuationBayeux;

  @Inject
  public JitsiMeetApplication(OrganizationService organizationService,
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
  public Response.Content index(ApplicationContext applicationContext,
                                SecurityContext securityContext,
                                UserContext userContext) throws Exception {
    PortalRequestContext requestContext = Util.getPortalRequestContext();

    HttpServletRequest request = requestContext.getRequest();
    HttpSession httpSession = request.getSession();

    String remoteUser = securityContext.getRemoteUser();

    String userFullName;
    User exoUser = organizationService.getUserHandler().findUserByName(remoteUser);
    if (exoUser != null) {
      userFullName = exoUser.getFirstName() + " " + exoUser.getLastName();
    } else {
      userFullName = remoteUser;
    }

    // Get bundle messages
    Locale locale = userContext.getLocale();
    ResourceBundle bundle = applicationContext.resolveBundle(locale);
    String messages = bundleService.getBundle("jitsiBundleData", bundle, locale);

    // Space
    String spacePrettyName, spaceRoomName;
    Space currSpace = getSpaceByContext();
    if (currSpace != null) {
      spacePrettyName = currSpace.getPrettyName();
      spaceRoomName = spaceRoomName(currSpace);
    } else {
      spacePrettyName = spaceRoomName = "".intern();
    }

    Builder builder = index.with()
                           .set("user", remoteUser)
                           .set("userFullName", userFullName)
                           .set("spaceName", spacePrettyName)
                           .set("spaceRoomName", spaceRoomName)
                           .set("videoCallVersion", VERSION)
                           .set("messages", messages)
                           .set("cometdUserToken", continuationService.getUserToken(remoteUser))
                           .set("cometdContextName",
                                (exoContinuationBayeux == null ? "cometd" : exoContinuationBayeux.getCometdContextName()));

    return builder.ok();
  }

  protected String spaceRoomName(Space space) {
    StringBuilder sname = new StringBuilder();
    sname.append("eXoMeet");
    String spaceName = space.getShortName();
    for (String s : spaceName.split("_")) {
      if (s.length() > 0) {
        sname.append(Character.toUpperCase(s.charAt(0)));
        if (s.length() > 1) {
          sname.append(s.substring(1));
        }
      }
    }
    sname.append("Space");
    return sname.toString();
  }

  private Space getSpaceByContext() {
    //
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestPath = pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    if (route == null)
      return null;

    //
    String spacePrettyName = route.localArgs.get("spacePrettyName");
    return spaceService.getSpaceByPrettyName(spacePrettyName);
  }
}
