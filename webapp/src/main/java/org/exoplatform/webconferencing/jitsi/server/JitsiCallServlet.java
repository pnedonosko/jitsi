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

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import org.exoplatform.container.web.AbstractHttpServlet;

/**
 * The Class JitsiCallServlet.
 */
public class JitsiCallServlet extends AbstractHttpServlet {

  /** The Constant serialVersionUID. */
  private static final long     serialVersionUID  = -6075521943684342671L;

  /** The Constant LOG. */
  protected static final Logger LOG               = LoggerFactory.getLogger(JitsiCallServlet.class);

  
  /** The Constant CALL_PAGE. */
  private final static String   CALL_PAGE         = "/WEB-INF/pages/call.jsp";

  /**
   * Instantiates a new my call servlet.
   */
  public JitsiCallServlet() {
    //
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Cookie clientIdCookie = new Cookie("client-id", "142");
    clientIdCookie.setPath("/");
    clientIdCookie.setMaxAge(3600);
    resp.addCookie(clientIdCookie);
    req.getRequestDispatcher(CALL_PAGE).include(req, resp);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    doGet(req, resp);
  }

}
