<template>
  <div class="VuetifyApp">
    <v-app :style="{'display': storage.isDrawerOpen}">
      <exo-drawer
        ref="callPopupDrawer"
        class="call-popup-drawer"
        body-classes="hide-scroll"
        right
        style="width: 430px;">
        <template slot="content">
          <div class="call-popup-drawer-header-container">
            <span class="drawer-header">Incoming calls</span>
            <i class="uiIconPopupClose" @click="closedDrawer"></i>
          </div>
          <v-app class="VuetifyApp call-popup-list">
            <div ref="callpopuplistdrawer" class="incoming-toast-list"></div>
          </v-app>
        </template>
      </exo-drawer>
    </v-app>
  </div>
</template>

<script>
import CallPopup from "./CallPopup.vue";
import { EventBus } from "../main.js";

export default {
  components: {
    CallPopup
  },
  props: {},
  data() {
    return {
      showPopupDrawer: false,
      storage: {
        isDrawerOpen: "none"
      }
    };
  },
  mounted() {
    const thevue = this;
    // EventBus.$emit("closeDrawer", thevue.closedDrawer);
    EventBus.$on("openDrawer", function(payload) {
      thevue.openDrawer();
    });
    EventBus.$on("instanceCreated", data => {
      this.storage = data.instanceCreated;
    });
    this.openDrawer();
  },
  updated() {
    EventBus.$on("instanceCreated", data => {
      this.storage = data.instanceCreated;
    });

  },
  methods: {
    openDrawer() {
      this.$refs.callPopupDrawer.open();
    },
    closedDrawer() {
      this.storage.isDrawerOpen = "none";
    }
  }
};
</script>
<style lang="less">
.call-popup-drawer {
  .call-popup-drawer-header-container {
    width: 430px;
    display: flex;
    justify-content: space-between;
    .drawer-header {
      font-size: 16px;
      color: #333;
      font-weight: bold;
      height: 50px;
      padding: 15px;
    }
  }
  .uiIconPopupClose {
    opacity: 1;
    align-self: center;
    margin: 0 15px;
    cursor: pointer;
    &::before {
      color: #aeb3b7;
      content: "\e9d2";
    }
  }
  .call-popup-list {
    .incoming-toast-list {
      .call-popup-toast {
        margin: 0px 0px 0px;
        .v-sheet.v-card {
          border: none;
        }
      }
    }
  }
}
</style>