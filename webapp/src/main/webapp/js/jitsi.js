(function() {

  function Jitsi() {

    this.init = function() {
      const domain = 'jitsi-dev.francecentral.cloudapp.azure.com';
      const options = {
        roomName : 'JitsiMeetAPIExample',
        width : 700,
        height : 700,
        parentNode : document.querySelector('#container'),
        interfaceConfigOverwrite : {
          TOOLBAR_BUTTONS : [ 'microphone', 'camera', 'closedcaptions', 'desktop', 'fullscreen', 'fodeviceselection', 'hangup',
              'profile', 'chat', 'recording', 'livestreaming', 'etherpad', 'sharedvideo', 'settings', 'raisehand',
              'videoquality', 'filmstrip', 'invite', 'feedback', 'stats', 'shortcuts', 'tileview', 'videobackgroundblur',
              'download', 'help', 'mute-everyone', 'security' ]
        },
        jwt : 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb250ZXh0Ijp7InVzZXIiOnsiYXZhdGFyIjoiaHR0cHM6L2dyYXZhdGFyLmNvbS9hdmF0YXIvYWJjMTIzIiwibmFtZSI6IkpvaG4gRG9lIiwiZW1haWwiOiJqZG9lQGV4YW1wbGUuY29tIiwiaWQiOiJhYmNkOmExYjJjMy1kNGU1ZjYtMGFiYzEtMjNkZS1hYmNkZWYwMWZlZGNiYSJ9fSwiYXVkIjoibXlfaml0c2lfYXBwX2lkIiwiaXNzIjoibXlfaml0c2lfYXBwX2lkIiwic3ViIjoiaml0c2ktZGV2LmZyYW5jZWNlbnRyYWwuY2xvdWRhcHAuYXp1cmUuY29tIiwicm9vbSI6IioifQ.96DlXN1pdu6vhaZYBCmpp3dD7fBNa_o07MaD3u1XyGE'
      };
      const api = new JitsiMeetExternalAPI(domain, options);
    };
  }

  var jitsi = new Jitsi();

  return jitsi;

})();