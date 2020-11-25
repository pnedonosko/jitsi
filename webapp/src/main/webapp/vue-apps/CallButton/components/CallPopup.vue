<template>
  <v-row justify="center">
    <v-dialog
      v-model="isDialogVisible"
      width="430"
      @click:outside="passRejected">
      <v-card>
        <v-avatar 
          color="#578dc9" 
          width="70" 
          height="70">
          <img :src="avatar" :alt="caller">
        </v-avatar>
        <i class="uiIconSocPhone start-call"></i>
        <v-card-text v-html="callerMessage" />
        <v-card-actions>
          <v-btn
            class="ma-2 accept-button"
            color="#2eb58c"
            elevation="0"
            fab
            @click="passAccepted">
            <i class="uiIconSocPhone"></i>
          </v-btn>
          <span class="button-title">JoIn</span>
          <v-spacer />
          <v-btn 
            class="ma-2 decline-button" 
            outlined 
            fab 
            color="#b1b5b9" 
            @click="passRejected()">
            <i class="uiIconClose"></i>
          </v-btn>
          <span class="button-title">Ignore</span>
          <audio 
            ref="audio" 
            style="display: none" 
            loop 
            preload="auto">
            <source :src="ringtone">
            <p>"Your browser does not support the audio element</p>
          </audio>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>

<script>
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
    }
  },
  data() {
    return {
      ringtone: "/jitsi/resources/audio/ringtone_exo-1.m4a",
      callRinging: "",
      ringId: ""
    };
  },
  mounted() {
    const ringId = `jitsi-call-ring-${this.caller}`;
    this.ringId = ringId;
    // // let $ring;
    // localStorage.removeItem(this.ringId);
    const callRinging = localStorage.getItem(ringId);
    // log.trace(callRinging);

    if (!callRinging || Date.now() - callRinging > 5000) {
      // log.trace(">>> Ringing the caller: " + callerId);
      // if not rnging or ring flag too old (for cases of crashed browser page w/o work in process.always below)
      localStorage.setItem(
        ringId,
        Date.now()
      ); // set it quick as possible to avoid rice conditions
      this.$refs.audio.play();
    } 
    else {
      this.$refs.audio.stop();
    }
  },
  // mounted() {
  //   // this.$watch(this.ringtone, function() {
  //   //   this.$refs.player.load();
  //   // });
  // },
  methods: {
    passAccepted() {
      if (localStorage.getItem(this.ringId)) {
        localStorage.removeItem(this.ringId);
      }
      this.$emit("accepted");
    },
    passRejected() {
      if (localStorage.getItem(this.ringId)) {
        localStorage.removeItem(this.ringId);
      }
      this.$emit("rejected");
    }
  }
};
</script>
<style scoped lang="less">
.VuetifyApp {
  .spacer {
    flex-grow: unset !important;
    width: 12%;
  }
  //.theme--light.v-card > .v-card__text
  .v-dialog__content {
    justify-content: center;
    left: unset;
    bottom: 2%;
    top: unset;
    right: 2%;
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
      border: 1px solid #292929;
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
        color: #404040;
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
                &::before {
                  color: #aeb3b7;
                  font-size: 35px;
                  content: "\00d7";
                  position: absolute;
                  top: -25px;
                  left: -25px;
                  transform: translate(14px, 5px);
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
}
</style>