<template>
  <v-btn 
    id="myCallAction" 
    ref="jitsi" 
    outlined="true" 
    @click.native="startCall">
    <i class="uiIconSocPhone uiIconSocBlue"></i>
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
    this.log = webConferencing.getLog("jitsi!!!");
    const callButton = this.$refs.jitsi;
    console.log(this.settings, "sett");
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
  .theme--light.v-btn {
    &:hover {
      &::before {
        color: transparent;
        opacity: 0;
      }
    }
  }
}
</style>