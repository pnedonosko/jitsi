import JitsiMeetButton from "./components/JitsiMeetButton.vue";
import CallPopup from "./components/CallPopup.vue";
// import { Howl } from "howler";

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

// console.log(webConferencing)

// let audio;
// let decline;
// function addCallSound(sound) {
//   if (sound) {
//     audio = new Howl({
//       autoUnlock: true,
//       // autoplay: true,
//       src: [sound],
//       loop: true,
//       preload: true
//     });
//   }
// }
// function addRejectSound(sound) {
//   if (sound) {
//     decline = new Howl({
//       autoUnlock: true,
//       // autoplay: true,
//       src: [sound],
//       preload: true
//     });
//   }
// }
// addCallSound("/jitsi/resources/audio/ringtone_exo-1.m4a");
// addRejectSound("/webrtc/audio/manner_cancel.mp3");

// console.log(audio.loop)
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
  playRingtone
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
          playRingtone: playRingtone
        };
      },
      mounted() {
        // console.log("mounted!")
        // if (playRingtone) {
        //   audio.play();
        // }
        webConferencing.jitsi.playIncomingRing(callerId, playRingtone);
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
            // audio: audio
          },
          on: {
            accepted: function() {
              if (onAccepted) {
                onAccepted();
                thevue.isDialogVisible = false;
                // audio.stop();
                thevue.$destroy();
              }
            },
            rejected: function(isClosed) {
              if (onRejected) {
                // audio.stop();
                // decline.play();
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