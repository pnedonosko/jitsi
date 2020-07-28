function init(domain, room, name, email) {
  var height = window.innerHeight;
  const options = {
    roomName : room,
    height : height,
    parentNode : document.querySelector('#container'),
    interfaceConfigOverwrite : {
      TOOLBAR_BUTTONS : [ 'microphone', 'camera', 'closedcaptions', 'desktop', 'embedmeeting', 'fullscreen', 'fodeviceselection',
          'hangup', 'profile', 'recording', 'livestreaming', 'etherpad', 'sharedvideo', 'settings', 'videoquality', 'filmstrip',
          'invite', 'feedback', 'stats', 'shortcuts', 'tileview', 'videobackgroundblur', 'download', 'help', 'mute-everyone',
          'security', 'e2ee' ]
    },
    userInfo : {
      email : email,
      displayName : name
    }
  };
  const api = new JitsiMeetExternalAPI(domain, options);
};
