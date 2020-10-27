<template>
  <v-btn 
    id="myCallAction" 
    ref="jitsi" 
    outlined 
    width="86px" 
    @click.native="startCall">
    <i class="uiIconSocPhone uiIconBlue"></i>
    <span>
      {{ i18n.te("UICallButton.label.jitsi")
        ? $t("UICallButton.label.jitsi")
      : "Jitsi Call" }}
    </span>
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
    // const callButton = this.$refs.jitsi;
    //callButton.$el.dataset.targetid = this.settings.target.id;
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
  }
  [class^="uiIcon"] {
    // font-size: 12px;
    &:before {
      color: unset;
      height: 16px;
      width: 16px;
      margin-right: 4px;
    }
  }
  .theme--light.v-btn {
    margin-right: 10px;
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
