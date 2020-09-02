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
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import org.exoplatform.container.web.AbstractHttpServlet;

/**
 * The Class JitsiCallGateway.
 */
public class JitsiUserGateway extends AbstractHttpServlet {

  /** The Constant serialVersionUID. */
  private static final long     serialVersionUID         = -6075521943684342671L;

  /** The Constant LOG. */
  protected static final Logger LOG                      = LoggerFactory.getLogger(JitsiUserGateway.class);

  /** The Constant CALL_URL. */
  private final static String   INVITE_URL               = "http://192.168.0.105:9080/jitsi/invite";

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String   AUTH_TOKEN_HEADER        = "X-Exoplatform-Auth";

  /** The Constant TRANSFER_ENCODING_HEADER. */
  private final static String   TRANSFER_ENCODING_HEADER = "Transfer-Encoding";

  /** 
   * Instantiates a new my call servlet.
   */
  public JitsiUserGateway() {
    //
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final AsyncContext ctx = req.startAsync();
    ctx.start(new Runnable() {
      public void run() {
        if (req.getRemoteUser() != null) {
          String userId = req.getRemoteUser();
          // TODO: check if port is not 80
          String url = req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + "/portal/rest/v1/social/"
              + userId;
          HttpGet request = new HttpGet(url);
          
          
          
          String path = "/rest/v1/social/users/" + userId;
          ServletContext servletContext = getServletContext().getContext("/portal");
          RequestDispatcher requestDispatcher = servletContext.getRequestDispatcher(path);
          try {
            requestDispatcher.forward(req, resp);
          } catch (ServletException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          
          
          /*
          try {
            resp.getWriter().write(req.getRemoteUser());
          } catch (IOException e) {
            log("Error occured", e);
          }*/
        } else {
          String inviteId = req.getParameter("inviteId");
          if (inviteId != null) {
            StringBuilder requestUrl = new StringBuilder(INVITE_URL).append("?inviteId=" + inviteId);
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
              log("Error occured while requesting call page", e);
            }
          } else {
            try {
              resp.getWriter().write("User is not authorized");
            } catch (IOException e) {
              log("Error occured:" + e);
            }
          }
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

}
