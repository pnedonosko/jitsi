<template>
  <div class="VuetifyApp call-popup-list" >
    <h1 style="text-align: right;">Hello</h1>
    <!-- <call-popup
      :is-dialog-visible="isNotifVisible"
      :caller="caller"
      :avatar="avatar"
      :caller-message="callerMessage"
      :play-ringtone="playRingtone"
      :i18n="i18n"
      @accepted="accepted"
      @rejected="rejected" /> -->
  </div>
</template>

<script>
import CallPopup from "./CallPopup.vue";

export default {
  name: "CallPopupList",
  components: {
    CallPopup
  },
  data() {
    return {};
  },
  methods: {
  }
};
</script>
<style lang="less" scoped>
</style>
<style>
.VuetifyApp.call-popup-list {
    position: absolute;
    width: 100%;
    top: 0;
    text-align: right;
    display: flex;
    align-items: flex-end;
    flex-flow: column;
}</style>