
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.picocontainer.Startable;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: JitsiMeetService.java 00000 Mar 10, 2016 pnedonosko $
 */
public class JitsiMeetService implements Startable {

  /** The Constant PROPERTY_JITSI_MEET_DOMAIN. */
  public static final String                      PROPERTY_JITSI_MEET_DOMAIN = "jitsi.meet.domain";

  /** The Constant LOG. */
  protected static final Log                      LOG                        = ExoLogger.getLogger(JitsiMeetService.class);

  /** The meets. */
  protected final ConcurrentHashMap<String, Meet> meets                      = new ConcurrentHashMap<String, Meet>();

  /** The domain. */
  protected final String                          domain;

  /** The space service. */
  protected SpaceService                          spaceService;

  /**
   * The Class Meet.
   */
  public class Meet {

    /** The room. */
    private final String      room;

    /** The domain. */
    private final String      domain;

    /** The users. */
    private final Set<String> users = new HashSet<>();

    /**
     * Instantiates a new meet.
     *
     * @param room the room
     * @param domain the domain
     */
    public Meet(String room, String domain) {
      this.room = room;
      this.domain = domain;
    }

    /**
     * Gets the room.
     *
     * @return the room
     */
    public String getRoom() {
      return room;
    }

    /**
     * Gets the domain.
     *
     * @return the domain
     */
    public String getDomain() {
      return domain;
    }

    /**
     * Gets the users.
     *
     * @return the users
     */
    public Set<String> getUsers() {
      return users;
    }

  }

  /**
   * Instantiates a new jitsi meet service.
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
      LOG.warn("Configuration preperty " + PROPERTY_JITSI_MEET_DOMAIN + " not provided, will use default one: " + this.domain);
    }
  }

  /**
   * Creates the meet.
   *
   * @param room the room
   * @return the meet
   */
  public Meet createMeet(String room) {
    if (meets.containsKey(room)) {
      return meets.get(room);
    } else {
      Meet meet = new Meet(room, domain);
      meets.put(room, meet);
      return meet;
    }
  }


  /**
   * Join meet.
   *
   * @param room the room
   * @param userId the user id
   * @return true, if successful
   */
  public boolean joinMeet(String room, String userId) {
    if (meets.containsKey(room)) {
      Meet meet = meets.get(room);
      meet.getUsers().add(userId);
      return true;
    }
    return false;
  }
  
  /**
   * Join meet.
   *
   * @param room the room
   * @param userId the user id
   * @return true, if successful
   */
  public boolean leaveMeet(String room, String userId) {
    if (meets.containsKey(room)) {
      Meet meet = meets.get(room);
      Set<String> users = meet.getUsers();
      if(users.contains(userId)) {}
      return true;
    }
    return false;
  }
  

  /**
   * Gets the domain.
   *
   * @return the domain
   */
  public String getDomain() {
    return domain;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    // nothing
  }

}
