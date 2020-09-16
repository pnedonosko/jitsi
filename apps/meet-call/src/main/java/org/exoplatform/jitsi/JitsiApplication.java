package org.exoplatform.jitsi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JitsiApplication {

  @Value("${exo.jwt.secret}")
  private String secret;
  
  public static void main(String[] args) {
    SpringApplication.run(JitsiApplication.class, args);
  }

  @Bean
  public FilterRegistrationBean<TokenAuthenticationFilter> tokenAuthFilter() {
    FilterRegistrationBean<TokenAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(new TokenAuthenticationFilter(secret));
    registrationBean.addUrlPatterns("/meet/*");
    registrationBean.addUrlPatterns("/api/*");
    return registrationBean;
  }

}
