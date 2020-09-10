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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webconferencing.WebConferencingService;
import org.exoplatform.webconferencing.jitsi.JitsiProvider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * The Class JitsiCallGateway.
 */
public class JitsiGateway extends AbstractHttpServlet {

  private enum Action {
    EXTERNAL_AUTH, INTERNAL_AUTH
  }

  /** The Constant serialVersionUID. */
  private static final long   serialVersionUID           = -6075521943684342671L;

  /** The Constant LOG. */
  protected static final Log  LOG                        = ExoLogger.getLogger(JitsiGateway.class);

  /** The Constant EXTERNAL_AUTH_TOKEN_HEADER. */
  private final static String EXTERNAL_AUTH_TOKEN_HEADER = "X-Exoplatform-External-Auth";

  /** The Constant INTERNAL_AUTH_TOKEN_HEADER. */
  private final static String INTERNAL_AUTH_TOKEN_HEADER = "X-Exoplatform-Internal-Auth";

  /** The Constant TRANSFER_ENCODING_HEADER. */
  private final static String TRANSFER_ENCODING_HEADER   = "Transfer-Encoding";

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException {
    WebConferencingService webconferencing =
                                           (WebConferencingService) getContainer().getComponentInstanceOfType(WebConferencingService.class);
    JitsiProvider jitsiProvider = (JitsiProvider) webconferencing.getProvider(JitsiProvider.TYPE);
    final AsyncContext ctx = httpRequest.startAsync();
    ctx.start(new Runnable() {
      public void run() {
        HttpServletRequest req = (HttpServletRequest) ctx.getRequest();
        HttpServletResponse resp = (HttpServletResponse) ctx.getResponse();
        String uri = req.getRequestURI() + (req.getQueryString() != null ? "?" + req.getQueryString() : "");
        uri = uri.substring(uri.indexOf("/jitsi/") + 6);
        if (req.getRequestURI().startsWith("/jitsi/portal/")) {
          String requestUrl = new StringBuilder(getPlatformUrl(req)).append(uri).toString();
          forward(requestUrl, Action.INTERNAL_AUTH, jitsiProvider.getInternalAuthSecret(), req, resp);
        } else {
          String requestUrl = new StringBuilder(jitsiProvider.getSettings().getUrl()).append(uri).toString();
          forward(requestUrl, Action.EXTERNAL_AUTH, jitsiProvider.getExternalAuthSecret(), req, resp);
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

  private void forward(String requestUrl, Action action, String secret, HttpServletRequest req, HttpServletResponse resp) {
    HttpGet request = new HttpGet(requestUrl);
    String authHeader;
    // TODO: Use servlet forwarding for accessing internal resources. Solve the issue with filters (not invoked when forwarding)
    if (action == Action.INTERNAL_AUTH) {
      // Pass cookies for internal auth
      if (req.getCookies() != null) {
        request.setHeader("Cookie", getCookiesAsString(req));
      }
      authHeader = INTERNAL_AUTH_TOKEN_HEADER;
    } else {
      authHeader = EXTERNAL_AUTH_TOKEN_HEADER;
    }
    String token = Jwts.builder()
                       .setSubject("exo-webconf")
                       .claim("action", action.toString().toLowerCase())
                       .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                       .compact();

    request.setHeader(authHeader, token);

    SSLContextBuilder builder = new SSLContextBuilder();
    try {
      builder.loadTrustMaterial(null, new TrustStrategy() {
        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
          return true;
        }
      });
    } catch (NoSuchAlgorithmException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    } catch (KeyStoreException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }

    SSLConnectionSocketFactory sslSF = null;
    try {
      sslSF = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    } catch (KeyManagementException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    } catch (NoSuchAlgorithmException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }

    try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();
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
      LOG.warn("Error occured while requesting remote resource", e.getMessage());
      try {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"error\":\"Cannot connect to the remote resource\"}");
      } catch (IOException e1) {
        LOG.error("Cannot write response", e.getMessage());
      }
    }
  }

  /**
   * Gets the cookies as string.
   *
   * @param req the req
   * @return the cookies as string
   */
  private String getCookiesAsString(HttpServletRequest req) {
    StringBuilder builder = new StringBuilder();
    for (Cookie cookie : req.getCookies()) {
      if (builder.length() != 0) {
        builder.append(" ");
      }
      builder.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
    }
    return builder.toString();
  }

  /**
   * Gets the platform url.
   *
   * @param req the req
   * @return the platform url
   */
  private String getPlatformUrl(HttpServletRequest req) {
    return new StringBuilder(req.getScheme()).append("://")
                                             .append(req.getServerName())
                                             .append(("http".equals(req.getScheme()) && req.getServerPort() == 80
                                                 || "https".equals(req.getScheme()) && req.getServerPort() == 443
                                                                                                                  ? ""
                                                                                                                  : ":"
                                                                                                                      + req.getServerPort()))
                                             .toString();
  }

}
