<template>
  <div class="VuetifyApp">
    <v-app :style="{'display': this.$store.state.isDrawerOpen}">
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
  mounted() {
    const thevue = this;
    this.EventBus.$on("openDropdown", function(payload) {
      thevue.openDrawer();
    });
    this.openDrawer();
  },
  methods: {
    openDrawer() {
      this.$refs.callPopupDrawer.open();
    },

    closedDrawer() {
      this.$store.commit("closeDrawer");
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