/**
 * Jitsi provider module for Web Conferencing. This script will be used to add a
 * provider to Web Conferencing module and then handle calls for portal
 * user/groups.
 */
(function($, webConferencing, callButton) {
  "use strict";

  var globalWebConferencing = typeof eXo != "undefined" && eXo && eXo.webConferencing ? eXo.webConferencing : null;

  // Use webConferencing from global eXo namespace (for non AMD uses).
  // This can be actual when running the script outside the portal page - e.g.
  // on a custom call page.
  if (!webConferencing && globalWebConferencing) {
    webConferencing = globalWebConferencing;
  }

  if (webConferencing) {

    // Start with default logger, later in configure() we'll get it for the
    // provider.
    // We know it's jitsi here.
    var log = webConferencing.getLog("jitsi");
    // log.trace("> Loading at " + location.origin + location.pathname);

    /**
     * An object that implements Web Conferencing SPI contract for a call
     * provider.
     */
    function JitsiProvider() {

      var self = this;
      var settings;

      /**
       * MUST return a call type name. If several types supported, this one is
       * assumed as major one and it will be used for referring this connector
       * in getProvider() and similar methods. This type also should listed in
       * getSupportedTypes(). Call type is the same as used in user profile.
       */
      this.getType = function() {
        if (settings) {
          return settings.type;
        }
      };

      /**
       * MUST return all call types supported by a connector.
       */
      this.getSupportedTypes = function() {
        if (settings) {
          return settings.supportedTypes;
        }
      };

      /**
       * MUST return human-readable title of a connector.
       */
      this.getTitle = function() {
        if (settings) {
          return settings.title;
        }
      };

      /**
       * MUST be implemented by a connector provider to build a Call button and
       * call invoked by it. Web Conferencing core provides a context object
       * where following information can be found: - currentUser - username of
       * an user that will run the call - userId - if found, it's 1:1 call
       * context, it's an username of another participant for the call - spaceId -
       * if found, it's space call, it contains a space's pretty name - roomId -
       * if found, it's eXo Chat room call, it contains a room (target) id (e.g.
       * team-we3o23o12eldm) - roomTitle - if roomId found, then roomTitle will
       * contain a human readable title - roomName - if roomId found, then
       * roomName will contain a no-space name of the room for use with Chat
       * APIs or to build connector URLs where need refer a room by its name (in
       * addition to the ID). NOTE: in case of space room, the name will contain
       * the space's pretty name prefixed with 'space-' text. - isGroup - if
       * true, it's a group call, false then 1-one-1 - details - it's
       * asynchronous function to call, it returns jQuery promise which when
       * resolved (done) will provide an object with call information. In
       * general it is a serialized to JSON Java class, extended from
       * IdentityInfo - consult related classes for full set of available bean
       * fields.
       * 
       * This method returns a jQuery promise. When it resolved (done) it should
       * offer a jQuery element of a button(s) container. When rejected
       * (failed), need return an error description text (it may be shown
       * directly to an user), the connector will not be added to the call
       * button and user will not see it.
       */
      this.callButton = function(context, buttonType) {
        var button = $.Deferred();
        if (settings && context && context.currentUser) {
          context.details().done(
              function(target) {
                var callMembers = [];
                if (target.group) {
                  // If target is a group: go through its members (this will
                  // work for both space and chat room)
                  for ( var uname in target.members) {
                    if (target.members.hasOwnProperty(uname)) {
                      var u = target.members[uname];
                      callMembers.push(u);
                    }
                  }
                } else {
                  // Otherwise it's 1:1 call
                  callMembers.push(context.currentUser);
                  callMembers.push(target);
                }
                if (callMembers.length > 1) {
                  if (buttonType === "vue") {
                    const callSettings = {};
                    callSettings.target = target;
                    callSettings.context = context;
                    callSettings.callMembers = callMembers;
                    //callSettings.callWindow = callWindow;
                    callButton.init(callSettings).then(jitsiCallButton => {
                      button.resolve(jitsiCallButton);
                   })
                    // Resolve with our button - return Vue object here, so it
                    // will be appended to Call Button UI in the Platform
                  } else {
                    // If we have more than single user, then we have participants
                    // for a call.
                    // Build jQuery element of the call button:
                    // It can be an anchor or button. It may use any custom CSS
                    // class (like myCallAction) we know that
                    // Web Conferencing may add btn class (from PLF's styles) if
                    // this connector will be a single compatible
                    // for an user.
                    // You need provide an icon and title for the button.
                    // An icon should have 'uiIconLightGray' class to look like
                    // other buttons of the Platform;
                    // any other style starting with 'uiIcon' will also inherit
                    // style settings of the Platform.
                    // Here we use 'uiIconVideoPortlet' class from Platform UI, it
                    // provides actual icon of the button.
                    // A title should be in non-block element marked by a class
                    // 'callTitle'. This will let remove
                    // a title when button should appear without it. You may add
                    // any other styles to the title.
                    var $button = $("<a title='" + target.title + "' href='javascript:void(0)' class='myCallAction'>"
                      + "<i class='uiIconMyCall uiIconVideoPortlet uiIconLightGray'></i>" + "<span class='callTitle'>"
                      + self.getCallTitle() + "</span></a>");
                    // Add click handler to the button and add logic to open a
                    // link of call window (here we assume it needs
                    // a dedicated page, but it also could be possible to run it
                    // embedded on the current page)
                    $button.click(function() {
                      // handle only of not disabled (see init())
                      //  if (!$button.hasClass("callDisabled")) {
                      // When user clicked the button - create an actual call.
                      // Use Web Conferencing helper to open a new window
                      // Build a call page URL on your own and for your needs.

                      // You can save this call in eXo to inform other parts and
                      // be able restore the call in case of page reload
                      // or on other Platform pages. Respectively, you'll need
                      // to delete the call - this could be done from a call
                      // page, but also may be done from server-side (on some
                      // event, external call, timer, etc.).
                      // Find a way informing end of the call from your actual
                      // connector capabilities.
                      //
                      // Adding (and then removing) a call is not mandatory. If
                      // your call provider inform other parts about the call
                      // by itself (e.g. via native app), you can skip
                      // adding/removing calls.
                      //
                      // To save a new call we need an ID with some info about
                      // an owner, its type, provider, title and participants.
                      // Call ID should be generated by a connector, there is no
                      // restrictions for how ID should look,
                      // but it's recommended to keep it without spaces and
                      // friendly to URLs.
                      // Below we construct an ID to simply identify call on
                      // both portal and chat pages:
                      // * for group call we prefix with (g/) with group ID
                      // (effectively:
                      // - for spaces we want space_name (known as pretty name,
                      // e.g. product_team) and
                      // if it's space room in chat we use roomName from the
                      // context
                      // - for chat room its room-name (e.g. space-121218554...
                      // or team-8978676565...)
                      // * for 1:1 prefix (p/) appended with participant IDs
                      // sorted always in same order.
                      // XXX Call ID should only contain characters supported by
                      // CometD,
                      // find more in
                      // https://docs.cometd.org/current/reference/#_bayeux_protocol_elements
                      var callId;
                      if (target.group) {
                        callId = "g_" + (target.type == "chat_room" ? context.roomName : target.id);
                      } else {
                        // Sort call members to have always the same ID for two
                        // parts independently on who started the call
                        var callMembersAsc = callMembers.map(function(member) {
                          return member.id;
                        }).slice();
                        callMembersAsc.sort();
                        callId = "p_" + callMembersAsc.join("-");
                      }

                      var callUrl = window.location.protocol + "//" + window.location.host + "/jitsi/meet/" + callId;

                      // Next we need ensure this call not yet already started
                      // (e.g. remotely),
                      // it's actual especially for group calls where user can
                      // join already running conversations
                      // As we have two cases: new call and joining a call, we
                      // use promise to complete the call page for any of cases
                      // depending on asynchronous requests to the server.
                      var callProcess = $.Deferred();
                      // Open call window before asynchronous requests to avoid
                      // browser's popup blocker
                      // window title
                      // visible to
                      // user
                      // Try get a call by the ID to know is it exists already -
                      // it why we need stable ID clearly defining the target
                      webConferencing.getCall(callId).done(function(call) {
                        // Call already running - join it
                        log.info("Joining call: " + callId);
                        callProcess.resolve(call, false);
                      }).fail(function(err) {
                        if (err) {
                          if (err.code == "NOT_FOUND_ERROR") {
                            var participatntsIds = callMembers.map(function(member) {
                              return member.id;
                            }).join(";");
                            // OK, this call not found - start a new one,
                            var callInfo = {
                              // for group calls an owner is a group entity
                              // (space or room), otherwise it's 1:1 and who
                              // started is an owner
                              owner : target.group ? target.id : context.currentUser.id,
                              // ownerType can be 'user' for 1:1 calls, 'space'
                              // for group call in space, 'chat_room' for group
                              // call in Chat room
                              ownerType : target.type, // use target type
                              provider : self.getType(),
                              // tagret's title is a group or user full name
                              title : target.title,
                              participants : participatntsIds
                              // string build from array separated by ';'
                            };
                            webConferencing.addCall(callId, callInfo).done(function(call) {
                              log.info("Call created: " + callId);
                              callProcess.resolve(call, true);
                            });
                          } else {
                            log.error("Failed to get call info: " + callId, err);
                            webConferencing.showError("Joining call error", webConferencing.errorText(err));
                          }
                        } else {
                          log.error("Failed to get call info: " + callId);
                          webConferencing.showError("Joining call error", "Error read call information from the server");
                        }
                      });
                      // We wait for call readiness and invoke start it in the
                      // popup window
                      callProcess.done(function(call, isNew) {
                        var callWindow = callWindow = webConferencing.showCallPopup(callUrl, target.title);
                        callWindow.document.title = target.title;


                        var callStarted = false;
                        webConferencing.onCallUpdate(callId, function(update){
                          console.log("Received update: " + JSON.stringify(update));
                          if (update.exoId === context.currentUser.id && update.action === "started") {
                            callStarted = true;
                          }
                        });
                        // Delete call if it hasn't been started for 15 secs after openning the call page
                        setTimeout(function(){
                          if (!callStarted) {
                            // Smth went wrong on call page. Delete call.
                            webConferencing.deleteCall(callId).then(function(){
                              console.log("The call " + callId + " hasn't been started. Deleted call");
                            });
                          }
                        }, 15000);


                        // Next, we invoke a call window to initialize the call.
                        // Note: it's assumed below that startCall() method
                        // added by the call page script,
                        // it is not defined in Web Conferencing module. You can
                        // use any namespace and way to invoke your
                        // connector methods - it's up to the implementation.
                        // Ensure the call window loaded before calling it.
                        $(callWindow).on("load", function() {
                          $button.addClass("callDisabled"); // should be
                          // removed
                          // on
                          // stop/leaved
                          // event in
                          // init()
                          $button.data("callid", callId); // Assign call
                          // ID to the
                          // button for
                          // later use
                          // (see above)

                        });
                      });
                      //    } else {
                      //      log.debug("Call disabled for " + target.id);
                      //    }
                    });
                    // Assign target ID to the button for later use on started
                    // event in init()
                    $button.data("targetid", target.id);
                    // Resolve with our button - return jQuery object here, so it
                    // will be appended to Call Button UI in the Platform
                    button.resolve($button);
                  }
                } else {
                  // If not users compatible with Jitsi IM type found, we
                  // reject, thus don't show the button for this context
                  var msg = "No " + self.getTitle() + " users found for " + target.id;
                  log.warn(msg);
                  button.reject(msg);
                }
              }).fail(function(err) {
            // On error, we don't show the button for this context
            if (err && err.code == "NOT_FOUND_ERROR") {
              // If target not found, for any reason, we don't need tell it's an
              // error - just no button for the target
              button.reject(err.message);
            } else {
              // For other failures we seems met an error (server or network)
              // and send it as a second parameter,
              // thus the core add-on will be able recognize it and do
              // accordingly (at least log to server log)
              var msg = "Error getting context details";
              log.error(msg, err);
              button.reject(msg, err);
            }
          });
        } else {
          // If not initialized, we don't show the button for this context
          var msg = "Not configured or empty context";
          log.error(msg);
          button.reject(msg);
        }
        // Return a promise, when resolved it will be used by Web Conferencing
        // core to add a button to a required places
        return button.promise();
      };

      /**
       * Helper method to build an incoming call popup.
       */
      var acceptCallPopover = function(callerLink, callerAvatar, callerMessage, playRingtone) {
        log.trace(">> acceptCallPopover '" + callerMessage + "' caler:" + callerLink + " avatar:" + callerAvatar);
        var process = $.Deferred();
        var $call = $("div.uiIncomingCall");
        // Remove previous dialogs (if you need handle several incoming at the
        // same time - implement special logic for this)
        if ($call.length > 0) {
          try {
            // By destroying a dialog, it should reject its incoming call
            $call.dialog("destroy");
          } catch (e) {
            log.error("acceptCallPopover: error destroing previous dialog ", e);
          }
          $call.remove();
        }
        $call = $("<div class='uiIncomingCall' title='Incoming call'></div>");
        $call.append($("<div class='messageAuthor'><a target='_blank' href='" + callerLink + "' class='avatarMedium'>"
            + "<img src='" + callerAvatar + "'></a></div>" + "<div class='messageBody'><div class='messageText'>" + callerMessage
            + "</div></div>"));
        $(document.body).append($call);
        $call.dialog({
          resizable : false,
          height : "auto",
          width : 400,
          modal : false,
          buttons : {
            "Answer" : function() {
              process.resolve("accepted");
              $call.dialog("close");
            },
            "Decline" : function() {
              process.reject("declined");
              $call.dialog("close");
            }
          }
        });
        $call.on("dialogclose", function(event, ui) {
          if (process.state() == "pending") {
            process.reject("closed");
          }
        });
        process.notify($call);
        if (playRingtone) {
          // Start ringing incoming sound only if requested (depends on user
          // status)
          var $ring = $("<audio loop autoplay style='display: none;'>" // controls
              + "<source src='/jitsi/resources/audio/incoming.mp3' type='audio/mpeg'>"
              + "Your browser does not support the audio element.</audio>");
          $(document.body).append($ring);
          process.fail(function() {
            if ($call.callState != "joined") {
              var $cancel = $("<audio autoplay style='display: none;'>" // controls
                  + "<source src='/jitsi/resources/audio/incoming_cancel.mp3' type='audio/mpeg'>"
                  + "Your browser does not support the audio element.</audio>");
              $(document.body).append($cancel);
              setTimeout(function() {
                $cancel.remove();
              }, 2500);
            }
          });
          process.always(function() {
            // Stop incoming ringing on dialog completion
            $ring.remove();
          });
        }
        return process.promise();
      };

      /**
       * OPTIONAL method. If implemented, it will be called by Web Conferencing
       * core on addProvider() method. It is assumed that the connector will
       * initialize internals depending on the given context.
       */
      this.init = function(context) {
        var process = $.Deferred();
        if (eXo && eXo.env && eXo.env.portal) {
          // We want initialize call buttons and incoming calls dialog only for
          // portal pages (including Chat)
          var currentUserId = webConferencing.getUser().id;
          // Incoming call popup (embedded into the current page), it is based
          // on jQueryUI dialog widget
          var $callPopup;
          var closeCallPopup = function(callId, state) {
            if ($callPopup && $callPopup.callId && $callPopup.callId == callId) {
              if ($callPopup.is(":visible")) {
                // Set state before closing the dialog, it will be used by
                // promise failure handler
                $callPopup.callState = state;
                $callPopup.dialog("close");
              }
            }
          };
          // When call is already running we want lock a call button and then
          // unlock on stop.
          // As we may find several call buttons on eXo pages, need update only
          // related to the call.
          // On space pages (space call button) we can rely on call ownerId (it
          // will be a space pretty_name),
          // for Chat page we need use its internal room name to distinguish
          // rooms and ownerId for users.
          var lockCallButton = function(targetId, callId) {
            var $buttons = $(".myCallAction");
            $buttons.each(function() {
              var $button = $(this);
              if ($button.data("targetid") == targetId) {
                if (!$button.hasClass("callDisabled")) {
                  // log.trace(">> lockCallButton " + targetId);
                  // TODO: add class (removed for testing)
                  // $button.addClass("callDisabled");
                  $button.data("callid", callId);
                }
              }
            });
          };
          var unlockCallButton = function(callId) {
            var $buttons = $(".myCallAction");
            $buttons.each(function() {
              var $button = $(this);
              if ($button.data("callid") == callId) {
                // log.trace(">> unlockCallButton " + callId + " " +
                // $button.data("targetid"));
                $button.removeClass("callDisabled");
                $button.removeData("callid"); // we don't touch targetid, it
                // managed by callButton()
              }
            });
          };
          // Subscribe to user updates (incoming calls will be notified here)
          webConferencing.onUserUpdate(currentUserId, function(update) {
            // This connector cares only about own provider events
            if (update.providerType == self.getType()) {
              var callId = update.callId;
              if (update.eventType == "call_state") {
                // A call state changed (can be 'started', 'stopped', 'paused'
                // (not used for the moment)
                // rely on logic implemented in callButton() here: group call ID
                // starts with 'g/'
                var isGroup = callId.startsWith("g_");
                log.trace(">>> User call state updated: " + JSON.stringify(update));
                if (update.callState == "started") {
                  // When call started it means we have an incoming call for
                  // this particular user
                  log.info("Incoming call: " + callId);
                  // Get call details by ID
                  webConferencing.getCall(callId).done(
                      function(call) {
                        var callerId = call.owner.id;
                        var callerLink = call.owner.profileLink;
                        var callerAvatar = call.owner.avatarLink;
                        var callerMessage = call.owner.title + " is calling you...";
                        var callerRoom = callerId;
                        call.title = call.owner.title; // for callee the call
                        // title is a caller
                        // name
                        // Get current user status, we need this to figure out a
                        // need of playing ringtone
                        // we'll do for users with status 'Available' or 'Away',
                        // but ones with 'Do Not Disturb' will not hear an
                        // incoming ring.
                        webConferencing.getUserStatus(currentUserId).done(
                            function(user) {
                              // Build a call popover
                              var popover = acceptCallPopover(callerLink, callerAvatar, callerMessage, !user
                                  || user.status == "available" || user.status == "away");
                              // We use the popover promise to finish
                              // initialization on its progress state, on
                              // resolved (done)
                              // to act on accepted call and on rejected (fail)
                              // on declined call.
                              popover.progress(function($callDialog) {
                                // Finish initialization...
                                $callPopup = $callDialog;
                                // And some extra info to distinguish the popup
                                $callPopup.callId = callId;
                                $callPopup.callState = update.callState;
                              });
                              popover.done(function(msg) {
                                // User accepted the call...
                                log.info("User " + msg + " call: " + callId);
                                var longTitle = self.getTitle() + " " + self.getCallTitle();
                                
                                var callUrl = window.location.protocol + "//" + window.location.host + "/jitsi/meet/" + encodeURIComponent(callId);
                                
                                var callWindow = webConferencing.showCallPopup(callUrl, longTitle);
                                callWindow.document.title = call.title;
                                // Optionally, we may invoke a call window to
                                // initialize the call.
                                // First wait the call window loaded
                                $(callWindow).on("load", function() {
                                  log.debug("Call page loaded: " + callId);
                                  lockCallButton(update.owner.id, callId);
                                });
                              });
                              popover.fail(function(err) {
                                // User rejected the call, call was stopped or
                                // joined on another client/page.
                                if (!isGroup && $callPopup.callState != "stopped" && $callPopup.callState != "joined") {
                                  // Delete the call if it is not group one, not
                                  // already stopped and wasn't joined -
                                  // a group call will be deleted automatically
                                  // when last party leave it.
                                  log.trace("<<< User " + err + ($callPopup.callState ? " just " + $callPopup.callState : "")
                                      + " call " + callId + ", deleting it.");
                                  webConferencing.deleteCall(callId).done(function() {
                                    log.info("Call deleted: " + callId);
                                  }).fail(function(err) {
                                    if (err && (err.code == "NOT_FOUND_ERROR")) {
                                      // already deleted
                                      log.trace("<< Call not found " + callId);
                                    } else {
                                      log.error("Failed to stop call: " + callId, err);
                                      webConferencing.showError("Error stopping call", webConferencing.errorText(err));
                                    }
                                  });
                                }
                              });
                            }).fail(
                            function(err) {
                              log.error("Failed to get user status: " + currentUserId, err);
                              if (err) {
                                webConferencing.showError("Incoming call error", webConferencing.errorText(err));
                              } else {
                                webConferencing.showError("Incoming call error",
                                    "Error read user status information from the server");
                              }
                            });
                      }).fail(function(err) {
                    log.error("Failed to get call info: " + callId, err);
                    if (err) {
                      webConferencing.showError("Incoming call error", webConferencing.errorText(err));
                    } else {
                      webConferencing.showError("Incoming call error", "Error read call information from the server");
                    }
                  });
                } else if (update.callState == "stopped") {
                  log.info("Call stopped remotelly: " + callId);
                  // Hide call popover for this call, if any
                  closeCallPopup(callId, update.callState);
                  // Unclock the call button
                  unlockCallButton(callId);
                }
              } else if (update.eventType == "call_joined") {
                log.debug("User call joined: " + update.callId);
                // If user has incoming popup open for this call (in several
                // user's windows/clients), then close it
                if (currentUserId == update.part.id) {
                  closeCallPopup(callId, "joined");
                }
              } else if (update.eventType == "call_leaved") {
                log.debug("User call leaved: " + update.callId);
                // When user leaves a call, we unlock his button, thus it will
                // be possible to join the call again -
                // actual for group conversations.
                if (currentUserId == update.part.id) {
                  unlockCallButton(callId);
                }
              } else {
                log.debug("Unexpected user update: " + JSON.stringify(update));
              }
            } // it's other provider type - skip it
          }, function(err) {
            log.error("Failed to listen on user updates", err);
          });
        }
        process.resolve();
        return process.promise();
      };

      /**
       * OPTIONAL method. If implemented, it will cause showing a settings
       * button in Web Conferencing Administration page and when button clicked
       * this method will be invoked. In this method you can show a popup to an
       * admin user with provider specific settings.
       */
      this.showSettings = function() {
        // load HTML with settings
        var $popup = $("#jitsi-settings-popup");
        if ($popup.length == 0) {
          $popup = $("<div class='uiPopupWrapper' id='jitsi-settings-popup' style='display: none;'><div>");
          $(document.body).append($popup);
        }
        $popup.load("/jitsi/settings", function(content, textStatus) {
          if (textStatus == "success" || textStatus == "notmodified") {
            var $settings = $popup.find(".settingsForm");
            // TODO fill settings form and handle its changes to update the
            // settings on the server (e.g. by using your provider REST service)
            // .....
            // Show the settings popup when ready
            $popup.show();
          } // otherwise it's error
        });
      };

      // ****** Custom methods required by the connector itself or dependent on
      // it modules ******

      /**
       * Set connector settings from the server-side. Will be called by script
       * of JitsiPortlet class.
       */
      this.configure = function(mySettings) {
        settings = mySettings;
      };

      /**
       * Used in the callButton() code. Also can be used by dependent modules
       * (e.g. when need run a call page in a window).
       */
      this.getApiClientId = function() {
        if (settings) {
          return settings.apiClientId;
        }
      };

      /**
       * Used in the callButton() code. Also can be used by dependent modules
       * (e.g. when need run a call page in a window).
       */
      this.getUrl = function() {
        if (settings) {
          return settings.url;
        }
      };

      /**
       * Used in the callButton() code. Also can be used by dependent modules (e.g. when need run a call page in a window).
       */
      this.getCallTitle = function() {
        return "Jitsi Call"; // TODO i18n
      };

      /**
       * Sample function used by JitsiIMRenderer to show how IM type renderer can be initialized.
       */
      this.initSettings = function(mySettings) {
        // initialize IM type settings UI
      };
    }

    var provider = new JitsiProvider();

    // Add Jitsi provider into webConferencing object of global eXo namespace (for non AMD uses)
    if (globalWebConferencing) {
      globalWebConferencing.jitsi = provider;
    } else {
      log.warn("eXo.webConferencing not defined");
    }

    log.trace("< Loaded at " + location.origin + location.pathname);
    return provider;
  } else {
    window.console
        && window.console
            .log("WARN: webConferencing not given and eXo.webConferencing not defined. Jitsi provider registration skipped.");
  }
})($, webConferencing, callButton);
