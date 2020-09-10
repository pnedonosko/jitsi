require(["jquery", "SHARED/webConferencing" ], function(jquery, webconferencing) {

  var MeetApp = function() {
    var getUrlParameter = function(sParam) {
      var sPageURL = window.location.search.substring(1), sURLVariables = sPageURL.split('&'), sParameterName, i;

      for (i = 0; i < sURLVariables.length; i++) {
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam) {
          return sParameterName[1] === undefined ? true : decodeURIComponent(sParameterName[1]);
        }
      }
    };

    var initJitsiIframe = function(username) {
      const domain = "meet.jit.si";
      const options = {
        roomName : "JitsiMeetAPIExample",
        width : 700,
        height : 700,
        parentNode : document.querySelector("#meet"),
        userInfo: {
          email: 'testEmail@jitsiexamplemail.com',
          displayName: username
      }
      };
      new JitsiMeetExternalAPI(domain, options);
    };

    this.init = function() {
      var inviteId = getUrlParameter("inviteId");
      if (inviteId) {
        var url = "/jitsi/api/invite/" + inviteId;
        // Todo: use promises
        $.get(url, function(data) {
          console.log(JSON.stringify(data));
          let trimmedUrl = window.location.href.substring(0, window.location.href.indexOf("?"));
          window.history.pushState({}, "", trimmedUrl);
          initJitsiIframe(data.username);
        });
      } else {
        var url = "/jitsi/portal/rest/jitsi/context";
        $.get(url, function(data) {
          console.log(JSON.stringify(data));
          webconferencing.init(data.userInfo, data.context);
          initJitsiIframe(data.username);
        });
      }
    };

  };

  var meetApp = new MeetApp();
  meetApp.init();
});