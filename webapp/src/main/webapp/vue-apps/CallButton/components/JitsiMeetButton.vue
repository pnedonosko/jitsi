<template>
  <v-btn 
    id="myCallAction" 
    ref="jitsi" 
    :ripple="false"
    outlined 
    @click.native="startCall">
    <!-- <i class="uiIconSocPhone uiIconBlue"></i> -->
    <JitsiLogo
      class="logo pr-2"
      width="18px" 
      height="24px"/>
    <!-- <svg class="logo pr-3" width="18px" height="24px" viewBox="0 0 774 1042" preserveAspectRatio="xMidYMid meet"><image xlink:href="#svg5488" x="0" y="0" width="100%" height="100%"></image></svg> -->
    <!-- <img src="/jitsi/resources/assets/icons/Jitsi.svg"> -->
    <!-- <svg viewBox="0 0 30 10" xmlns="http://www.w3.org/2000/svg"><use xlink:href="#svg5488" x="20" fill="white"></use></svg> -->
    <!-- <object type="image/svg+xml" data="/jitsi/resources/assets/icons/Jitsi.svg" class="logo pr-3"></object> -->
    <span>{{ i18n.te("UICallButton.label.jitsi")
      ? $t("UICallButton.label.jitsi")
    : "Jitsi Call" }}</span>
  </v-btn>
</template>

<script>
import JitsiLogo from "../icons/Jitsi.svg";
export default {
  components: {
    JitsiLogo
  },
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
    // this.$refs.jitsi.$el.classList.add("btn--dropdown");
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
    .logo {
    //  margin-right: 2px;
     align-self: center;
    }
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
