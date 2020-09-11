require([ "SHARED/jquery", "SHARED/webConferencing" ], function(jquery, webconferencing) {

  var MeetApp = function() {

    var currentUserInfo;

    var getUrlParameter = function(sParam) {
      var sPageURL = window.location.search.substring(1), sURLVariables = sPageURL.split('&'), sParameterName, i;

      for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
          return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
      }
    };

    var getExoUserInfo = function() {
      return $.get({
        type : "GET",
        url : "/jitsi/portal/rest/jitsi/userinfo",
      });
    };

    var getGuestUserInfo = function(inviteId) {
      return $.get({
        type : "GET",
        url : "/jitsi/api/userinfo/" + inviteId,
      });
    };

    var getContextInfo = function(userId) {
      return $.get({
        type : "GET",
        url : "/jitsi/portal/rest/jitsi/context/" + userId,
      });
    };

    var initCall = function(userInfo, contextInfo) {
      webconferencing.init(userInfo, contextInfo);
      webconferencing.getProvider("jitsi").done(function(provider){
        console.log("JITSI PROVIDER: " + JSON.stringify(provider));
      }).fail(function(err){
        console.log("ERROR: " + err);
      });
      var fullname = userInfo.firstName + " " + userInfo.lastName;
      const domain = "meet.jit.si";
      const options = {
        roomName : "Jitsi Meet Example",
        width : 1000,
        height : 550,
        parentNode : document.querySelector("#meet"),
        userInfo : {
          displayName : fullname
        }
      };
      new JitsiMeetExternalAPI(domain, options);
    };

    /**
     * Inits current user and context
     */
    this.init = function() {
      // TODO: get jitsi settings: url of app, jwt..
      var inviteId = getUrlParameter("inviteId");
      if (inviteId) {
        getGuestUserInfo(inviteId).then(function(userInfo) {
          currentUserInfo = userInfo;
          getContextInfo(userInfo.id).then(function(contextInfo) {
            let trimmedUrl = window.location.href.substring(0, window.location.href.indexOf("?"));
            window.history.pushState({}, "", trimmedUrl);
            initCall(userInfo, contextInfo);
          });
        }).catch(function(err) {
          console.log("Cannot get guest user info: " + JSON.stringify(err));
          // TODO: show user-friendly error?
        });
      } else {
        getExoUserInfo().then(function(userInfo){
          currentUserInfo = userInfo;
          getContextInfo(userInfo.id).then(function(contextInfo) {
            initCall(userInfo, contextInfo);
          });
        }).catch(function(err) {
          console.log("Cannot get exo user info: " + JSON.stringify(err));
          // TODO: redirect to login page if the satus code is 401 or 403?
        });
      }
    };

  };

  var meetApp = new MeetApp();
  meetApp.init();
});