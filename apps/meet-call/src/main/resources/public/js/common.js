var xmlHttp = new XMLHttpRequest();
xmlHttp.open("GET", "../portal/rest/jitsi/resources/version", false); // false for synchronous request
xmlHttp.send(null);
var resorcesVersion = xmlHttp.responseText;

/* RequireJS config */
var require = {
  baseUrl : "/portal/scripts/" + resorcesVersion,
  waitSeconds : 60,
  paths : {
    "jquery" : "https://ajax.googleapis.com/ajax/libs/jquery/1.7/jquery",
    "main" : "../../../jitsi/js/main"
  }
};

if (!window.eXo) {
  window.eXo = {
    core : {},
    define : {},
    env : {
      portal : {},
      client : {},
      server : {}
    },

    portal : {},

    webui : {},

    gadget : {},

    session : {},

    i18n : {}
  };
}
