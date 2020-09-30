<template>
  <v-btn
    id="jitsiCallButton"
    ref="jitsi"
    :click="startCall"
    color="white"
    elevation="1"
    class="myCallAction">
    <i class="uiIconMyCall uiIconVideoPortlet uiIconLightGray"></i>
    {{ i18n.te('UICallButton.label.jitsi')
      ? $t('UICallButton.label.jitsi')
    : 'Call with Jitsi' }}
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
      // callButtonClass: "",
      settings: this.callSettings,
      log: null,
      callWindow: null
    };
  },
  created() {
    this.log = webConferencing.getLog("jitsi");
  },
  mounted() {
    // Assign target ID to the button for later use on started
    // event in init()
    const callButton = this.$refs.jitsi;
    // console.log(this.$refs.jitsi)
    callButton.$el.dataset.targetid = this.settings.target.id;
  },
  methods: {}
};
</script>