
/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.jitsi;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.picocontainer.Startable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: JitsiMeetService.java 00000 Mar 10, 2016 pnedonosko $
 * 
 */
public class JitsiMeetService implements Startable {

  public static final String PROPERTY_JITSI_MEET_DOMAIN = "jitsi.meet.domain";

  protected static final Log LOG                        = ExoLogger.getLogger(JitsiMeetService.class);

  protected class MeetKey {
    final int         hashCode;

    final Set<String> users;

    protected MeetKey(Set<String> users) {
      super();
      this.users = users;
      int hcode = 31;
      for (String un : users) {
        // sum hashes without multiplication!
        hcode = hcode + un.hashCode();
      }
      this.hashCode = hcode * 31;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof MeetKey) {
        MeetKey other = (MeetKey) obj;
        if (users.size() == other.users.size()) {
          for (String u : users) {
            if (!other.users.contains(u)) {
              return false;
            }
          }
          return true;
        }
      }
      return super.equals(obj);
    }

  }

  public abstract class MeetInfo {
    final String room;

    final String domain;

    final String name;

    protected MeetInfo(String domain, String room, String name) {
      super();
      this.domain = domain;
      this.room = room;
      this.name = name;
    }

    /**
     * @param userName
     * @throws JitsiMeetForbiddenException
     * @return
     */
    public boolean join(String userName) throws JitsiMeetForbiddenException {
      return joinMeet(this, userName);
    }

    /**
     * @param userName
     * @return
     * @throws JitsiMeetForbiddenException
     */
    public boolean leave(String userName) throws JitsiMeetForbiddenException {
      return leaveMeet(this, userName);
    }

    /**
     * Tells does given user can join (and later leave) the meet.
     * 
     * @return {@link Boolean} <code>true</code> if user can join/leave the meeting, <code>false</code>
     *         otherwise
     */
    public abstract boolean canJoin(String userName);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return new StringBuilder(room).append('@').append(domain).toString();
    }

    /**
     * @return the room
     */
    public String getRoom() {
      return room;
    }

    /**
     * @return the domain
     */
    public String getDomain() {
      return domain;
    }

    /**
     * @return the name
     */
    public String getName() {
      return name;
    }
  }

  public abstract class ContextMeet<M extends MeetInfo> extends MeetInfo {

    final String url;

    protected ContextMeet(String domain, String room, String name, String url) {
      super(domain, room, name);
      this.url = url;
    }

    protected abstract M buildContext(String url);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return new StringBuilder(super.toString()).append('[').append(url).append(']').toString();
    }

    /**
     * @return the url
     */
    public String getUrl() {
      return url;
    }

    /**
     * @param serviceUrl the url to init with Meet parameters (domain and room)
     * @throws URISyntaxException
     */
    public M contextMeet(URI serviceUrl) throws URISyntaxException {
      // URI: String scheme, String userInfo, String host, int port, String path, String query, String
      // fragment
      URI meetUri = new URI(serviceUrl.getScheme(),
                            null,
                            serviceUrl.getHost(),
                            serviceUrl.getPort(),
                            new StringBuilder("/jitsi-meet/call/").append(domain).append('/').append(room).toString(),
                            null,
                            null);
      return buildContext(meetUri.toASCIIString());
    }

  }

  public class UserMeet extends ContextMeet<UserMeet> {

    final Set<String> users = new LinkedHashSet<String>();

    protected UserMeet(String domain, String room, String name, String... users) {
      super(domain, room, name, null);
      for (String un : users) {
        this.users.add(un);
      }
    }

    private UserMeet(String domain, String room, String name, String url, Set<String> users) {
      super(domain, room, name, url);
      this.users.addAll(users);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public boolean join(String userName) throws JitsiMeetForbiddenException {
      // Only member of users set can join
      if (users.contains(userName)) {
        return super.join(userName);
      }
      throw new JitsiMeetForbiddenException("Not in meet users");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leave(String userName) throws JitsiMeetForbiddenException {
      // Only member of users set can leave
      if (users.contains(userName)) {
        return super.leave(userName);
      }
      throw new JitsiMeetForbiddenException("Not in meet users");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canJoin(String userName) {
      return users.contains(userName);
    }

    /**
     * @return the users
     */
    public Set<String> getUsers() {
      return users;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UserMeet buildContext(String url) {
      return new UserMeet(domain, room, name, url, users);
    }
  }

  public class GroupMeet extends ContextMeet<GroupMeet> {

    final String spacePrettyName;

    protected GroupMeet(String domain, String room, String name, String spacePrettyName) {
      super(domain, room, name, null);
      this.spacePrettyName = spacePrettyName;
    }

    private GroupMeet(String domain, String room, String name, String url, String space) {
      super(domain, room, name, url);
      this.spacePrettyName = space;
    }

    /**
     * @return the space pretty name
     */
    public String getSpaceName() {
      return spacePrettyName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GroupMeet buildContext(String url) {
      return new GroupMeet(domain, room, name, url, spacePrettyName);
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public boolean join(String userName) throws JitsiMeetForbiddenException {
      // Only member of the space set can join
      if (isSpaceMember(userName, spacePrettyName)) {
        return super.join(userName);
      }
      throw new JitsiMeetForbiddenException("Not space member");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean leave(String userName) throws JitsiMeetForbiddenException {
      // Only member of the space set can leave
      if (isSpaceMember(userName, spacePrettyName)) {
        return super.leave(userName);
      }
      throw new JitsiMeetForbiddenException("Not space member");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canJoin(String userName) {
      return isSpaceMember(userName, spacePrettyName);
    }
  }

  protected final ConcurrentHashMap<String, GroupMeet>     groupMeets = new ConcurrentHashMap<String, GroupMeet>();

  protected final ConcurrentHashMap<MeetKey, UserMeet>     userMeets  = new ConcurrentHashMap<MeetKey, UserMeet>();

  protected final ConcurrentHashMap<String, Queue<String>> rooms      = new ConcurrentHashMap<String, Queue<String>>();

  protected final String                                   domain;

  protected SpaceService                                   spaceService;

  /**
   * 
   */
  public JitsiMeetService() {
    // TODO use component config (init params etc)
    String jitsiDomain = PropertyManager.getProperty(PROPERTY_JITSI_MEET_DOMAIN);
    if (jitsiDomain != null && jitsiDomain.length() > 0) {
      this.domain = jitsiDomain;
      LOG.info("Default " + PROPERTY_JITSI_MEET_DOMAIN + ": " + this.domain);
    } else {
      // TODO should remove this default?
      this.domain = "meet.jit.si";
      LOG.warn("Configuration preperty " + PROPERTY_JITSI_MEET_DOMAIN + " not provided, will use default one: "
          + this.domain);
    }
  }

  public String getDomain() {
    return domain;
  }

  public UserMeet createUsersMeet(String room, String name, String... users) {
    Set<String> userSet = new LinkedHashSet<String>();
    for (String un : users) {
      userSet.add(un);
    }
    // List<String> userList = new ArrayList<String>();
    // Collections.sort(userList);

    MeetKey key = new MeetKey(userSet);
    UserMeet meet = userMeets.get(key);
    if (meet == null) {
      meet = new UserMeet(domain, room, name, users);
      userMeets.put(key, meet);
    }
    return meet;
  }

  public UserMeet getUsersMeet(String... users) {
    Set<String> userSet = new LinkedHashSet<String>();
    for (String un : users) {
      userSet.add(un);
    }
    MeetKey key = new MeetKey(userSet);
    return userMeets.get(key);
  }

  public GroupMeet createGroupMeet(String room, String name, String spacePrettyName) {
    GroupMeet meet = groupMeets.get(spacePrettyName);
    if (meet == null) {
      meet = new GroupMeet(domain, room, name, spacePrettyName);
      groupMeets.put(spacePrettyName, meet);
    }
    return meet;
  }

  public GroupMeet getGroupMeet(String spacePrettyName) {
    return groupMeets.get(spacePrettyName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {
    // XXX SpaceService done in crappy way and we need reference it after the container start only, otherwise
    // it will fail the whole server start due to not found JCR service
    this.spaceService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // nothing
  }

  protected boolean joinMeet(MeetInfo meet, String userName) {
    Queue<String> active = rooms.get(meet.getRoom());
    if (active == null) {
      Queue<String> newActive = new ConcurrentLinkedQueue<String>();
      Queue<String> existing = rooms.putIfAbsent(meet.getRoom(), newActive);
      active = existing != null ? existing : newActive;
    }
    if (!active.contains(userName)) {
      active.add(userName);
      return true;
    }
    return false;
  }

  protected boolean leaveMeet(MeetInfo meet, String userName) {
    Queue<String> active = rooms.get(meet.getRoom());
    if (active != null) {
      boolean res = active.remove(userName);
      if (active.size() == 0) {
        if (rooms.remove(meet.getRoom(), active)) {
          // clean meets map
          if (meet instanceof UserMeet) {
            deleteUsersMeet(((UserMeet) meet).getUsers());
          } else if (meet instanceof GroupMeet) {
            deleteGroupMeet(((GroupMeet) meet).getSpaceName());
          }
        }
      }
      return res;
    }
    return false;
  }

  protected UserMeet deleteUsersMeet(Set<String> userSet) {
    MeetKey key = new MeetKey(userSet);
    UserMeet meet = userMeets.remove(key);
    return meet;
  }

  protected GroupMeet deleteGroupMeet(String space) {
    GroupMeet meet = groupMeets.remove(space);
    return meet;
  }

  protected boolean isSpaceMember(String userName, String spacePrettyName) {
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    Set<String> spaceMembers = new HashSet<String>();
    for (String sm : space.getMembers()) {
      spaceMembers.add(sm);
    }
    return spaceMembers.contains(userName);
  }
}
