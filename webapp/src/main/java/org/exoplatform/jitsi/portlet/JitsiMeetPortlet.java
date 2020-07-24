package org.exoplatform.jitsi.portlet;

import java.io.IOException;

import javax.portlet.GenericPortlet;
import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderMode;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.w3c.dom.Element;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;

public class JitsiMeetPortlet extends GenericPortlet {

  /** The Constant LOG. */
  private static final Log LOG = ExoLogger.getLogger(JitsiMeetPortlet.class);

  /**
   * Do headers.
   *
   * @param request the request
   * @param response the response
   */
  @Override
  protected void doHeaders(RenderRequest request, RenderResponse response) {
    super.doHeaders(request, response);

   Element jitsiJS = response.createElement("script");
    jitsiJS.setAttribute("type", "text/javascript");
    jitsiJS.setAttribute("src", "https://jitsi-dev.francecentral.cloudapp.azure.com/external_api.js");
    response.addProperty(MimeResponse.MARKUP_HEAD_ELEMENT, jitsiJS);

  }

  /**
   * Renderer the portlet view.
   *
   * @param request the request
   * @param response the response
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws PortletException the portlet exception
   */
  @RenderMode(name = "view")
  public void view(RenderRequest request, RenderResponse response) throws IOException, PortletException {

    LOG.info("JItsi meet portlet called");
   JavascriptManager js = ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).getJavascriptManager();
   js.require("SHARED/jitsi", "jitsi").addScripts("jitsi.init();");
    PortletRequestDispatcher prDispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/pages/jitsi.jsp");
    prDispatcher.include(request, response);
  }
}
