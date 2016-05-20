/**
 * Jitsi application in eXo Platform.
 */
require(["SHARED/jquery", "SHARED/jitsiMeet", "SHARED/jqueryUI", "SHARED/jqueryPnotify"], function($, jitsiMeet) {

	var getIEVersion = function()
	// Returns the version of Windows Internet Explorer or a -1
	// (indicating the use of another browser).
	{
		var rv = -1;
		// Return value assumes failure.
		if (navigator.appName == "Microsoft Internet Explorer") {
			var ua = navigator.userAgent;
			var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
			if (re.exec(ua) != null)
				rv = parseFloat(RegExp.$1);
		}
		return rv;
	};

	$(function() {
		var $videocall = $("#videocall-status");
		if ($videocall.size() > 0) {
			var username = $videocall.data("username");
			var userfullname = $videocall.data("userfullname");
			var spacename = $videocall.data("spacename");
			var spaceroomname = $videocall.data("spaceroomname");
			// init meet on page ready
			jitsiMeet.init(username, userfullname, spacename, spaceroomname);
			jitsiMeet.initButtons();

			// and later on DOM changes (when portlets will load by Ajax)
			var iev = getIEVersion();
			if (iev == -1 || iev >= 11) {
				// TODO as for IE<11 need use polyfills http://webcomponents.org/polyfills/
				setTimeout(function() {
					var portal = document.getElementById("UIPortalApplication");
					var MutationObserver = window.MutationObserver || window.WebKitMutationObserver || window.MozMutationObserver;
					var observer = new MutationObserver(function(mutations) {
						// mutations.forEach(function(mutation) {
						// console.log(mutation.type);
						// });
						// TODO this will be fired twice on each update, try reduce or use another approach (call from WebUI server-side)
						jitsiMeet.initButtons();
					});
					observer.observe(portal, {
						subtree : true,
						childList : true,
						attributes : false,
						characterData : false
					});
				}, 2500);
			}
		}
	});

});

