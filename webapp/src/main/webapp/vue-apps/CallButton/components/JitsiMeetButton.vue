<template>
  <v-btn
    ref="jitsi"
    class="myCallAction"
    outlined="true"
    height="33px"
    min-width="fit-content"
    @click.native="startCall">
    <i class="uiIconMyCall uiIconVideoPortlet uiIconLightGray"></i>
    {{ i18n.te("UICallButton.label.jitsi")
      ? $t("UICallButton.label.jitsi")
    : "Call with Jitsi" }}
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
  border: 1px solid #e1e8ee;
  border-radius: 2px;
}
</style>