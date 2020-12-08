<template>
  <div class="VuetifyApp">
  <v-app v-if="isDialogVisible">
    <div>
      <v-dialog
        ref="incoming"
        :retain-focus="false"
        v-model="isDialogVisible"
        content-class="incoming-dialog"
        no-click-animation
        persistent
        hide-overlay
        width="430" >
        <v-card>
          <v-avatar 
            color="#578dc9" 
            width="70" 
            height="70">
            <img :src="avatar" :alt="caller" >
          </v-avatar>
          <i class="uiIconSocPhone start-call"></i>
          <v-card-text color="#333" v-html="callerMessage" />
          <v-card-actions color="#333">
            <v-btn 
              class="ma-2 accept-button" 
              color="#2eb58c" 
              elevation="0" 
              fab 
              @click="passAccepted">
              <i class="uiIconSocPhone"></i>
            </v-btn>
            <span class="button-title" @click="passAccepted">
              {{ i18n.te("UICallPopup.label.join")
                ? $t("UICallPopup.label.join")
              : "Join" }}
            </span>
            <v-spacer />
            <v-btn 
              class="ma-2 decline-button" 
              outlined 
              fab 
              color="#b1b5b9" 
              @click="passRejected()">
              <i class="uiIconClose"></i> 
            </v-btn>
            <span class="button-title" @click="passRejected()">
              {{ i18n.te("UICallPopup.label.ignore")
                ? $t("UICallPopup.label.ignore")
              : "Ignore" }}
            </span>
            <audio 
              class="audio-call-popup"
              ref="audio" 
              style="display: none" 
              loop 
              preload="auto">
              <source src="/jitsi/resources/audio/ringtone_exo-1.m4a" >
              <p>"Your browser does not support the audio element</p>
            </audio>
          </v-card-actions>
        </v-card>
      </v-dialog>
      <!-- <h1>Hello</h1>
      <v-text-field label="INPUT"></v-text-field> -->
    </div>
  </v-app>
  </div>
</template>

<script>
function stopAudio(audio) {
  if (audio) {
    audio.pause();
    audio.currentTime = 0;
  }
}

export default {
  name: "CallPopup",
  props: {
    isDialogVisible: {
      type: Boolean,
      required: true,
      default: false
    },
    caller: {
      type: String,
      required: true
    },
    avatar: {
      type: String,
      required: true
    },
    callerMessage: {
      type: String,
      required: true
    },
    playRingtone: {
      type: Boolean,
      required: true
    },
    // state: {
    //   type: Boolean,
    //   required: false,
    //   default: null
    // },
    i18n: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      state: null,
    };
  },
  mounted() {
    // console.log(this.$refs.incoming.$el.parentElement.parentElement);
    // this.$refs.application.$el.height = "100vh";
    // this.$refs.application.$el.overflow = "scroll";
    // console.log(this.$refs.application.$el);
    this.state = "shown";
    // this.class();
    if (this.playRingtone) {
      try {
        //audio.muted = true;
        // TODO this would help to fix "Uncaught (in promise) DOMException: play() failed because the user didn't interact with the document first."
        //document.body.addEventListener("mousemove", function () {
        //  audio.play();
        //});
        this.$refs.audio.play();
      } catch (e) {
        // TODO we need remove this popup flag from local storage to let others to play
        console.log("Error playing ringtone for Jitsi call: " + this.caller, e);
      }
    }
  },
  // updated() {
  //   // this.class();
  // },
  methods: {
    passAccepted() {
      if (this.state === "shown") {
        this.state = "closed";
        this.$emit("accepted");
        stopAudio(this.$refs.audio);
      }
    },
    passRejected() {
      if (this.state === "shown") {
        this.state = "closed";
        this.$emit("rejected");
        stopAudio(this.$refs.audio);
      }
    }
  }
};
</script>
<style scoped lang="less">
// .VuetifyApp {
  .spacer {
    flex-grow: unset !important;
    width: 12%;
  }
  //.theme--light.v-card > .v-card__text
  .v-dialog__content {
    justify-content: center;
    // position: absolute;
    left: unset;
    // bottom: 2%;
    // top: unset;
    // right: 2%;
    position: static;
    margin: 0px 10px 20px;
    height: fit-content;
    width: fit-content;
    height: -moz-fit-content;
    width: -moz-fit-content;
  }
  .v-dialog {
    border-radius: 2px;
    height: 160px;
    .v-sheet.v-card {
      border-radius: 2px;
      height: 160px;
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(2, 80px);
      grid-auto-rows: 10px;
      width: initial;
      [class^="uiIcon"].start-call {
        position: absolute;
        top: 15%;
        left: 24%;
        &::before {
          content: "\e61c";
          color: #fb8e18;
          font-size: 20px;
        }
      }
      .v-avatar {
        border-radius: 50% !important;
        align-self: center;
        justify-self: center;
        top: 15px;
        left: -15px;
      }
      .v-card__text {
        grid-column: 2 / span 2;
        grid-row: 1 / span 1;
        padding: 20px 15px 20px 0px;
        font-size: 16px;
        color: #333;
      }
      .v-card__actions {
        grid-column: 2 / span 2;
        grid-row: 2 / span 1;
        padding: 8px 0px !important;
        .v-btn {
          padding: 0;
          height: 50px;
          width: 50px;
          border: 1px solid;
          margin-left: 0px !important;
          .v-btn__content {
            [class^="uiIcon"]::before {
              font-size: 25px;
            }
          }
          &.accept-button {
            .v-btn__content {
              [class^="uiIcon"] {
                color: white;
                &::before {
                  content: "\e92b";
                  left: 0%;
                  bottom: 0%;
                  transform: translate(-13px, 12px);
                  position: absolute;
                }
              }
            }
          }
          &.decline-button {
            &:before {
              color: transparent;
            }
            .v-btn__content {
              [class^="uiIcon"] {
                position: relative;
                height: 25px;
                width: 50px;
                &::before {
                  color: #aeb3b7;
                  font-size: 40px;
                  content: "\00d7";
                  position: absolute;
                  right: 0%;
                  bottom: 0%;
                  transform: translate(-13px, 4px);
                }
              }
              .uiIconClose {
                opacity: 1;
              }
            }
          }
        }
        .button-title {
          font-weight: 700;
          font-size: 14px;
          cursor: pointer;
          color: #333;
        }
      }
    }
    //   @media (max-width: 959px) {
    //     .v-dialog {
    //       border-radius: 2px;
    //       border: 1px solid red;
    //       height: 160px;
    //     }
    //   }
  }
// }
</style>

<style>
  #vuetify-apps {
    padding-top: 20px;
    display: flex;
    flex-flow: column;
    align-items: flex-end;
    justify-content: flex-end;
    width: 100%;
    min-height: 100vh;
  }
.incoming-dialog {
  border: 1px solid #aeb3b7;
}
.VuetifyApp.call-popup {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow-y: scroll;
  z-index: 200;
}
</style>