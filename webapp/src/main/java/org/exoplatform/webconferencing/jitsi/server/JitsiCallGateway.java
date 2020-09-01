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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import com.drew.lang.StringUtil;

import org.exoplatform.container.web.AbstractHttpServlet;

/**
 * The Class JitsiCallGateway.
 */
public class JitsiCallGateway extends AbstractHttpServlet {

  /** The Constant serialVersionUID. */
  private static final long     serialVersionUID      = -6075521943684342671L;

  /** The Constant LOG. */
  protected static final Logger LOG                   = LoggerFactory.getLogger(JitsiCallGateway.class);

  /** The Constant CALL_URL. */
  private final static String   CALL_URL              = "http://192.168.0.105:9080/call";

  /** The Constant IDENTITY_HEADER. */
  private final static String   IDENTITY_HEADER       = "X-Exoplatform-Identity";

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String   AUTH_TOKEN_HEADER     = "X-Exoplatform-Auth";

  /** The Constant GUEST_USERNAME_HEADER. */
  private final static String   GUEST_USERNAME_HEADER = "X-Exoplatform-Guest-Username";

  private final static String   GUEST_INFO_ENDPOINT   = "http://192.168.0.105:9080/guest";

  /** 
   * Instantiates a new my call servlet.
   */
  public JitsiCallGateway() {
    //
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    final AsyncContext ctx = req.startAsync();
    String roomId = req.getPathInfo().substring(1);

    if (roomId.isEmpty()) {
      log("RoomId cannot be empty");
      resp.setStatus(500);
      return;
    }

    String guestName = null;
    if (req.getRemoteUser() == null) {
      String inviteId = req.getParameter("inviteId");
      if (inviteId != null) {
        // Ask microservice the guest name
        guestName = getGuestUsername(inviteId);
        resp.sendRedirect(req.getContextPath() + "/call/" + roomId);
      }
    }

    ctx.start(new Runnable() {
      public void run() {
       // HttpResponse response = getCallPage(username, isGuest);

        ctx.complete();
      }
    });
  }

  protected HttpResponse getCallPage(String username, boolean isGuest) throws IOException {

    HttpGet request = new HttpGet(CALL_URL);
    if (isGuest) {
      request.setHeader(GUEST_USERNAME_HEADER, username);
    } else {
      request.setHeader(IDENTITY_HEADER, username);
    }

    request.setHeader(AUTH_TOKEN_HEADER, "auth-token-123");
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request)) {
      return response;
    }
  }

  /**
   * Gets the guest username.
   *
   * @param inviteId the invite id
   * @return the guest username
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected String getGuestUsername(String inviteId) throws IOException {

    HttpGet request = new HttpGet(GUEST_INFO_ENDPOINT + "/" + inviteId);
    request.setHeader(AUTH_TOKEN_HEADER, "auth-token-123");
    try (CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = httpClient.execute(request)) {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        // TODO: parse JSON
        return EntityUtils.toString(entity);
      }
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

}
