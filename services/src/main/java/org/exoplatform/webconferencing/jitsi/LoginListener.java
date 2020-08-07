package org.exoplatform.webconferencing.jitsi;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

public class LoginListener extends Listener<ConversationRegistry, ConversationState> {

  /** The Constant LOG. */
  protected static final Log  LOG              = ExoLogger.getLogger(LoginListener.class);

  /** The Constant CLIENT_ID_COOKIE. */
  private static final String CLIENT_ID_COOKIE = "jitsi-client-id";

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {

  }
}
