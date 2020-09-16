package org.exoplatform.jitsi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("meet")
public class MeetController {

  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/{meetId}")
  public String index(HttpServletRequest req, HttpServletResponse resp) {
    return "call";
  }
}
