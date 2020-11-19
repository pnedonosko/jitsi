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
const callsStates = new Map();

export function init(settings) {
  // getting locale ressources
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    // init Vue app when locale ressources are ready
    return new Vue({
      data() {
        return {
          callSettings: settings
        };
      },
      created() {
        if(!callsStates.has(this.callSettings.callId)) {
          callsStates.set(this.callSettings.callId, new Map());
        }
        // different buttons for the same call states
        const statesForTheSameCall = callsStates.get(this.callSettings.callId);
        statesForTheSameCall.set(this.callSettings.context.parentClasses, {
          setCallState: this.setCallState,
          getCallState: this.getCallState
        });
      },
      methods: {
        setCallState: function(callState) {
          this.$set(this.callSettings, "callState", callState);
        },
        getCallState: function() {
          return this.callSettings.callState;
        }
      },
      render: (h) =>
        h(JitsiMeetButton, {
          props: {
            callSettings: settings,
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

export function updateCallState(callId, state) {
  callsStates.get(callId).forEach((stateHandler) => {
    stateHandler.setCallState(state);
  });
}

export function initCallPopup(
    callId,
    callState,
    callerId,
    callerLink,
    callerAvatar,
    callerMessage,
    playRingtone) {
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    const container = document.createElement("div");
    container.setAttribute("id", "call-popup"); // TODO why we need an ID unique per page?
    let onAccepted;
    let onRejected;
    const comp = new Vue({
      el: container,
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
                // TODO copypasted in thee places, why not a single function?
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