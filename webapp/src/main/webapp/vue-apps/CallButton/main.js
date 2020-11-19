import JitsiMeetButton from "./components/JitsiMeetButton.vue";
import CallPopup from "./components/CallPopup.vue";

Vue.use(Vuetify);
Vue.component("jitsi-meet-button", JitsiMeetButton);
const vuetify = new Vuetify({
  dark: true,
  iconfont: "",
});

// getting language of user
const lang =
  (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || "en";
const localePortlet = "locale.jitsi";
const resourceBundleName = "Jitsi";
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${localePortlet}.${resourceBundleName}-${lang}.json`;

export function init(callSettings) {
  // getting locale ressources
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    // init Vue app when locale ressources are ready
    return new Vue({
      render: (h) =>
        h(JitsiMeetButton, {
          props: {
            callSettings: callSettings,
            i18n: i18n,
            language: lang,
            resourceBundleName: resourceBundleName,
          },
        }),
      i18n,
      vuetify,
    });
  });
}

export function initCallPopup(
  callId,
  callState,
  callerId,
  callerLink,
  callerAvatar,
  callerMessage,
  playRingtone,
) {

  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    const container = document.createElement("div");
    container.setAttribute("id", "call-popup");
    let onAccepted;
    let onRejected;
    const comp = new Vue({
      el: "#call-popup",
      components: {
        CallPopup,
      },
      data() {
        return {
          isDialogVisible: true,
          callerId: callerId,
          avatar: callerAvatar,
          callerMessage: callerMessage,
          playRingtone: playRingtone,
        };
      },
      i18n,
      vuetify,
      render: function(h) {
        const thevue = this;
        return h(CallPopup, {
          props: {
            isDialogVisible: this.isDialogVisible,
            caller: this.callerId,
            avatar: this.avatar,
            callerMessage: this.callerMessage,
            playRingtone: this.playRingtone,
          },
          on: {
            accepted: function() {
              if (onAccepted) {
                onAccepted();
                thevue.isDialogVisible = false;
                thevue.$destroy();
              }
            },
            rejected: function(isClosed) {
              if (onRejected) {
                onRejected(isClosed);
                thevue.isDialogVisible = false;
                thevue.$destroy();
              }
            },
          },
        });
      },
    });
    return {
      callId,
      callState,
      callerId,
      close: function() {
        comp.isDialogVisible = false;
        comp.$destroy();
      },
      onAccepted: function(callback) {
        onAccepted = callback;
      },
      onRejected: function(callback) {
        onRejected = callback;
      },
    };
  });
}