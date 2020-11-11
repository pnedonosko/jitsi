<template>
  <v-row justify="center">
    <v-dialog 
      v-model="isDialogVisible" 
      width="430"
      @click-outside="passRejected">
      <v-card>
        <v-avatar 
          color="#578dc9" 
          width="70" 
          height="70">
          <img :src="avatar" :alt="caller" >
        </v-avatar>
        <i class="uiIconSocPhone start-call"></i>
        <v-card-text v-html="callerMessage" />
        <v-card-actions>
          <v-btn
            class="ma-2 accept-button"
            color="#2eb58c"
            elevation="0"
            fab
            dark
            @click="passAssepted">
            <i class="uiIconSocPhone"></i>
          </v-btn>
          <span class="button-title">JoIn</span>
          <v-spacer />
          <v-btn 
            class="ma-2 decline-button" 
            outlined 
            fab 
            color="#aeb3b7" 
            @click="passRejected">
            <i class="uiIconClose"></i>
          </v-btn>
          <span class="button-title">Ignore</span>
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
    }
  },
  data() {
    return {
    };
  },
  created() {
    console.log(this.activator)
  },
  // watch: {
  //   playRingtone(oldValue, newValue) {
  //     if (oldValue === true || newValue === true) {
  //       this.addSound("/webrtc/audio/line.mp3");
  //       this.audio.play();
  //     }
  //   }
  // },
  methods: {
    // closePopup() {
    //   this.dialog = false;
    // },
    passAssepted() {
      this.$emit("accepted");
    },
    passRejected() {
      this.$emit("rejected");
    },
  }
};
</script>
<style scoped lang="less">
.VuetifyApp {
  .spacer {
    flex-grow: unset !important;
    width: 12%;
  }
  .v-application {
    .v-dialog {
      position: absolute;
      bottom: 7%;
      right: 7%;
    }
  }
  .v-dialog {
    border-radius: 2px;
    height: 160px;
    // position: absolute;
    // bottom: 7%;
    // right: 7%;
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