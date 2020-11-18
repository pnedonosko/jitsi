<template>
  <v-btn
    id="jitsiCallAction"
    ref="jitsi" 
    :ripple="false"
    outlined 
    @click.native="startCall">
    <i class="uiIconSocPhone uiIconBlue"></i>
    <span>{{ buttonTitle }}</span>
  </v-btn>
</template>

<script>
export default {
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
      callWindow: null,
      callState: null
    };
  },
  computed: {
    buttonTitle: function() {
      let title;
      console.log(`Call state for the button's title: ${this.callState}`);
      if (this.callState === "joined") {
        title = this.i18n.te("UICallButton.label.joined")
          ? this.$t("UICallButton.label.joined")
          : "Joined";
      } else if (this.callState === "started") {
        title = this.i18n.te("UICallButton.label.join")
          ? this.$t("UICallButton.label.join")
          : "Join";
      } else {
        title = this.i18n.te("UICallButton.label.jitsi")
          ? this.$t("UICallButton.label.jitsi")
          : "Jitsi Call";
      }
      return title;
    },
  },
  watch: {
    callSettings(newSettings, oldSettings) {
      this.settings = newSettings;
      this.updateCallState();
    }
  },
  created() {
    this.log = webConferencing.getLog("jitsi");
    const callButton = this.$refs.jitsi;
    this.callState = this.callSettings.callState;
  },

  mounted() {
    // Assign target ID to the button for later use on started
    // event in init()
    // console.log(this.settings)
  },
  methods: {
    startCall: function() {
      this.callSettings.onCallOpen();
    },
    updateCallState: function() {
      this.callState = this.callSettings.callState;
      console.log(`updatedCallState: ${this.callState}`);
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
  }
  .v-btn {
    padding: 0px;
  }
  .uiIconSocPhone {
    &:before {
      color: unset;
      height: 16px;
      width: 16px;
      margin-right: 4px;
      margin-left: 3px;
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
