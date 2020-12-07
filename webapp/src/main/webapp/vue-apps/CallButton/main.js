import JitsiMeetButton from "./components/JitsiMeetButton.vue";
import CallPopup from "./components/CallPopup.vue";
// import CallPopupDrawer from "./components/CallPopupDrawer.vue";

Vue.use(Vuetify);
Vue.component("jitsi-meet-button", JitsiMeetButton);
// Vue.component("CallPopup", CallPopup);
const vuetify = new Vuetify({
  dark: true,
  iconfont: "",
});

// getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || "en";
const localePortlet = "locale.jitsi";
const resourceBundleName = "Jitsi";
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${localePortlet}.${resourceBundleName}-${lang}.json`;
const callStates = new Map();
const callPopups = new Map();

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
        if(!callStates.has(this.callSettings.callId)) {
          callStates.set(this.callSettings.callId, new Map());
        }
        // different buttons for the same call states
        const statesForTheSameCall = callStates.get(this.callSettings.callId);
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
          }
        }),
      i18n,
      vuetify,
    });
  });
}

export function updateCallState(callId, state) {
  const buttonStates = callStates.get(callId);
  if (buttonStates) {
    buttonStates.forEach((stateHandler) => {
      stateHandler.setCallState(state);
    });
  }
}

export function initCallPopup(
    callId,
    callerId,
    callerLink,
    callerAvatar,
    callerMessage,
    playRingtone) {
      
  const log = webConferencing.getLog("jitsi");
  const currentUserId = webConferencing.getUser().id;
      
  // Ring ID should be unique per a Platform instance
  const ringId = `jitsi-call-ring-${window.location.host}-${callerId}`;
  if (playRingtone) {
    // TODO We need play a single ringtone per page - it will play for at least one incoming call
    const callRinging = localStorage.getItem(ringId);
    if (!callRinging || Date.now() - callRinging > 5000) {
      log.trace(">>> Call start ringing: " + callId + " for " + currentUserId);
      // if not rnging or ring flag too old (for cases of crashed browser page w/o work in process.always below)
      localStorage.setItem(
        ringId,
        Date.now()
      ); // set it quick as possible to avoid race conditions
    } else {
      playRingtone = false;
      log.trace(">>> Call already ringing: " + callId + " for " + currentUserId);
    }
  }
  
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    // const container = document.createElement("div");
    // container.setAttribute("class", "call-popup"); // TODO why we need an ID unique per page?
    // const parentContainer = document.getElementById("UIPortalApplication");
    // parentContainer.appendChild(container);
    console.log(document.body.querySelectorAll(".call-popup"));
    // eslint-disable-next-line no-debugger
    debugger;
    let onAccepted;
    let onRejected;
    let autoRejectId;
    const comp = new Vue({
      el: "#call-popup",
      components: {
        CallPopup
      },
      data() {
        return {
          isDialogVisible: true,
          callerId: callerId,
          avatar: callerAvatar,
          callerMessage: callerMessage,
          playRingtone: playRingtone
        };
      },
      mounted() {
        console.log(this);
        // autoRejectId = setTimeout(() => {
        //   log.info("Auto rejected the call: " + callId + " user: " + currentUserId);
        //   doReject();
        // }, 60000); // Reject automatically calls in 60 seconds if the user hasn't answered
      },
      i18n,
      vuetify,
      render: function(h) {
        return h(CallPopup, {
          props: {
            isDialogVisible: this.isDialogVisible,
            caller: this.callerId,
            avatar: this.avatar,
            callerMessage: this.callerMessage,
            playRingtone: this.playRingtone,
            i18n
          },
          on: {
            accepted: doAccept,
            rejected: doReject
          }
        });
      }
    }) 
    // .$mount("#call-popup");
    function doAccept() {
      closeCallPopup(callId);
      if (playRingtone) {
        localStorage.removeItem(ringId);
      }
      if (onAccepted) {
        onAccepted();
      }
    }
    
    function doReject(isClosed) {
      closeCallPopup(callId);
      if (playRingtone) {
        localStorage.removeItem(ringId);
      }
      if (onRejected) {
        onRejected(isClosed);
      }
    }
    // comp.$mount(".call-popup");
    const popup = {
      callId,
      callerId,
      component: comp,
      close: function() {
        clearTimeout(autoRejectId); // Clear autoreject for the call
        this.component.isDialogVisible = false;
        this.component.$destroy();
      },
      onAccepted: function(callback) {
        onAccepted = callback;
      },
      onRejected: function(callback) {
        onRejected = callback;
      }
    };
    callPopups.set(callId, popup);
    return popup;
  });
}

export function closeCallPopup(callId) {
  const popup = callPopups.get(callId);
  if (popup) {
    callPopups.delete(callId);
    popup.close();
  }
}

// export function initDrawer(callId, callerId, callerLink, callerAvatar, callerMessage, playRingtone) {
//   return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    
//     const comp = new Vue({
//       el: ".room-content",
//       components: {
//         CallPopupDrawer
//       },
//       render: (h) => h(CallPopupDrawer, {props: {
//         isDialogVisible: true,
//         caller: callerId,
//         avatar: callerAvatar,
//         callerMessage: callerMessage,
//         playRingtone: playRingtone,
//         i18n
//       },}),
//       vuetify,
//       i18n
//     });
//     // initCallPopup(
//     //   callId,
//     //   callerId,
//     //   callerLink,
//     //   callerAvatar,
//     //   callerMessage,
//     //   playRingtone);
//     return comp;
//   });
// }