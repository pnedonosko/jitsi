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
package org.exoplatform.webconferencing.jitsi.server;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * The Class JitsiCallGateway.
 */
public class JitsiGateway extends AbstractHttpServlet {

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID         = -6075521943684342671L;

  /** The Constant LOG. */
  protected static final Log  LOG                      = ExoLogger.getLogger(JitsiGateway.class);

  /** The Constant CALL_URL. */
  private final static String JITSI_APP_URL            = "http://192.168.1.103:9080";

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String AUTH_TOKEN_HEADER        = "X-Exoplatform-Auth";

  /** The Constant TRANSFER_ENCODING_HEADER. */
  private final static String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final AsyncContext ctx = req.startAsync();
    ctx.start(new Runnable() {
      public void run() {
        if (req.getRequestURI().startsWith("/jitsi/portal/")) {
          forwardInternally(req, resp);
        } else {
          forwardToCallApp(req, resp);
        }
        ctx.complete();
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

  private void forwardToCallApp(HttpServletRequest req, HttpServletResponse resp) {
    String uri = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
    uri = uri.substring(uri.indexOf("/jitsi/") + 6);
    StringBuilder requestUrl = new StringBuilder(JITSI_APP_URL).append(uri);
    HttpGet request = new HttpGet(requestUrl.toString());
    request.setHeader(AUTH_TOKEN_HEADER, "mock-auth-token");
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request)) {
      for (Header header : response.getAllHeaders()) {
        if (!header.getName().equals(TRANSFER_ENCODING_HEADER)) {
          resp.setHeader(header.getName(), header.getValue());
        }
      }
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        resp.getWriter().write(EntityUtils.toString(entity));
      }
    } catch (IOException e) {
      log("Error occured while requesting remote resource", e);
    }
  }

  private void forwardInternally(HttpServletRequest req, HttpServletResponse resp) {
    String uri = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
    uri = uri.substring(uri.indexOf("/jitsi/portal/") + 13);
    ServletContext servletContext = getServletContext().getContext("/portal");
    RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(uri);
    try {
      requestDispatcher.forward(req, resp);
    } catch (Exception e) {
      log("Cannot forward request to /portal" + uri, e);
    }
  }
}
