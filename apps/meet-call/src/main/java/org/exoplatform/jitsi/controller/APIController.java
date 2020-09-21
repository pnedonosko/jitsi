package org.exoplatform.jitsi.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Class APIController.
 */
@RestController
@RequestMapping("api")
public class APIController {

  private final static String AUTH_TOKEN_HEADER = "X-Exoplatform-External-Auth";

  /**
   * Userinfo.
   *
   * @param inviteId the invite id
   * @return the map
   */
  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/userinfo/{inviteId}")
  public UserInfoResponse userinfo(HttpServletRequest request, @PathVariable("inviteId") String inviteId) {
    UserInfo userInfo = new UserInfo("guest-" + inviteId, "James", "Guest");
    String token = request.getHeader(AUTH_TOKEN_HEADER);
    return new UserInfoResponse(userInfo, token);
  }

  /**
   * The Class UserInfoResponse.
   */
  public class UserInfoResponse {

    /** The user info. */
    private final UserInfo userInfo;

    /** The auth token. */
    private final String   authToken;

    /**
     * Instantiates a new user info response.
     *
     * @param userInfo the user info
     * @param authToken the auth token
     */
    public UserInfoResponse(UserInfo userInfo, String authToken) {
      super();
      this.userInfo = userInfo;
      this.authToken = authToken;
    }

    /**
     * Gets the user info.
     *
     * @return the user info
     */
    public UserInfo getUserInfo() {
      return userInfo;
    }

    /**
     * Gets the auth token.
     *
     * @return the auth token
     */
    public String getAuthToken() {
      return authToken;
    }

  }

  /**
   * The Class UserInfo.
   */
  public class UserInfo {

    /** The id. */
    private final String id;

    /** The firstname. */
    private final String firstName;

    /** The lastname. */
    private final String lastName;

    /**
     * Instantiates a new user info.
     *
     * @param id the id
     * @param firstname the firstname
     * @param lastname the lastname
     */
    public UserInfo(String id, String firstname, String lastname) {
      super();
      this.id = id;
      this.firstName = firstname;
      this.lastName = lastname;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public String getId() {
      return id;
    }

    /**
     * Gets the firstname.
     *
     * @return the firstname
     */
    public String getFirstName() {
      return firstName;
    }

    /**
     * Gets the lastname.
     *
     * @return the lastname
     */
    public String getLastName() {
      return lastName;
    }

  }
}
