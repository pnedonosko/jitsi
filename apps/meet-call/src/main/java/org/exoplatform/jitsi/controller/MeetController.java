package org.exoplatform.jitsi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("meet")
public class MeetController {

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String AUTH_TOKEN_HEADER = "X-Exoplatform-External-Auth";

  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/{meetId}")
  public String index(HttpServletRequest req, HttpServletResponse resp) {
    String authToken = req.getHeader(AUTH_TOKEN_HEADER);
    System.out.println("Meet endpoint. AUTH: " + authToken);
    return "call";
  }
}
