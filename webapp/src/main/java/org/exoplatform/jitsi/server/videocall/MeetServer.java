/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.jitsi.server.videocall;

import juzu.Path;
import juzu.Response;
import juzu.Route;
import juzu.View;
import juzu.impl.common.Tools;
import juzu.request.HttpContext;
import juzu.template.Template;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MeetServer {
  private static final Logger LOG = Logger.getLogger(MeetServer.class.getSimpleName());

  @Inject
  @Path("index.gtmpl")
  Template                    index;

  @Inject
  @Path("meet.gtmpl")
  Template                    meet;

  public MeetServer() {

  }

  @View
  @Route("/")
  public Response.Content index() throws IOException {
    return index.ok();
  }

  @View
  @Route("/{domain}/{room}")
  public Response meet(HttpContext context, String domain, String room) {
    if (domain == null || domain.length() == 0) {
      domain = null;
    }

    if (room == null || room.length() == 0) {
      room = null;
    }

    return meet.with()
               .set("scheme", context.getScheme())
               .set("room", room)
               .set("domain", domain)
               .ok()
               .withCharset(Tools.UTF_8);
  }

}
