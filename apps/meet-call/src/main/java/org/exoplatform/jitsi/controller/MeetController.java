package org.exoplatform.jitsi.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("meet")
public class MeetController {
  
  @Value("${jitsi.app.url}")
  private String appUrl;

  @GetMapping("/{meetId}")
  public String index(Model model) {
    model.addAttribute("appUrl", appUrl);
    return "call";
  }

}
