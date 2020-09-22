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
package org.exoplatform.webconferencing.jitsi;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;
import org.exoplatform.webconferencing.CallProvider;
import org.exoplatform.webconferencing.UserInfo.IMInfo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Jitsi provider implementation.
 * 
 * Created by The eXo Platform SAS.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: MyConnectorProvider.java 00000 Mar 30, 2017 pnedonosko $
 */
public class JitsiProvider extends CallProvider {

  /** The Constant LOG. */
  protected static final Log LOG                         = ExoLogger.getLogger(JitsiProvider.class);

  /** The Constant TYPE. */
  public static final String TYPE                        = "jitsi";

  /** The Constant CONFIG_CLIENT_SECRET. */
  public static final String CONFIG_CLIENT_SECRET        = "client-secret";

  /** The Constant CONFIG_EXTERNAL_AUTH_SECRET. */
  public static final String CONFIG_INTERNAL_AUTH_SECRET = "internal-auth-secret";

  /** The Constant CONFIG_EXTERNAL_AUTH_SECRET. */
  public static final String CONFIG_EXTERNAL_AUTH_SECRET = "external-auth-secret";

  /** The Constant CONFIG_APP_ID. */
  public static final String CONFIG_APP_ID               = "app-id";

  /** The Constant CONFIG_SERVICE_URL. */
  public static final String CONFIG_SERVICE_URL          = "service-url";

  /** The Constant TITLE. */
  public static final String TITLE                       = "Jitsi";

  /** The Constant VERSION. */
  public static final String VERSION                     = "1.0.0";

  /**
   * Settings for Jitsi provider.
   */
  public class JitsiSettings extends Settings {

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
      return JitsiProvider.this.getUrl();
    }

  }

  /**
   * IM info for user profile.
   */
  public class JitsiIMInfo extends IMInfo {

    /**
     * Instantiates a new Jitsi IM info.
     *
     * @param id the id
     */
    protected JitsiIMInfo(String id) {
      super(TYPE, id);
    }

    // You may add other specific methods here. Getters will be serialized to JSON and available on client
    // side (in Javascript provider module).
  }

  /** The internal auth secret. */
  protected final String internalAuthSecret;

  /** The external auth secret. */
  protected final String externalAuthSecret;

  /** The connector web-services URL (will be used to generate Call page URLs). */
  protected final String url;

  /** The app id. */
  protected final String appId;

  /**
   * Instantiates a new JitsiProvider provider.
   *
   * @param profileSettings the profile settings
   * @param params the params (from configuration.xml)
   * @throws ConfigurationException the configuration exception
   */
  public JitsiProvider(UserProfileSettingsService profileSettings, InitParams params) throws ConfigurationException {
    super(params);

    String internalAuthSecret = this.config.get(CONFIG_INTERNAL_AUTH_SECRET);
    if (internalAuthSecret == null || (internalAuthSecret = internalAuthSecret.trim()).length() == 0) {
      throw new ConfigurationException(CONFIG_INTERNAL_AUTH_SECRET + " required and should be non empty.");
    }
    this.internalAuthSecret = internalAuthSecret;

    String externalAuthSecret = this.config.get(CONFIG_EXTERNAL_AUTH_SECRET);
    if (externalAuthSecret == null || (externalAuthSecret = externalAuthSecret.trim()).length() == 0) {
      throw new ConfigurationException(CONFIG_EXTERNAL_AUTH_SECRET + " required and should be non empty.");
    }
    this.externalAuthSecret = externalAuthSecret;

    String appId = this.config.get(CONFIG_APP_ID);
    if (appId == null || (appId = appId.trim()).length() == 0) {
      throw new ConfigurationException(CONFIG_APP_ID + " required and should be non empty.");
    }
    this.appId = appId;

    String serviceUrl = this.config.get(CONFIG_SERVICE_URL);
    if (serviceUrl == null || (serviceUrl = serviceUrl.trim()).length() == 0) {
      throw new ConfigurationException(CONFIG_SERVICE_URL + " required and should be non empty.");
    }
    this.url = serviceUrl;

    if (profileSettings != null) {
      // add plugin programmatically as it's an integral part of the provider
      profileSettings.addIMType(new IMType(TYPE, TITLE));
    }
  }

  /**
   * Instantiates a new JitsiProvider provider. This constructor can be used in environments when no
   * {@link UserProfileSettingsService} found (e.g. in test environments).
   *
   * @param params the params (from configuration.xml)
   * @throws ConfigurationException the configuration exception
   */
  public JitsiProvider(InitParams params) throws ConfigurationException {
    this(null, params);
  }

  /**
   * Creates the token.
   *
   * @param username the username
   * @return the string
   */
  public String createToken(String username) {
    // TODO: replace maps by objects
    Map<String, Object> context = new HashMap<>();
    Map<String, String> user = new HashMap<>();
    user.put("name", username);
    context.put("user", user);
    String token = Jwts.builder()
                       .setHeaderParam("typ", "JWT")
                       .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)))
                       .setSubject("*")
                       .setIssuer(appId)
                       .setAudience(appId)
                       .claim("context", context)
                       .claim("room", "*")
                       .signWith(Keys.hmacShaKeyFor(externalAuthSecret.getBytes()))
                       .compact();
    return token;
  }

  /**
   * Gets the internal auth secret.
   *
   * @return the internal auth secret
   */
  public String getInternalAuthSecret() {
    return this.internalAuthSecret;
  }

  /**
   * Gets the external auth secret.
   *
   * @return the external auth secret
   */
  public String getExternalAuthSecret() {
    return this.externalAuthSecret;
  }

  /**
   * Gets the settings.
   *
   * @return the settings
   */
  public JitsiSettings getSettings() {
    return new JitsiSettings();
  }

  /**
   * Gets the url.
   *
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public IMInfo getIMInfo(String imId) {
    // TODO here you can validate, extend or do any other IM id preparations
    return new JitsiIMInfo(imId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getType() {
    return TYPE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getSupportedTypes() {
    return new String[] { getType() };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getTitle() {
    return TITLE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getDescription(Locale locale) {
    // TODO Implement i18n description of the provider
    return super.getDescription(locale);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getVersion() {
    return VERSION;
  }

}
