<template>
  <div class="VuetifyApp">
    <v-app :style="{'display': this.$store.state.isDrawerOpen}">
      <exo-drawer
        ref="callPopupDrawer"
        class="callPopupDrawer"
        body-classes="hide-scroll"
        right
        style="width: 430px;">
        <template slot="content">
          <span class="drawer-header" @click="closedDrawer">Incoming calls</span>
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
.callPopupDrawer {
      .drawer-header {
        font-size: 16px;
        color: #333;
        font-weight: bold;
        width: 100%;
        height: 50px;
        padding: 15px;
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