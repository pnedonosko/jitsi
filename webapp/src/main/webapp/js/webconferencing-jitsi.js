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
       * Request a status of Jitsi Call App
       */
      var getStatus = function(inviteId) {
        return $.get({
          type: "GET",
          url: "/jitsi",
        });
      };

      /**
       * Returns call members
       */
      var getCallMembers = function(currentUser, target) {
        var callMembers = [];
        if (target.group) {
          // If target is a group: go through its members (this will
          // work for both space and chat room)
          for (var uname in target.members) {
            if (target.members.hasOwnProperty(uname)) {
              var u = target.members[uname];
              callMembers.push(u);
            }
          }
        } else {
          // Otherwise it's 1:1 call
          callMembers.push(currentUser);
          callMembers.push(target);
        }
        return callMembers;
      };

      /**
       * Creates callId for given context and target
       */
      var getCallId = function(context, target) {
        var callId;
        if (target.group) {
          callId = "g_" + (target.type == "chat_room" ? context.roomName : target.id);
        } else {
          // Sort call members to have always the same ID for two
          // parts independently on who started the call
          var callMembersAsc = getCallMembers(context.currentUser, target).map(function(member) {
            return member.id;
          }).slice();
          callMembersAsc.sort();
          callId = "p_" + callMembersAsc.join("-");
        }
        // Transliterate callId
        return window.slugify(callId);
      };

      /**
       * Returns call url
       */
      var getCallUrl = function(callId) {
        return window.location.protocol + "//" + window.location.host + "/jitsi/meet/" + callId;
      };

      /**
       * Creates a new call, returns promise with call object when resolved.
       */
      var createCall = function(callId, currentUser, target) {
        var participatntsIds = getCallMembers(currentUser, target).map(function(member) {
          return member.id;
        }).join(";");
        // OK, this call not found - start a new one,
        var callInfo = {
          // for group calls an owner is a group entity
          // (space or room), otherwise it's 1:1 and who
          // started is an owner
          owner: target.group ? target.id : currentUser.id,
          // ownerType can be 'user' for 1:1 calls, 'space'
          // for group call in space, 'chat_room' for group
          // call in Chat room
          ownerType: target.type, // use target type
          provider: self.getType(),
          // tagret's title is a group or user full name
          title: target.title,
          participants: participatntsIds
          // string build from array separated by ';'
        };
        return webConferencing.addCall(callId, callInfo);
      };


      var startCall = function(context, target) {
        var callProcess = $.Deferred();
        var callId = getCallId(context, target);
        // Open call window
        var callWindow = webConferencing.showCallWindow("", self.getTitle() + " " + callId);
        getStatus().then(function(response) {
          if (response.status === "active") {
            webConferencing.getCall(callId).done(function(call) {
              // Call already running - join it
              log.info("Call already exists. Joining call: " + callId);
              // For grop calls
              // Note: webconf starts call when first user joins it
              if (call.state === "stopped" && target.type === "chat_room") {
                var participants = Object.values(target.members).map(function(member) {
                  return member.id;
                });
                webConferencing.updateParticipants(callId, participants);
              }
              callProcess.resolve(call);
            }).fail(function(err) {
              if (err) {
                if (err.code == "NOT_FOUND_ERROR") {
                  createCall(callId, context.currentUser, target).done(function(call) {
                    log.info("Call created: " + callId);
                    callProcess.resolve(call);
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
          } else {
            callProcess.reject("The Call App is not active");
          }
        }).catch(function(err) {
          callProcess.reject("The Call App is temporary unavailable.");
        });

        // We wait for call readiness and invoke start it in the
        // popup window
        callProcess.done(function(call) {
          callWindow.location = getCallUrl(callId);
          callWindow.document.title = call.title; // TODO was target.title
        }).fail(function(msg) {
          callWindow.close();
          setTimeout(function() {
            alert("Cannot open call page. " + msg);
          }, 50);

        });
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
              if (!buttonType || buttonType === "vue") {
                const callSettings = {};
                callSettings.target = target;
                callSettings.context = context;
                callSettings.provider = self;
                callSettings.onCallOpen = function() {
                  startCall(context, target);
                };
                // callSettings.callWindow = callWindow;
                callButton.init(callSettings).then(comp => {
                  button.resolve(comp);
                });
                // Resolve with our button - return Vue object here, so it
                // will be appended to Call Button UI in the Platform
              } else if (buttonType === "element") {
                var $button = $("<a title='" + target.title + "' href='javascript:void(0)' class='myCallAction'>" +
                  "<i class='uiIconMyCall uiIconVideoPortlet uiIconLightGray'></i>" + "<span class='callTitle'>" +
                  self.getCallTitle() + "</span></a>");
                $button.click(function() {
                  startCall(context, target);
                });
                $button.data("targetid", target.id);
                button.resolve($button);
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
      // var _acceptCallPopover = function(callerId, callerLink, callerAvatar, callerMessage, playRingtone) {
      //   log.trace(">> acceptCallPopover '" + callerMessage + "' caler:" + callerId + " (" + callerLink + ", avatar:" + callerAvatar + ")");
      //   var process = $.Deferred();
      //   var $call = $("div.uiIncomingCall");
      //   // Remove previous dialogs (if you need handle several incoming at the
      //   // same time - implement special logic for this)
      //   if ($call.length > 0) {
      //     try {
      //       // By destroying a dialog, it should reject its incoming call
      //       $call.dialog("destroy");
      //     } catch (e) {
      //       log.error("acceptCallPopover: error destroing previous dialog ", e);
      //     }
      //     $call.remove();
      //   }
      //   $call = $("<div class='uiIncomingCall' title='Incoming call'></div>");
      //   $call.append($("<div class='messageAuthor'><a target='_blank' href='" + callerLink + "' class='avatarMedium'>" +
      //     "<img src='" + callerAvatar + "'></a></div>" + "<div class='messageBody'><div class='messageText'>" + callerMessage +
      //     "</div></div>"));
      //   $(document.body).append($call);
      //   $call.dialog({
      //     resizable: false,
      //     height: "auto",
      //     width: 400,
      //     modal: false,
      //     buttons: {
      //       "Answer": function() {
      //         process.resolve("accepted");
      //         $call.dialog("close");
      //       },
      //       "Decline": function() {
      //         process.reject("declined");
      //         $call.dialog("close");
      //       }
      //     }
      //   });
      //   $call.on("dialogclose", function(event, ui) {
      //     if (process.state() == "pending") {
      //       process.reject("closed");
      //     }
      //   });
      //   process.notify($call);
        // if (playRingtone) {
        //   const ringId = "jitsi-call-ring-" + callerId;
        //   let $ring;
        //   let callRinging = localStorage.getItem(ringId);
        //   if (!callRinging || Date.now() - callRinging.time > 5000) {
        //     log.trace(">>> Ringing the caller: " + callerId);
        //     // if not rnging or ring flag too old (for cases of crashed browser page w/o work in process.always below)
        //     localStorage.setItem(ringId, {
        //       time: Date.now()
        //     }); // set it quick as possible to avoid rice conditions
        //     callRinging = true;
        //     // Start ringing incoming sound only if requested (depends on user status)
        //     // TODO ringtone was incoming.mp3 type='audio/mpeg' -- Oct 29, 2020
        //     $ring = $("<audio loop autoplay style='display: none;'>" +
        //       "<source src='/jitsi/resources/audio/ringtone_exo-1.m4a'>" +
        //       "Your browser does not support the audio element.</audio>");
        //     $(document.body).append($ring);
        //   }
      //     process.fail(function() {
      //       if ($call.callState != "joined") {
      //         var $cancel = $("<audio autoplay style='display: none;'>" +
      //           "<source src='/jitsi/resources/audio/incoming_cancel.mp3' type='audio/mpeg'>" +
      //           "Your browser does not support the audio element.</audio>");
      //         $(document.body).append($cancel);
      //         setTimeout(function() {
      //           $cancel.remove();
      //         }, 2500);
      //       }
      //     });
      //     process.always(function() {
      //       // Stop incoming ringing on dialog completion
            // if (callRinging) {
            //   localStorage.removeItem(ringId);
            // }
            // if ($ring) {
            //   $ring.remove();
            //   log.trace("<<< Ringing stopped: " + callerId);
            // }
      //     });
      //   }
      //   return process.promise();
      // };

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
          // var $callPopup;


          let callPopUp;

          
          var closeCallPopup = function(callId, state) {
            // if (callPopUp && callPopUp.callId && callPopUp.callId == callId) {
              // if ($callPopUp.is(":visible")) {
                // Set state before closing the dialog, it will be used by
                // promise failure handler
                console.log("CLOSECALLPOPUP");
                console.log(callPopUp);
                callPopUp.callState = state;
                // acceptCallPopoverVueComp.close();
                callPopUp.close();
                // $callPopup.dialog("close");
              // }
            // }
          };

          // var readCallWindow = function(callId) {
          //   if (typeof Storage != "undefined") {
          //     return localStorage.getItem(LOCAL_CALL_PREFIX + callId);
          //   } else {
          //     return null;
          //   }
          // };

          // var playSound = function(sound) {
          //   if(sound) {
          //     var audio = new Audio(sound);
          //     audio.play();
          //   }
          // }
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

                var callPopupRing = {
                  incomingSound: '/jitsi/resources/audio/ringtone_exo-1.m4a',
                  declineSound: '/jitsi/resources/audio/incoming_cancel.mp3',
                  doneSound: '/jitsi/resources/audio/done.mp3',
                  decline: false,
                //   incomingAudio: `<audio id="call-popup-ring" preload="auto" autoplay loop style="display: none;">
                //   <source src="/jitsi/resources/audio/incoming.mp3">
                //   <p>Your browser does not support the <code>audio</code> element.</p>
                // </audio>`,
                //   declineAudio: `<audio id="call-popup-decline" preload="auto" style="display: none;">
                //     <source src="/jitsi/resources/audio/incoming_cancel.mp3">
                //     <p>Your browser does not support the <code>audio</code> element.</p>
                //   </audio>`,
                //   doneAudio: `<audio id="call-popup-done" preload="auto" style="display: none;">
                //     <source src="/jitsi/resources/audio/done.mp3">
                //     <p>Your browser does not support the <code>audio</code> element.</p>
                //   </audio>`,
                  // loop: true,
                  playRing: function(audio) {
                    audio.play();
                  },
                  pauseRing: function(audio) {
                    audio.pause();
                    audio.currentTime = 0;
                  }
                }
                var audio;
                var declineAudio;
  
                var createAudio = function(el, id, sound, autoplay, loop) {
                  el = document.createElement("AUDIO");
                  el.setAttribute("id", id);
                  el.setAttribute("src", sound);
                  el.setAttribute("preload", "auto");
                  autoplay ? el.setAttribute("autolpay", autoplay) : el.removeAttribute("autolpay", autoplay);
                  loop ? el.setAttribute("loop", loop) :   el.removeAttribute("loop", loop);
                  el.style.display = "none";
                  console.log(el);
                  document.body.appendChild(el);
                }
                createAudio(audio, "call-popup-ring", "/jitsi/resources/audio/ringtone_exo-1.m4a", "true", "true");
                // createAudio(declineAudio, "call-popup-decline", "/webrtc/audio/echo.mp3", "false", "false");
                if (update.callState == "started") {
                  // When call started it means we have an incoming call for
                  // this particular user
                  log.info("Incoming call: " + callId);
                  // Get call details by ID
                  webConferencing.getCall(callId).done(
                    function(call) {


                      // var callWindow;
											// 		var callWindowId = readCallWindow(callId);
											// 		if (callWindowId) {
											// 			callWindow = webConferencing.showCallPopup("", callWindowId);
											// 		}
											// 		// here this window will be blocked by popup blocker and will be undefined
											// 		// need use messaging between windows to know if a call is actually running
											// 		if (callWindow && callWindow.location.href !== "about:blank") {
											// 			// call already running in this browser - don't need ask the user for it
											// 			log.trace(">>> Call alreadey joined and running: " + callWindowId);
											// 		} else {
											// 			if (callWindow) {
											// 				callWindow.close();
                      //       }
                      //     }

                      


                      var callerId = call.owner.id;
                      var callerLink = call.owner.profileLink;
                      var callerAvatar = call.owner.avatarLink;
                      const styledOwnerTitle = call.owner.title.bold();
                      var callerMessage = !isGroup ? styledOwnerTitle + " started a Meeting with you." : "A meeting has started in the room " + styledOwnerTitle;
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
                          // var popover = acceptCallPopover(callerId, callerLink, callerAvatar, callerMessage, !user ||
                            // user.status == "available" || user.status == "away");
                          // We use the popover promise to finish
                          // initialization on its progress state, on
                          // resolved (done)
                          // to act on accepted call and on rejected (fail)
                          // on declined call.
                          let playRingtone = !user || user.status == "available" || user.status == "away";

                          callButton.initCallPopup(callId, update.callState, callerId, callerLink,  callerAvatar, callerMessage, playRingtone, callPopupRing).then((callPopup) => {
                            callPopUp = callPopup;
                            // createAudio(audio, "call-popup-ring", "/webrtc/audio/line.mp3", "true", "true");
                            createAudio(declineAudio, "call-popup-decline", "/jitsi/resources/audio/incoming_cancel.mp3");
                            audio = document.getElementById("call-popup-ring");
                            declineAudio = document.getElementById("call-popup-decline");
                            if (playRingtone) {
                              var audio = document.getElementById("call-popup-ring");
                              var declineAudio = document.getElementById("call-popup-decline");
                              // const ringId = "jitsi-call-ring-" + callerId;
                              const ringId = "jitsi-call-ring-" + callerId;
                              // let $ring;
                              let callRinging = JSON.parse(localStorage.getItem(ringId));
                              //log.trace(callRinging);
                              if (!callRinging || Date.now() - callRinging.time > 5000) {
                                callPopupRing.playRing(audio);
                                // for(let i = 0; playRingtone === true; i++) {
                                //   playSound('/webrtc/audio/line.mp3');
                                // }
                                // log.trace(">>> Ringing the caller: " + callerId);
                                // if not rnging or ring flag too old (for cases of crashed browser page w/o work in process.always below)
                                localStorage.setItem(ringId, JSON.stringify({
                                 time: Date.now()
                                })); // set it quick as possible to avoid rice conditions
                                
                                // callRinging = true;
                                // Start ringing incoming sound only if requested (depends on user status)
                                // TODO ringtone was incoming.mp3 type='audio/mpeg' -- Oct 29, 2020
                                // playSound('/jitsi/resources/audio/ringtone_exo-1.m4a');
                                // $ring = $("<audio loop autoplay style='display: none;'>" +
                                //   "<source src='/webrtc/audio/line.mp3' type='audio/mpeg'>" +
                                //   "Your browser does not support the audio element.</audio>");
                                // $(document.body).append($ring);
                                // playSound('/webrtc/audio/line.mp3');
                                
                              }

                            } 
                            callPopup.onAccepted(() => {
                              // playRingtone = false;
                                log.info("User accepted call: " + callId);
                                //var callUrl = window.location.protocol + "//" + window.location.host + "/jitsi/meet/" + encodeURIComponent(callId);
                                var callUrl = getCallUrl(callId);
                                var callWindow = webConferencing.showCallWindow(callUrl, self.getTitle() + " " + callId);
                                callWindow.document.title = call.title;
                                // if (callRinging) {
                                //   localStorage.removeItem(ringId);
                                // }

                                if (audio) {
                                  callPopupRing.pauseRing(audio);
                                  // $ring.remove();
                                  // log.trace("<<< Ringing stopped: " + callerId);
                                }

                              });
                              callPopup.onRejected(() => {
                                if (!isGroup && callPopup.callState != "stopped" && callPopup.callState != "joined") {
                                  // callPopupRing.decline = true;
                                  // Delete the call if it is not group one, not
                                  // already stopped and wasn't joined -
                                  // a group call will be deleted automatically
                                  // when last party lea ve it.
                                  if (audio) {
                                    callPopupRing.pauseRing(audio);
                                    console.log(declineAudio, "declineAudio")
                                    // if (declineAudio) {
                                        setTimeout(function() {
                                          callPopupRing.playRing(declineAudio);
                                        }, 300)
                                        callPopupRing.pauseRing(declineAudio);
                                    // }
                                    // $ring.remove();
                                    // log.trace("<<< Ringing stopped: " + callerId);
                                  }
                                  // closeCallPopup(callId, "started");

                                  log.trace("<<< User declined " + (callPopup.callState ? " just " + callPopup.callState : "") +
                                    " call " + callId + ", deleting it.");
                                  webConferencing.deleteCall(callId).done(function() {
                                    // if (callRinging) {
                                    //   localStorage.removeItem(ringId);
                                    // }
                                    
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
                            })
                          
                          .catch(error => {
                            log.error(error)
                          })
                          // popover.progress(function($callDialog) {
                          //   // Finish initialization...
                          //   $callPopup = $callDialog;
                          //   // And some extra info to distinguish the popup
                          //   $callPopup.callId = callId;
                          //   $callPopup.callState = update.callState;
                          // });
                          // popover.done(function(msg) {
                          //   // User accepted the call...
                          //   log.info("User " + msg + " call: " + callId);
                          //   //var callUrl = window.location.protocol + "//" + window.location.host + "/jitsi/meet/" + encodeURIComponent(callId);
                          //   var callUrl = getCallUrl(callId);
                          //   var callWindow = webConferencing.showCallWindow(callUrl, self.getTitle() + " " + callId);
                          //   callWindow.document.title = call.title;
                          //   // Optionally, we may invoke a call window to
                          //   // initialize the call.
                          //   // First wait the call window loaded
                          //   //$(callWindow).on("load", function() {
                          //   //  log.debug("Call page loaded: " + callId);
                          //   //  lockCallButton(update.owner.id, callId); // TODO cleanup
                          //   //});
                          // });
                          // popover.fail(function(err) {
                          //   // User rejected the call, call was stopped or
                          //   // joined on another client/page.
                          //   if (!isGroup && $callPopup.callState != "stopped" && $callPopup.callState != "joined") {
                          //     // Delete the call if it is not group one, not
                          //     // already stopped and wasn't joined -
                          //     // a group call will be deleted automatically
                          //     // when last party leave it.
                          //     log.trace("<<< User " + err + ($callPopup.callState ? " just " + $callPopup.callState : "") +
                          //       " call " + callId + ", deleting it.");
                          //     webConferencing.deleteCall(callId).done(function() {
                          //       log.info("Call deleted: " + callId);
                          //     }).fail(function(err) {
                          //       if (err && (err.code == "NOT_FOUND_ERROR")) {
                          //         // already deleted
                          //         log.trace("<< Call not found " + callId);
                          //       } else {
                          //         log.error("Failed to stop call: " + callId, err);
                          //         webConferencing.showError("Error stopping call", webConferencing.errorText(err));
                          //       }
                          //     });
                          //   }
                          // });
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
                  //unlockCallButton(callId); // TODO cleanup
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
                //if (currentUserId == update.part.id) {
                //  unlockCallButton(callId); // TODO cleanup
                //}
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
       * Used in the callButton() code. Also can be used by dependent modules
       * (e.g. when need run a call page in a window).
       */
      this.getCallTitle = function() {
        return "Jitsi Call"; // TODO i18n
      };

      /**
       * Sample function used by JitsiIMRenderer to show how IM type renderer
       * can be initialized.
       */
      this.initSettings = function(mySettings) {
        // initialize IM type settings UI
      };
    };

    var provider = new JitsiProvider();

    // Add Jitsi provider into webConferencing object of global eXo namespace
    // (for non AMD uses)
    if (globalWebConferencing) {
      globalWebConferencing.jitsi = provider;
    } else {
      log.warn("eXo.webConferencing not defined");
    }

    log.trace("< Loaded at " + location.origin + location.pathname);
    return provider;
  } else {
    window.console &&
      window.console
        .log("WARN: webConferencing not given and eXo.webConferencing not defined. Jitsi provider registration skipped.");
  }
})($, webConferencing, callButton);