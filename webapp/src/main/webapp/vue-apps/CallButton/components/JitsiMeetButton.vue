<template>
  <v-btn id="myCallAction" ref="jitsi" outlined @click.native="startCall">
    <!-- <svg-sprite /> -->
    <i class="uiIconSocPhone uiIconBlue"></i>
    <!-- <svg viewBox="0 0 100 100" class="icon shape-codepen">
      <use xlink:href="#shape-codepen"></use>
    </svg> -->
    <!-- <svg-icon icon="Jitsi"/> -->
    {{ i18n.te("UICallButton.label.jitsi")
      ? $t("UICallButton.label.jitsi")
    : "Jitsi Call" }}
  </v-btn>
</template>

<script>
// import SvgIcon from "./SvgIcon.vue";
// import SvgSprite from "./SvgSprite.vue";
export default {
  // components: {
  //   SvgIcon,
  //   SvgSprite
  // },
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
    };
  },
  create() {
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
  [class^="uiIcon"] {
    font-size: 12px !important;
    &:before {
      color: unset;
      height: 16px;
      width: 16px;
      margin-right: 4px;
    }
  }
  .theme--light.v-btn {
    margin-right: 10px;
    background: inherit;
    // border: 1px solid rgb(232, 238, 242);
    &:focus::before {
      opacity: 0;
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
