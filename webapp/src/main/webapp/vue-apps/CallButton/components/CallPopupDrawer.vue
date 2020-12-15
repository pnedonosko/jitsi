<template>
  <div class="VuetifyApp">
    <v-app :style="{'display': this.$store.state.isDrawerOpen}">
      <!-- <v-btn :style="{'display': displayButton.button}" @click="openDrawer">See more</v-btn> -->
      <exo-drawer
        ref="callPopupDrawer"
        class="callPopupDrawer"
        body-classes="hide-scroll"
        right
        style="width: 475px;">
        <template slot="content">
          <div>Hello1</div>
          <v-app class="VuetifyApp call-popup-list">
            <div ref="callpopuplistdrawer" class="incoming-toast-list"></div>
          </v-app>
          <!-- <CallPopup :isdialogvisible="isNotifVisible" :caller="caller" :avatar="avatar" :callermessage="callerMessage" :playringtone="playRingtone" :state="state" :i18n="i18n"/> -->
        </template>
      </exo-drawer>
    </v-app>
  </div>
</template>
<script>

import CallPopup from "./CallPopup.vue";
// import { EventBus } from "../main.js";

export default {
  components: {
    CallPopup
  },
  props: {},
  data() {
    return {
      showPopupDrawer: false
    };
  },
  // computed: {
  //   displayButton() {
  //     // this.hideChild();
  //     return this.$store.state.instance > 0
  //       ? { button: "inline-flex" }
  //       : { button: "none" };
  //   }
  // },
  // watch: {
  //   displayButton(newVal, oldVal) {
  //     // console.log(newVal, oldVal);
  //   }
  // },
  //   created() {
  //   },
  mounted() {
    // const thevue = this;
    // this.$store.commit({type: "passOpenDrawer", method: this.$refs.callPopupDrawer.open()
    // })
  //  this.$refs.callPopupDrawer.open();
    this.EventBus.$emit("openDropdown", () => this.openDrawer())
         this.openDrawer();

  },
  methods: {
    openDrawer() {
      this.$refs.callPopupDrawer.open();
    },
    // closedDrawer() {
    //   this.showPopupDrawer = false;
    // }
  }
};
</script>