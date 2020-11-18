<template>
  <v-btn 
    id="myCallAction" 
    ref="jitsi" 
    :ripple="false"
    outlined 
    @click.native="startCall">
    <i class="uiIconSocPhone uiIconBlue"></i>
    <span>{{ i18n.te("UICallButton.label.jitsi")
      ? $t("UICallButton.label.jitsi")
    : "Jitsi Call" }}</span>
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
      callWindow: null
    };
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
      this.settings.onCallOpen();
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
