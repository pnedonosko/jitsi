<template>
  <v-btn
    id="jitsiCallAction"
    ref="jitsi" 
    :ripple="false"
    outlined 
    @click.native="startCall">
    <i :class="buttonTitle.icon" class="uiIconSocPhone uiIconBlue"></i>
    <span>{{ buttonTitle.title }}</span>
  </v-btn>
</template>

<script>
export default {
  name: "JitsiMeetButton",
  props: {
    callSettings: {
      type: Object,
      required: true
    },
    i18n: {
      type: Object,
      required: true
    },
    language: {
      type: String,
      required: true
    },
    resourceBundleName: {
      type: String,
      required: true
    }
  },

  data: function() {
    return {
      settings: this.callSettings,
      log: null,
      callWindow: null
      // callState: null
    };
  },
  computed: {
    callState: function() {
      return this.callSettings.callState;
    },
    buttonTitle: function() {
      if (this.callState === "joined") {
        return {
          title: this.i18n.te("UICallButton.label.joined")
            ? this.$t("UICallButton.label.joined")
            : "Joined",
          icon: "callIcon-joined"
        };
      } else if (
        this.callState === "started" ||
        this.callState === "leaved"
      ) {
        return {
          title: this.i18n.te("UICallButton.label.join")
            ? this.$t("UICallButton.label.join")
            : "Join",
          icon: "callIcon-join"
        };
      } else {
        return {
          title: this.i18n.te("UICallButton.label.jitsi")
            ? this.$t("UICallButton.label.jitsi")
            : "Call",
          icon: "callIcon-call"
        };
      }
    }
  },
  created() {
    this.log = webConferencing.getLog("jitsi");
    const callButton = this.$refs.jitsi;
  },

  mounted() {
    // Assign target ID to the button for later use on started
    // event in init()
    // console.log(this.settings)
  },
  methods: {
    startCall: function() {
      if (this.callSettings.callState !== "joined") {
        this.callSettings.onCallOpen();
      }
    }
  }
};
</script>

<style scoped lang="less">
@import "../../../skin/variables.less";

.VuetifyApp {
  .v-btn:not(.v-btn--round).v-size--default {
    padding: 0px;
    min-width: unset;
    width: 100%;
    height: 100%;
  }
  .v-btn {
    padding: 0px;
    justify-content: flex-start;
  }
  .uiIconSocPhone {
    &:before {
      height: 16px;
      width: 16px;
      margin-right: 4px;
      margin-left: 3px;
    }
  }
  .callIcon-call {
    &:before {
      color: unset;
      content: "\e92b";
    }
  }
  .callIcon-join {
    &:before {
    content: "\E61C";
    font-size: 16px;
    color: #fb8e18;
    }
  }
  .callIcon-joined {
    &:before {
      color: #2eb58c;
      content: "\e92b";
    }
  }
  .theme--light.v-btn {
    background: inherit;
    &:focus::before {
      opacity: 0;
      background: transparent;
    }
    &:hover {
      &::before {
        color: @primaryColor;
        opacity: 0;
      }
      i {
        color: white;
      }
      span {
        color: white;
      }
    }
  }
}
</style>
