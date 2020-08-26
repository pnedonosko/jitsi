/**
 * Jitsi client integration in eXo Platform.
 */
(function($) {

	// ******** Constants ********

	// ******** Context ********
	var currentUser, currentSpace;

	// ******** Utils ********
	function pageBaseUrl(theLocation) {
		if (!theLocation) {
			theLocation = window.location;
		}

		var theHostName = theLocation.hostname;
		var theQueryString = theLocation.search;

		if (theLocation.port) {
			theHostName += ":" + theLocation.port;
		}

		return theLocation.protocol + "//" + theHostName;
	}

	/**
	 * Add style to current document (to the end of head).
	 */
	function loadStyle(cssUrl) {
		if (document.createStyleSheet) {
			document.createStyleSheet(cssUrl);
			// IE way
		} else {
			if ($("head").find("link[href='" + cssUrl + "']").size() == 0) {
				var headElems = document.getElementsByTagName("head");
				var style = document.createElement("link");
				style.type = "text/css";
				style.rel = "stylesheet";
				style.href = cssUrl;
				headElems[headElems.length - 1].appendChild(style);
				// $("head").append($("<link href='" + cssUrl + "' rel='stylesheet' type='text/css' />"));
			} // else, already added
		}
	}

	/** For debug logging. */
	function log(msg, e) {
		if ( typeof console != "undefined" && typeof console.log != "undefined") {
			console.log(msg);
			if (e && typeof e.stack != "undefined") {
				console.log(e.stack);
			}
		}
	}

	function getPortalUser() {
		return eXo.env.portal.userName;
	}

	function decodeString(str) {
		if (str) {
			try {
				str = str.replace(/\+/g, " ");
				str = decodeURIComponent(str);
				return str;
			} catch(e) {
				log("WARN: error decoding string " + str + ". " + e, e);
			}
		}
		return null;
	}

	function encodeString(str) {
		if (str) {
			try {
				str = encodeURIComponent(str);
				return str;
			} catch(e) {
				log("WARN: error decoding string " + str + ". " + e, e);
			}
		}
		return null;
	}

	// ******** UI utils **********

	/**
	 * Open pop-up.
	 */
	function popupWindow(url) {
		var w = 650;
		var h = 400;
		var left = (screen.width / 2) - (w / 2);
		var top = (screen.height / 2) - (h / 2);
		return window.open(url, 'contacts', 'width=' + w + ',height=' + h + ',top=' + top + ',left=' + left);
	}
	
	// UI messages 
	// Used to show immediate notifications in top right corner. 
	// This functionality requires pnotifyJQuery and jqueryui CSS.
	
	var NOTICE_WIDTH = "380px";

	/**
	 * Show notice to user. Options support "icon" class, "hide", "closer" and "nonblock" features.
	 */
	function showNotice(type, title, text, options) {
		var noticeOptions = {
			title : title,
			text : text,
			type : type,
			icon : "picon " + ( options ? options.icon : ""),
			hide : options && typeof options.hide != "undefined" ? options.hide : false,
			closer : options && typeof options.closer != "undefined" ? options.closer : true,
			sticker : false,
			opacity : .75,
			shadow : true,
			width : options && options.width ? options.width : NOTICE_WIDTH,
			nonblock : options && typeof options.nonblock != "undefined" ? options.nonblock : false,
			nonblock_opacity : .25,
			after_init : function(pnotify) {
				if (options && typeof options.onInit == "function") {
					options.onInit(pnotify);
				}
			}
		};
		return $.pnotify(noticeOptions);
	}

	/**
	 * Show error notice to user. Error will stick until an user close it.
	 */
	function showError(title, text, onInit) {
		return UI.showNotice("error", title, text, {
			icon : "picon-dialog-error",
			hide : false,
			delay : 0,
			onInit : onInit
		});
	}

	/**
	 * Show info notice to user. Info will be shown for 8sec and hidden then.
	 */
	function showInfo(title, text, onInit) {
		return UI.showNotice("info", title, text, {
			hide : true,
			delay : 8000,
			icon : "picon-dialog-information",
			onInit : onInit
		});
	}

	/**
	 * Show warning notice to user. Info will be shown for 8sec and hidden then.
	 */
	function showWarn(title, text, onInit) {
		return UI.showNotice("exclamation", title, text, {
			hide : false,
			delay : 30000,
			icon : "picon-dialog-warning",
			onInit : onInit
		});
	}

	// ******** REST services ********
	var prefixUrl = pageBaseUrl(location);

	function initRequest(request) {
		var process = $.Deferred();

		// stuff in textStatus is less interesting: it can be "timeout",
		// "error", "abort", and "parsererror",
		// "success" or smth like that
		request.fail(function(jqXHR, textStatus, err) {
			if (jqXHR.status != 309) {
				// check if response isn't JSON
				var data;
				try {
					data = $.parseJSON(jqXHR.responseText);
					if ( typeof data == "string") {
						// not JSON
						data = jqXHR.responseText;
					}
				} catch(e) {
					// not JSON
					data = jqXHR.responseText;
				}
				// in err - textual portion of the HTTP status, such as "Not
				// Found" or "Internal Server Error."
				process.reject(data, jqXHR.status, err, jqXHR);
			}
		});
		// hacking jQuery for statusCode handling
		var jQueryStatusCode = request.statusCode;
		request.statusCode = function(map) {
			var user502 = map[502];
			if (!user502) {
				map[502] = function() {
					// treat 502 as request error also
					process.fail("Bad gateway", 502, "error");
				};
			}
			return jQueryStatusCode(map);
		};

		request.done(function(data, textStatus, jqXHR) {
			process.resolve(data, jqXHR.status, textStatus, jqXHR);
		});

		request.always(function(data, textStatus, errorThrown) {
			var status;
			if (data && data.status) {
				status = data.status;
			} else if (errorThrown && errorThrown.status) {
				status = errorThrown.status;
			} else {
				status = 200;
				// what else we could to do
			}
			process.always(status, textStatus);
		});

		// custom Promise target to provide an access to jqXHR object
		var processTarget = {
			request : request
		};
		return process.promise(processTarget);
	}

	function postUserMeet(room, name, users) {
		var request = $.ajax({
			async : false,
			type : "POST",
			url : prefixUrl + "/portal/rest/jitsi/meet/user/" + room,
			dataType : "json",
			data : {
				name : name,
				users : users
			}
		});

		return initRequest(request);
	}

	function getUserMeet(room, users) {
		var request = $.ajax({
			async : false,
			type : "GET",
			url : prefixUrl + "/portal/rest/jitsi/meet/user/" + room + ( users ? "?users=" + users : "")
		});

		return initRequest(request);
	}

	function deleteUserMeet(room, users) {
		var request = $.ajax({
			async : false,
			type : "DELETE",
			url : prefixUrl + "/portal/rest/jitsi/meet/user/" + room + ( users ? "?users=" + users : "")
		});

		return initRequest(request);
	}

	function postGroupMeet(room, name, space) {
		var request = $.ajax({
			async : false,
			type : "POST",
			url : prefixUrl + "/portal/rest/jitsi/meet/group/" + room,
			dataType : "json",
			data : {
				name : name,
				space : space
			}
		});

		return initRequest(request);
	}

	function getGroupMeet(room, space) {
		var request = $.ajax({
			async : false,
			type : "GET",
			url : prefixUrl + "/portal/rest/jitsi/meet/group/" + room + ( space ? "?space=" + space : "")
		});

		return initRequest(request);
	}

	function deleteGroupMeet(room, space) {
		var request = $.ajax({
			async : false,
			type : "DELETE",
			url : prefixUrl + "/portal/rest/jitsi/meet/group/" + room + ( space ? "?space=" + space : "")
		});

		return initRequest(request);
	}

	function serviceGet(url, data) {
		var request = $.ajax({
			async : true,
			type : "GET",
			url : url,
			dataType : "json",
			data : data ? data : {}
		});
		return initRequest(request);
	}
	
	/**
	 * Jitsi Meet core class.
	 */
	function Meet() {

		var meets = {};

		var spaceUpdater;

		/**
		 * TODO a placeholder for Chat initialization
		 */
		var initChat = function() {
			if (chatApplication) {
				log("Init chat for " + chatApplication.username);

				var $chat = $("#chat-application");
				if ($chat.size() > 0) {
					var $room = $chat.find("a.room-detail-fullname");
					if ($room.size() == 0) {
						// TODO
					}
				}
			}
		};

		var initUserPopups = function(compId) {
			var $tiptip = $("#tiptip_content");
			// if not in user profile wait for UIUserProfilePopup script load
			if (window.location.href.indexOf("/portal/intranet/profile") < 0) {
				if ($tiptip.size() == 0 || $tiptip.hasClass("DisabledEvent")) {
					setTimeout($.proxy(initUserPopups, this), 250, compId);
					return;
				}
			}

			function addButton($userAction, userName, userTitle, pullRight) {
				if ($userAction.size() > 0 && $userAction.find("a.meetStartAction").size() === 0) {
					var room = "eXoMeet" + currentUser.name + userName;
					var meetName = "Meeting " + currentUser.title + " with " + userTitle;
					var users = [currentUser.name, userName].join();
					var meet = meets[room];
					if (!meet) {
						// check if meet not already started (by another user)
						var get = getUserMeet(room, users);
						get.done(function(existingMeet) {
							meet = existingMeet;
						});
						get.fail(function(e, status) {
							if (status == 403) {
								UI.showWarn("You cannot join meeting", e.message);
							} else if (status != 404) {
								log("ERROR reading meet: " + JSON.stringify(e));
							}
							meet = null;
						});
					}

					// had classes uiIconWeemoVideoCalls uiIconWeemoLightGray
					var meetLabel = "<i class='uiIconVideoCall'></i>Meet";
					var meetButton = "<a type='button' class='btn meetStartAction";
					if (pullRight) {
						meetButton += " pull-right";
					}
					if (meet) {
						if (meet.window && !meet.window.closed) {
							meetButton += " meetJoined";
						} else {
							meetButton += " meetReady";
						}
					}
					meetButton += "' title='Start meeting with " + userTitle + "' target='_blank' style='margin-left:5px;'";
					if (meet) {
						meetButton += " href='" + meet.url + "'";
					}
					meetButton += ">";
					meetButton += meetLabel;
					meetButton += "</a>";
					if (pullRight) {
						$userAction.prepend(meetButton);
					} else {
						$userAction.append(meetButton);
					}
					var $button = $userAction.find("a.meetStartAction");
					$button.click(function(e) {
						e.preventDefault();
						if (meet && meet.window && !meet.window.closed) {
							meet.window.focus();
						} else {
							var cursorCss = $button.css("cursor");
							$button.css("cursor", "wait");
							try {
								function showMeet(meet) {
									meet.window = window.open(meet.url, "_blank");
									$(meet.window.document).ready(function() {
										meets[room] = meet;
										$button.attr("title", "Open meeting with " + userTitle);
										$button.attr("data-meet-invitee", userName);
										setTimeout(function() {
											if (meet.window.api) {
												$button.removeClass("meetReady");
												$button.addClass("meetJoined");
												var api = meet.window.api;
												api.executeCommands({
													"displayName" : [currentUser.title]
												});
												api.addEventListener("participantJoined", function(event) {
													log("participantJoined: " + JSON.stringify(event));
												});
												api.addEventListener("participantLeft", function(event) {
													log("participantLeft: " + JSON.stringify(event));
												});
												//api.initListeners(); // TODO do we need this?
											}
										}, 5000);
									});
									$(meet.window).on("beforeunload unload", function() {
										if (meet.window && meet.window.api) {
											meet.window.api.dispose();
											meet.window = null;
											// call REST to end the meet session for this user
											var del = deleteUserMeet(room, users);
											del.done(function(existingMeet, status) {
												$button.removeClass("meetJoined");
												if (status == 204) {
													delete meets[room];
													meet = null;
													$button.removeClass("meetReady");
												}
											});
											del.fail(function(e, status) {
												log("ERROR (" + status + ") deleting meet: " + JSON.stringify(e));
											});
										}
									});
								}

								function openMeet() {
									var create = postUserMeet(room, meetName, users);
									create.done(function(meet) {
										showMeet(meet);
									});
									create.fail(function(e, status) {
										if (status == 403) {
											UI.showWarn("You cannot join meeting", e.message);
										}
										log("ERROR (" + status + ") opening meet: " + JSON.stringify(e));
									});
								}

								openMeet();
							} catch(e) {
								log("Error creating meet " + meetName + " " + e, e);
								if (meet && meet.window) {
									meet.window.close();
									meet.window = null;
								}
							} finally {
								$button.css("cursor", cursorCss);
							}
						}
					});
				}
			}

			function extractUserName($userLink) {
				var userName = $userLink.attr("href");
				return userName.substring(userName.lastIndexOf("/") + 1, userName.length);
			}

			// user popovers
			// XXX hardcoded for peopleSuggest as no way found to add Lifecycle to its portlet (juzu)
			$("#" + compId + ", #peopleSuggest").find('a:[href*="/profile/"]').each(function() {
				// attach action to
				$(this).mouseenter(function() {
					// need wait for popover initialization
					setTimeout(function() {
						// Find user's first name for a tip
						var $td = $tiptip.children("#tipName").children("tbody").children("tr").children("td");
						if ($td.size() > 1) {
							var $userLink = $("a", $td.get(1));
							var userTitle = $userLink.text();
							var userName = extractUserName($userLink);
							var $userAction = $tiptip.find(".uiAction");
							addButton($userAction, userName, userTitle);
						}
					}, 600);
				});
			});

			// user panel in connections (all, personal and in space)
			$("#" + compId).find(".spaceBox").each(function(i, elem) {
				var $userLink = $(elem).find(".spaceTitle a:first");
				if ($userLink.size() > 0) {
					var userTitle = $userLink.text();
					var userName = extractUserName($userLink);
					var $userAction = $(elem).find(".connectionBtn");
					addButton($userAction, userName, userTitle, true);
				}
			});

			// single user profile
			$("#" + compId).find("#UIProfile").each(function(i, elem) {
				var $userName = $(elem).find("#UIBasicInfoSection label[for='username']");
				var userName = $.trim($userName.siblings().text());
				var $firstName = $(elem).find("#UIBasicInfoSection label[for='firstName']");
				var $lastName = $(elem).find("#UIBasicInfoSection label[for='lastName']");
				var userTitle = $.trim($firstName.siblings().text()) + " " + $.trim($lastName.siblings().text());
				var $userAction = $(elem).find("#UIHeaderSection h3");
				addButton($userAction, userName, userTitle);
			});
		};

		var initSpace = function(compId) {
			if (currentUser && currentSpace) {
				var $navigationPortlet = $("#UIBreadCrumbsNavigationPortlet");
				if ($navigationPortlet.size() == 0) {
					setTimeout($.proxy(initSpace, this), 250, compId);
					return;
				}

				var $breadcumbEntry = $navigationPortlet.find(".breadcumbEntry");
				if ($breadcumbEntry.size() > 0 && $breadcumbEntry.find("a.meetStartAction").size() === 0) {
					if (spaceUpdater) {
						spaceUpdater.clearInterval();
					}
					// had classes uiIconWeemoVideoCalls uiIconWeemoLightGray
					var meetLabel = "<i class='uiIconVideoCall'></i>Meet";
					var meetButton = "<a class='actionIcon meetStartAction spaceMeet";
					meetButton += "' title='Meeting of " + currentSpace.name + "' target='_blank' style='margin-left:5px;'";
					meetButton += ">";
					meetButton += meetLabel;
					meetButton += "</a>";
					$breadcumbEntry.append(meetButton);
					var $button = $breadcumbEntry.find("a.meetStartAction");
					$button.click(function(e) {
						// meet click handler
						e.preventDefault();
						var meetName = "Meeting of " + currentSpace.name;
						var meet = meets[currentSpace.roomName];
						if (meet && meet.window && !meet.window.closed) {
							meet.window.focus();
						} else {
							var cursorCss = $button.css("cursor");
							$button.css("cursor", "wait");
							try {
								function showMeet(meet) {
									meet.window = window.open(meet.url, "_blank");
									$(meet.window).load(function() {
										meets[currentSpace.roomName] = meet;
										$button.attr("title", "Open meeting in " + currentSpace.name);
										setTimeout(function() {
											if (meet.window.api) {
												$button.removeClass("meetReady");
												$button.addClass("meetJoined");
												var api = meet.window.api;
												api.executeCommands({
													"displayName" : [currentUser.title]
												});
												api.addEventListener("participantJoined", function(event) {
													log("participantJoined: " + JSON.stringify(event));
												});
												api.addEventListener("participantLeft", function(event) {
													log("participantLeft: " + JSON.stringify(event));
												});
												api.addEventListener("displayNameChanged", function(event) {
													log("displayNameChanged: " + JSON.stringify(event));
													if (!meet.userJid) {
														if (event.displayname == currentUser.title) {
															// remember jid for later changes of display name
															meet.userJid = event.jid;
														}
													} else if (event.jid == meet.userJid) {
														if (event.displayname != currentUser.title) {
															// fix displayname back to eXo user
															api.executeCommands({
																"displayName" : [currentUser.title]
															});
														}
													}
												});
												log("Meet shown: " + JSON.stringify(api));
											}
										}, 5000);
									});
									$(meet.window).on("beforeunload unload", function() {
										if (meet.window && meet.window.api) {
											meet.window.api.dispose();
											meet.window = null;
											// call REST to end the meet session for this user
											var del = deleteGroupMeet(currentSpace.roomName, currentSpace.name);
											del.done(function(existingMeet, status) {
												$button.removeClass("meetJoined");
												if (status == 204) {
													delete meets[currentSpace.roomName];
													$button.removeClass("meetReady");
												}
											});
											del.fail(function(e, status) {
												log("ERROR (" + status + ") deleting meet: " + JSON.stringify(e));
											});
										}
									});
								}

								function openMeet() {
									var create = postGroupMeet(currentSpace.roomName, meetName, currentSpace.name);
									create.done(function(meet) {
										showMeet(meet);
									});
									create.fail(function(e, status) {
										if (status == 403) {
											UI.showWarn("You cannot join meeting", e.message);
										}
										log("ERROR (" + status + ") opening meet: " + JSON.stringify(e));
									});
								}

								openMeet();
							} catch(e) {
								log("Error creating meet " + meetName + " " + e, e);
								if (meet && meet.window) {
									meet.window.close();
									meet.window = null;
								}
							} finally {
								$button.css("cursor", cursorCss);
							}
						}
					});

					function checkMeet() {
						// check if meet not started by others
						var get = getGroupMeet(currentSpace.roomName, currentSpace.name);
						get.done(function(existingMeet) {
							if (!$button.hasClass("meetReady") && !$button.hasClass("meetJoined")) {
								$button.addClass("meetReady");
								$button.attr("title", "Open meeting in " + currentSpace.name);
							}
						});
						get.fail(function(e, status) {
							if (status == 404) {
								$button.removeClass("meetReady");
								$button.removeClass("meetJoined");
							}
						});
					}

					checkMeet();
					// we also want stay updated about space meet
					spaceUpdater = setInterval(function() {
						checkMeet();
					}, 10000);
				}
			}
		};

		this.initButtons = function(compId) {
			log("initButtons " + compId);
			if (!compId) {
				// by default we work with whole portal page
				compId = "UIPortalApplication";
			}

			initUserPopups(compId);
			initSpace(compId);
		};

		/**
		 * Initialize context
		 */
		this.init = function(userName, userTitle, spaceName, spaceRoomName) {
			// current eXo user
			currentUser = {
				name : userName,
				title : userTitle
			};
			currentSpace = {
				name : spaceName,
				roomName : spaceRoomName
			};
		};
	}

	var meet = new Meet();

	// Load dependencies only in top window (not in iframes of gadgets).
	if (window == top) {
		$(function() {
			try {
				// it's common styles for Jitsi client
				loadStyle("/jitsi-meet/skin/jquery-ui.css");
				loadStyle("/jitsi-meet/skin/jquery.pnotify.default.css");
				loadStyle("/jitsi-meet/skin/jquery.pnotify.default.icons.css");
				loadStyle("/jitsi-meet/skin/jitsi-meet.css");
				
				// configure Pnotify
				$.pnotify.defaults.styling = "jqueryui";
				// use jQuery UI css
				$.pnotify.defaults.history = false;
				// no history roller in the right corner
			} catch(e) {
				log("Error configuring Jitsi style.", e);
			}
		});
	}

	return meet;
})($);
