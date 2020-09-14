require([ "SHARED/jquery", "SHARED/webConferencing", "SHARED/webConferencing_jitsi"], function(jquery, webconferencing, provider) {

  var MeetApp = function() {

    var currentUserInfo;
    var callId;

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
    
    var getSettings = function() {
      return $.get({
        type : "GET",
        url : "/jitsi/portal/rest/jitsi/settings",
      });
    };

    var beforeunloadListener = function() {
      webConferencing.deleteCall(callId).done(function() {
        log.info("Call deleted: " + callId);
        process.resolve();
      })
    };
    
    // Not inited callId
    var initCall = function(userInfo, contextInfo, settings) {
      webconferencing.init(userInfo, contextInfo);
      provider.configure(settings);
      webconferencing.addProvider(provider);
      webconferencing.update();
      var fullname = userInfo.firstName + " " + userInfo.lastName;
      const domain = "dev03.exoplatform.org:8443";
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
    this.init = function(call) {
      if(call) {
        callId = call.id;
      }
      // TODO: get jitsi settings: url of app, jwt..
      var inviteId = getUrlParameter("inviteId");
      if (inviteId) {
        getGuestUserInfo(inviteId).then(function(userInfo) {
          currentUserInfo = userInfo;
          getContextInfo(userInfo.id).then(function(contextInfo) {
            let trimmedUrl = window.location.href.substring(0, window.location.href.indexOf("?"));
            window.history.pushState({}, "", trimmedUrl);
            getSettings().then(function(settings){
              initCall(userInfo, contextInfo, settings);
            });
          });
        }).catch(function(err) {
          console.log("Cannot get guest user info: " + JSON.stringify(err));
          // TODO: show user-friendly error?
        });
      } else {
        getExoUserInfo().then(function(userInfo){
          currentUserInfo = userInfo;
          getContextInfo(userInfo.id).then(function(contextInfo) {
            getSettings().then(function(settings){
              initCall(userInfo, contextInfo, settings);
            });
          });
        }).catch(function(err) {
          console.log("Cannot get exo user info: " + JSON.stringify(err));
          // TODO: redirect to login page if the satus code is 401 or 403?
        });
      }
      
      window.beforeunloadListener = beforeunloadListener;
      
      
    };

  };

  var meetApp = new MeetApp();
  window.startCall = function(call, isNew){
    meetApp.init(call);
    var promise = $.Deferred();
    promise.resolve("started");
    return promise;
  };
});