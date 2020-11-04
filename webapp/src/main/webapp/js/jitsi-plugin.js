const webconferencingJitsiExts = [{
    target: "webConferencing",
    type: "webconferencing",
    // configuration defined here is used in portlet-clouddrives/src/main/webapp/vue-app/components/connectJitsi.vue with
    // ecm-wcm-extension/src/main/webapp/attachments-selector/components/ExoAttachments.vue and connects them
    // key should be unique and used in parent component as a ref to connectJitsi component
    key: "jitsi",
    rank: 20,
    // iconName is a name of the icon which is displayed on action button with 'onExecute' action
    // iconName should be one of the names, supported by vuetify 'v-icon' component (https://vuetifyjs.com/en/components/icons/)
    // if it should be custom icon that isn't supported by vuetify iconClass instead of iconName should be used
    iconName: "uiIconSetting", // TODO setting??
    // appClass is a class of container which cosist of action button and Jitsi component
    appClass: "jitsiMeetButton",
    // component has property which will be passed to dynamic component inside parent
    // (https://vuejs.org/v2/guide/components.html#Dynamic-Components)
    component: {
      // name should be the name registered via Vue.component (https://vuejs.org/v2/guide/components-registration.html#Component-Names)
      name: "jitsi-meet-button",
      // events are passed to custom DynamicEvents directive (https://vuejs.org/v2/guide/custom-directive.html)
      // event is name of the event that Jitsi component emits
      // listener is name of the method that ExoAttachments has
      // all params added in 'emit()' inside Jitsi component will be available inside ExoAttachments methods
      events: [
      ],
    },
    // enabled just show that this extension is enabled, if enabled: false CloudDriveComponent will not appear on page
    enabled: true
  }];
require(["SHARED/extensionRegistry", "SHARED/webConferencing"], function(extensionRegistry, webConferencing) {
  // webConferencing.init(); // TODO why we care about WebConf init from one of its providers??
  const log = webConferencing.getLog("jitsi-plugin");
  for (const extension of webconferencingJitsiExts) {
    extensionRegistry.registerExtension(extension.target, extension.type, extension);
    log.trace(`Register extension type of ${extension.type} for ${extension.target}`);
  }
});
