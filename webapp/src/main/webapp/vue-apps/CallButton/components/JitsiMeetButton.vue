<template>
  <v-btn
    ref="jitsi"
    class="myCallAction"
    outlined="true"
    height="36px"
    min-width="80px"
    @click.native="startCall">
    <!-- <i class="uiIconMyCall uiIconVideoPortlet uiIconLightGray"></i> -->
    <i class="uiIconSocPhone uiIconSocBlue"></i>
    {{ i18n.te("UICallButton.label.jitsi")
      ? $t("UICallButton.label.jitsi")
    : "Jitsi Call" }}
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
  mounted() {
    //const callButton = this.$refs.jitsi;
  },
  methods: {
    startCall: function() {
      this.settings.onCallOpen();
    }
  }
};
</script>
<style>
.VuetifyApp button:not(.ignore-vuetify-classes),
.VuetifyApp input:not(.ignore-vuetify-classes),
.VuetifyApp select:not(.ignore-vuetify-classes),
.VuetifyApp textarea:not(.ignore-vuetify-classes) {
  background-color: transparent;
  border: 1px solid rgb(232, 238, 242);
  border-radius: 3px;
}
.room-actions-container [class^="uiIcon"]:before {
  color: unset;
  height: 16px;
  width: 16px;
}
[class^="uiIcon"] {
  font-size: 12px;
}

.VuetifyApp .v-btn__content {
  letter-spacing: 0.1px;
  justify-content: space-between;
}
.VuetifyApp .v-btn:not(.v-btn--round).v-size--default {
  padding: 0 10px;
  margin-right: 10px;
}
</style>