import Vuex from "vuex";
import JitsiMeetButton from "./components/JitsiMeetButton.vue";
import CallPopup from "./components/CallPopup.vue";
import CallPopupList from "./components/CallPopupList.vue";
import CallPopupDrawer from "./components/CallPopupDrawer.vue";
export const EventBus = new Vue();

Vue.use(Vuex);

Vue.mixin({
  data: function() {
    return {
      EventBus: EventBus
    }
  }
})

const store = new Vuex.Store({
  state: {
    instance: 0,
    instanceArray: [],
    caller: "",
    // openDrawer: null
    isDrawerOpen: "none"
  },
  mutations: {
    setCaller(state, payload) {
      state.caller = payload.caller;
    },
    increment(state, payload) {
      console.log(payload.caller, );
      if (payload.caller !== state.caller) {
        state.instance ++;
        state.instanceArray.push(state.instance);
      }
    },
    decrement(state) {
      state.instance --;
      state.instanceArray.pop(state.instance);
    },
    openDrawer(state) {
      state.isDrawerOpen = "block"
    },
    closeDrawer(state) {
      state.isDrawerOpen = "none"
    }
    // passOpenDrawer(state, payload) {
    //   state.openDrawer = payload.method;
    // }
  }
})

Vue.component("jitsi-meet-button", JitsiMeetButton);
// Vue.component("CallPopup", CallPopup);
Vue.use(Vuetify);
const vuetify = new Vuetify({
  dark: true,
  iconfont: "",
});

// getting language of user
const lang = (eXo && eXo.env && eXo.env.portal && eXo.env.portal.language) || "en";
const localePortlet = "locale.jitsi";
const resourceBundleName = "Jitsi";
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/${localePortlet}.${resourceBundleName}-${lang}.json`;
const log = webConferencing.getLog("jitsi");
const callStates = new Map();
export const callPopups = new Map();

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
      vuetify,
      render: (h) =>
        h(JitsiMeetButton, {
          props: {
            callSettings: settings,
            i18n: i18n,
            language: lang,
            resourceBundleName: resourceBundleName,
          }
        }),
      i18n
    });
  });
}

export function updateCallState(callId, state) {
  log.trace(">>> updateCallState for " + callId + " state: " + state);
  if (state === "started") {
    savePopupLoader(callId);
  }
  const buttonStates = callStates.get(callId);
  if (buttonStates) {
    buttonStates.forEach((stateHandler) => {
      stateHandler.setCallState(state);
    });
  }
}

function savePopupLoader(callId) {
  let popupResolve = null;
  const popupLoading = new Promise((resolve) => {
    popupResolve = resolve;
  });
  callPopups.set(callId, {
    loader: popupLoading,
    resolve: popupResolve
  }); // Add sooner when call state to come
  log.trace(">>> Save call popup for " + callId);
}
export function initCallPopupList() {
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    const container = document.createElement("div");
    document.body.appendChild(container);
    // EventBus.$on("created", ref => {child += ref; console.log(child)});
    // console.log(child, "child");
    return new Vue({
      el: container,
      store: store,
      components: {
        CallPopupList, 
        CallPopup
      },
      i18n, 
      vuetify,
      render: function(h) {
        return h(CallPopupList)
      }
    })
  })
}
export function initCallPopup(
    callId,
    callerId,
    callerLink,
    callerAvatar,
    callerMessage,
    playRingtone) {

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
    const container = document.createElement("div");
    container.setAttribute("class", "list-drawer-popup-container");
    const containerDrawer = document.createElement("div");
    containerDrawer.setAttribute("class", "list-drawer-popup-container");
    const parentContainer = document.querySelectorAll(".incoming-toast-list");
    const drawerContainer = parentContainer[0];
    const listContainer = parentContainer[1];
    console.log(listContainer, parentContainer);
    //Object.values(parentContainer).map(() => {drawerContainer = parentContainer[0]; listContainer = parentContainer[1]});
    listContainer.appendChild(container);
    //if (drawerContainer.children.length) {
     // console.log(drawerContainer.children)
    //  listContainer.appendChild(container);
    //}
    drawerContainer.appendChild(containerDrawer);
   // listContainer.appendChild(container);
    console.log(drawerContainer);
    // console.log(listContainer);
    let onAccepted;
    let onRejected;
    let autoRejectId;
    const containers = Object.values(document.querySelectorAll(".list-drawer-popup-container"));
    
    // eslint-disable-next-line prefer-const
    // for(let el of containers) {
      // console.log(el);
     const comp = new Vue({
        el: container,
        store: store,
        components: {
          CallPopup
        },
        data() {
          return {
            isNotifVisible: true,
          };
        },
        mounted() {
          // autoRejectId = setTimeout(() => {
          //   log.info("Auto rejected the call: " + callId + " user: " + currentUserId);
          //   this.$store.commit("decrement");
          // if (this.$store.state.instance <= 1) {
          //   this.$store.commit("closeDrawer")
          // }
          //   doReject();
          // }, 60000); // Reject automatically calls in 60 seconds if the user hasn't answered
        },
        i18n,
        vuetify,
        render: function(h) {
          return h(CallPopup, {
            props: {
              isNotifVisible: this.isNotifVisible,
              caller: callerId,
              avatar: callerAvatar,
              callerMessage: callerMessage,
              playRingtone: playRingtone,
              index: store.state.instance,
              i18n
            },
            on: {
              accepted: doAccept,
              rejected: doReject
            }
          });
        }
      });
    const compDrawer =  new Vue({
        el: containerDrawer,
        store: store,
        components: {
          CallPopup
        },
        data() {
          return {
            isNotifVisible: true,
          };
        },
        mounted() {
          // autoRejectId = setTimeout(() => {
          //   log.info("Auto rejected the call: " + callId + " user: " + currentUserId);
          //   this.$store.commit("decrement");
          //   doReject();
          // }, 60000); // Reject automatically calls in 60 seconds if the user hasn't answered
        },
        i18n,
        vuetify,
        render: function(h) {
          return h(CallPopup, {
            props: {
              isNotifVisible: this.isNotifVisible,
              caller: callerId,
              avatar: callerAvatar,
              callerMessage: callerMessage,
              playRingtone: playRingtone,
              index: store.state.instance,
              i18n
            },
            on: {
              accepted: doAccept,
              rejected: doReject
            }
          });
        }
      });
    // }
    // comp.$mount(container);
    // compDrawer.$mount(containerDrawer);
    
    function doAccept() {
      closeCallPopup(callId);
      if (onAccepted) {
        onAccepted();
      }
    }
    
    function doReject() {
      closeCallPopup(callId);
      if (onRejected) {
        onRejected();
      }
    }
    const popup = {
      callId,
      callerId,
      close: function() {
        clearTimeout(autoRejectId); // Clear autoreject for the call
        if (playRingtone) {
          localStorage.removeItem(ringId);
        }
        comp.$root.isNotifVisible = false;
        compDrawer.$root.isNotifVisible = false;
        // console.log(comp.$root.$el);
        comp.$root.$destroy();
        compDrawer.$root.$destroy();
        drawerContainer.removeChild(compDrawer.$el);
        listContainer.removeChild(comp.$el);
        // console.log(parentContainer, drawerContainer, "drawerContainerparentContainer")
        // drawerContainer.removeChild(comp.$root.$el);
        // Object.values(parentContainer).map(cont => {console.log(cont.children); cont.removeChild(comp.$root.$el)});
      },
      onAccepted: function(callback) {
        onAccepted = callback;

      },
      onRejected: function(callback) {
        onRejected = callback;
      }
    };
    const callPopup = callPopups.get(callId);
    if (callPopup) {
      callPopup.resolve(popup);
    } else {
      log.trace(`Call popup loader not found for the call: ${callId}`);
    }
    return popup;
  });
}

export function closeCallPopup(callId) {
  const popup = callPopups.get(callId);
  callPopups.delete(callId);
  let popupPromise = null;
  if (popup) {
    popupPromise = popup.loader;
  } else {
    log.trace(`The popup promise is absent for the call: ${callId}`);
  }
  log.trace(`>>> Close call popup; popupPromise: ${popupPromise}`);
  if (popupPromise) {
    popupPromise.then(popup => {
      callPopups.delete(callId);
      popup.close();

    });
  } else {
    log.trace(`Call has no popup: ${callId}`);
  }
}

export function initDrawer() {
  // callId, callerId, callerLink, callerAvatar, callerMessage, playRingtone) {
  const container = document.createElement("div");
  document.body.appendChild(container);
  return exoi18n.loadLanguageAsync(lang, url).then((i18n) => {
    
    const comp = new Vue({
      el: container,
      store: store,
      components: {
        CallPopupDrawer
      },
      render: (h) => h(CallPopupDrawer),
      vuetify,
      i18n
    });
    return comp;
  });
}
