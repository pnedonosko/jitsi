package org.exoplatform.jitsi.controller;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.server.PathParam;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class APIController {

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String AUTH_TOKEN_HEADER = "X-Exoplatform-Auth";

  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/invite/{inviteId}")
  public Map<String, String> invite(@RequestHeader(AUTH_TOKEN_HEADER) String authToken, @PathParam("inviteId") String inviteId) {
    System.out.println("InviteId: " + inviteId);
    System.out.println("AUTH: " + authToken);
    HashMap<String, String> map = new HashMap<>();
    map.put("username", "guest-123");
    return map;
  }
}
