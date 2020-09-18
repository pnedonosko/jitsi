package org.exoplatform.jitsi.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

  @GetMapping("/")
  public Map<String, String> status() {
    HashMap<String, String> map = new HashMap<>();
    map.put("status", "active");
    return map;
  }

}
