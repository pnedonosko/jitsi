import JitsiMeetButton from "./components/JitsiMeetButton.vue";
import NotificationPopUp from "./components/NotificationPopUp.vue";

Vue.use(Vuetify);
Vue.component("jitsi-meet-button", JitsiMeetButton);
const vuetify = new Vuetify({
  dark: true,
  iconfont: ""
});

// getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || "en";
const localePortlet = "locale.jitsi";
const resourceBundleName = "Jitsi";
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${localePortlet}.${resourceBundleName}-${lang}.json`;

export function init(callSettings) {
    // getting locale ressources
    return exoi18n.loadLanguageAsync(lang, url).then(i18n => {
      // init Vue app when locale ressources are ready
      return new Vue({
        render : h =>
          h(JitsiMeetButton, {
            props : {
              callSettings: callSettings,
              i18n : i18n,
              language : lang,
              resourceBundleName : resourceBundleName
            }
          }),
        i18n,
        vuetify
      });
    });
}

export function initNotificationPopup(target) {
  const comp = new Vue({
    el: target,
    components: {
      NotificationPopUp,
    },
    data() {
      return {
        callInfo: {
          dialog: false,
          callerId: "",
          avatar: "",
          callbackFunc: null
        }
      };
    },
    vuetify,
    render: function(h) {
      return h(NotificationPopUp, {
        props: {
          dialog: this.callInfo.dialog,
          caller: this.callInfo.callerId,
          avatar: this.callInfo.avatar,
          callbackFunc: this.callInfo.callbackFunc
          // language: lang,
          
        }
      });
    }
  })
  return {
    comp: comp,
    show: function(callerId, callerLink, callerAvatar, callerMessage, playRingtone, callbackFunc) {
      // console.log(callerId, callerLink, callerAvatar, callerMessage, playRingtone, callbackFunc, "ARGSS")
      comp.callInfo.dialog = true;
      comp.callInfo.callerId = callerId;
      comp.callInfo.avatar = callerAvatar;
      comp.callInfo.callbackFunc = callbackFunc
    },
    close: function() {
      comp.callInfo.dialog = false;
      comp.callInfo.callerId = "";
      comp.callInfo.avatar = "";
      comp.callInfo.callbackFunc = null;
    }
  }
}