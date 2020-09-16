package org.exoplatform.jitsi.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class APIController {


  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/userinfo/{inviteId}")
  public Map<String, String> userinfo(@PathVariable("inviteId") String inviteId) {
    HashMap<String, String> map = new HashMap<>();
    map.put("id", "guest-" + inviteId);
    map.put("firstName", "Special");
    map.put("lastName", "Guest");
    return map;
  }
}
