package org.exoplatform.jitsi;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

/**
 * The Class TokenAuthenticationFilter.
 */
public class TokenAuthenticationFilter implements Filter {

  /** The secret. */
  private String              secret;

  /** The Constant AUTH_TOKEN_HEADER. */
  private final static String AUTH_TOKEN_HEADER = "X-Exoplatform-External-Auth";

  /**
   * Instantiates a new token authentication filter.
   *
   * @param secret the secret
   */
  public TokenAuthenticationFilter(String secret) {
    this.secret = secret;
  }

  /**
   * Do filter.
   *
   * @param request the request
   * @param response the response
   * @param chain the chain
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    String authToken = req.getHeader(AUTH_TOKEN_HEADER);
    if (authToken != null && !authToken.trim().isEmpty()) {
      if (verifyToken(authToken)) {
        chain.doFilter(request, response);
      } else {
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.getWriter().write("{\"error\":\"The auth token is not valid\"}");
        res.flushBuffer();
      }
    } else {
      res.setStatus(HttpStatus.UNAUTHORIZED.value());
      res.getWriter().write("{\"error\":\"The auth token is not provided\"}");
      res.flushBuffer();
    }
  }

  /**
   * Verify token.
   *
   * @param token the token
   * @return true, if successful
   */
  private boolean verifyToken(String token) {
    try {
      Algorithm algorithm = Algorithm.HMAC384(secret.getBytes());
      JWTVerifier verifier = JWT.require(algorithm).withSubject("exo-webconf").build(); // Reusable verifier instance
      DecodedJWT jwt = verifier.verify(token);
      String action = jwt.getClaim("action").asString();
      if ("external_auth".equals(action)) {
        return true;
      }
    } catch (JWTVerificationException exception) {
      System.out.println("Cannot verify token: " + exception.getMessage());
    }
    return false;
  }

}
