package org.exoplatform.jitsi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("jitsi/call")
public class CallController {

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String AUTH_TOKEN_HEADER = "X-Exoplatform-Auth";

  /** The Constant IDENTITY_HEADER. */
  private final static String IDENTITY_HEADER   = "X-Exoplatform-Identity";
  
  /** The Constant IDENTITY_HEADER. */
  private final static String GUEST_USERNAME   = "X-Exoplatform-Guest-Username";

  // Auth endpoint ( will be open in iframe from eXo for setting the token to local storage )
  @GetMapping("/{callId}")
  public String index(HttpServletRequest req, HttpServletResponse resp) {
    String authToken = req.getHeader(AUTH_TOKEN_HEADER);
    String identity = req.getHeader(IDENTITY_HEADER);
    String inviteId = req.getParameter("inviteId");
    
    if(inviteId != null) {
      // TODO: Get username from database
      resp.setHeader(GUEST_USERNAME, "Mock Guest Username");
    }
    
    System.out.println("IDENTITY: " + identity);
    System.out.println("AUTH: " + authToken);
    
    return "call";
  }
}
