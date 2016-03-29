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
package org.exoplatform.jitsi.rest;

import org.exoplatform.jitsi.JitsiMeetForbiddenException;
import org.exoplatform.jitsi.JitsiMeetService;
import org.exoplatform.jitsi.JitsiMeetService.GroupMeet;
import org.exoplatform.jitsi.JitsiMeetService.UserMeet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

/**
 * 
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: MeetService.java 00000 Mar 11, 2016 pnedonosko $
 * 
 */
@Path("/jitsi/meet")
@Produces(MediaType.APPLICATION_JSON)
public class MeetService implements ResourceContainer {

  protected static final Log       LOG = ExoLogger.getLogger(MeetService.class);

  protected final JitsiMeetService jitsi;

  /**
   * 
   */
  public MeetService(JitsiMeetService jitsi) {
    this.jitsi = jitsi;
  }

  @POST
  @RolesAllowed("users")
  @Path("/user/{room}")
  public Response postUserMeet(@Context UriInfo uriInfo,
                               @PathParam("room") String room,
                               @FormParam("name") String name,
                               @FormParam("users") String users) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && users != null) {
        try {
          UserMeet meet = jitsi.createUsersMeet(room, name, users.split(","));
          meet.join(userName);
          URI requestUri = uriInfo.getAbsolutePath();
          meet = meet.contextMeet(requestUri);
          return Response.ok().entity(meet).build();
        } catch (JitsiMeetForbiddenException e) {
          LOG.warn("Forbidden user " + userName + " for meet " + room, e);
          return Response.status(Status.FORBIDDEN).entity(ErrorInfo.clientError("Forbidden")).build();
        } catch (Throwable e) {
          LOG.error("Error creating meet for user " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error creating meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  @DELETE
  @RolesAllowed("users")
  @Path("/user/{room}")
  public Response deleteUserMeet(@Context UriInfo uriInfo,
                                 @PathParam("room") String room,
                                 @QueryParam("users") String users) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && users != null) { // TODO room not used
        try {
          String[] meetUsers = users.split(",");
          UserMeet meet = jitsi.getUsersMeet(meetUsers);
          if (meet != null) {
            meet.leave(userName);
            meet = jitsi.getUsersMeet(meetUsers);
            if (meet != null) {
              try {
                URI requestUri = uriInfo.getAbsolutePath();
                meet = meet.contextMeet(requestUri);
              } catch (URISyntaxException e) {
                LOG.error("Error building meet URL " + meet, e);
              }
              return Response.ok().entity(meet).build();
            } else {
              return Response.status(Status.NO_CONTENT).build();
            }
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        } catch (JitsiMeetForbiddenException e) {
          LOG.warn("Forbidden user " + userName + " for meet " + room, e);
          return Response.status(Status.FORBIDDEN).entity(ErrorInfo.clientError("Forbidden")).build();
        } catch (Throwable e) {
          LOG.error("Error reading meet for user " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error reading meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  @GET
  @RolesAllowed("users")
  @Path("/user/{room}")
  public Response getUserMeet(@Context UriInfo uriInfo, @PathParam("room") String room, @QueryParam("users") String users) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && users != null) { // TODO room not used
        try {
          UserMeet meet = jitsi.getUsersMeet(users.split(","));
          if (meet != null) {
            try {
              URI requestUri = uriInfo.getAbsolutePath();
              meet = meet.contextMeet(requestUri);
            } catch (URISyntaxException e) {
              LOG.error("Error building meet URL " + meet, e);
            }
            return Response.ok().entity(meet).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        } catch (Throwable e) {
          LOG.error("Error reading meet for user " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error reading meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  @POST
  @RolesAllowed("users")
  @Path("/group/{room}")
  public Response postGroupMeet(@Context UriInfo uriInfo,
                                @PathParam("room") String room,
                                @FormParam("name") String name,
                                @FormParam("space") String space) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && space != null) {
        try {
          GroupMeet meet = jitsi.createGroupMeet(room, name, space);
          meet.join(userName);
          URI requestUri = uriInfo.getAbsolutePath();
          meet = meet.contextMeet(requestUri);
          return Response.ok().entity(meet).build();
        } catch (JitsiMeetForbiddenException e) {
          LOG.warn("Forbidden user " + userName + " for meet " + room, e);
          return Response.status(Status.FORBIDDEN).entity(ErrorInfo.clientError("Forbidden")).build();
        } catch (Throwable e) {
          LOG.error("Error creating meet for user " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error creating meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  @DELETE
  @RolesAllowed("users")
  @Path("/group/{room}")
  public Response deleteGroupMeet(@Context UriInfo uriInfo,
                                  @PathParam("room") String room,
                                  @QueryParam("space") String space) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && space != null) { // TODO room not used
        try {
          GroupMeet meet = jitsi.getGroupMeet(space);
          if (meet != null) {
            meet.leave(userName);
            meet = jitsi.getGroupMeet(space);
            if (meet != null) {
              try {
                URI requestUri = uriInfo.getAbsolutePath();
                meet = meet.contextMeet(requestUri);
              } catch (URISyntaxException e) {
                LOG.error("Error building meet URL " + meet, e);
              }
              return Response.ok().entity(meet).build();
            } else {
              return Response.status(Status.NO_CONTENT).build();
            }
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        } catch (JitsiMeetForbiddenException e) {
          LOG.warn("Forbidden user " + userName + " for meet " + room, e);
          return Response.status(Status.FORBIDDEN).entity(ErrorInfo.clientError("Forbidden")).build();
        } catch (Throwable e) {
          LOG.error("Error deleting (leaving) meet for space " + space + " and " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error reading meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }

  @GET
  @RolesAllowed("users")
  @Path("/group/{room}")
  public Response getGroupMeet(@Context UriInfo uriInfo, @PathParam("room") String room, @QueryParam("space") String space) {
    ConversationState convo = ConversationState.getCurrent();
    if (convo != null) {
      String userName = convo.getIdentity().getUserId();
      if (room != null && space != null) { // TODO room not used
        try {
          GroupMeet meet = jitsi.getGroupMeet(space);
          if (meet != null) {
            try {
              URI requestUri = uriInfo.getAbsolutePath();
              meet = meet.contextMeet(requestUri);
            } catch (URISyntaxException e) {
              LOG.error("Error building meet URL " + meet, e);
            }
            return Response.ok().entity(meet).build();
          } else {
            return Response.status(Status.NOT_FOUND).build();
          }
        } catch (Throwable e) {
          LOG.error("Error reading meet for space " + space + " and " + userName, e);
          return Response.serverError().entity(ErrorInfo.serverError("Error reading meet")).build();
        }
      } else {
        return Response.status(Status.BAD_REQUEST).entity(ErrorInfo.clientError("Wrong parameters")).build();
      }
    } else {
      return Response.status(Status.UNAUTHORIZED).build();
    }
  }
}
