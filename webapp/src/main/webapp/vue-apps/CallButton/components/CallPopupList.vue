<template>
  <v-app class="VuetifyApp call-popup-list">
    <v-btn :ripple="false" :style="{'display': displayButton.button}" outlined @click="openDrawer">
      <i class="uiIconIncomingCalls"></i>
      <span class="message">You have {{ this.$store.state.instance - 2 }} more incoming calls</span>
      <span class="btn-link">See all</span>
    </v-btn>
    <div ref="callpopuplist" class="incoming-toast-list"></div>
  </v-app>
</template>

<script>
import CallPopup from "./CallPopup.vue";
// import { EventBus } from "../main.js";

export default {
  name: "CallPopupList",
  components: {
    CallPopup
  },
  data() {
    return {
      child: 0
    };
  },
  computed: {
    displayButton() {
      this.hideChild();
      return this.$store.state.instance > 2
        ? { button: "inline-flex" }
        : { button: "none" };
    }
  },
  watch: {
    displayButton(newVal, oldVal) {
      console.log(newVal, oldVal);
    }
  },
  methods: {
    hideChild() {
      if (this.$refs.callpopuplist) {
        if (this.$refs.callpopuplist.children) {
          if (this.$store.state.instance > 0) {
            Object.values(this.$refs.callpopuplist.children).map(
              (popup, index) => {
                if (index < 2 && this.$store.state.instance <= 2) {
                  this.$refs.callpopuplist.children[index].style.display =
                    "flex";
                }
                if (index >= 2 && this.$store.state.instance > 2) {
                  this.$refs.callpopuplist.children[index].style.display =
                    "none";
                }
              }
            );
          }
        }
      }
    },
    openDrawer() {
      this.$store.commit("openDrawer");
      this.EventBus.$emit("openDropdown");
    }
  }
};
</script>
<style lang="less" scoped>
</style>
<style scoped lang="less">
.VuetifyApp {
  &.call-popup-list {
    position: absolute;
    width: 100%;
    top: 0;
    text-align: right;
    height: 100vh;
    overflow-y: scroll;
    pointer-events: none;
    .v-btn {
      border-radius: 2px;
      position: absolute;
      background-color: white;
      border: 1px solid #aeb3b7;
      top: 40%;
      text-align: right;
      justify-content: space-evenly;
      margin: 15px;
      margin-bottom: 0px;
      padding: 0;
      align-self: flex-end;
      width: 428px;
      pointer-events: all;
      .uiIconIncomingCalls {
        &::before {
          content: "\e61c";
          color: #fb8e18;
          font-size: 20px;
        }
      }
      span {
        font-size: 16px;
        &.message {
          color: #333;
          font-weight: bold;
        }
        &.btn-link {
          color: #aeb3b7;
          text-decoration: none;
        }
      }
    }
    .incoming-toast-list {
      display: flex;
      align-items: flex-end;
      justify-content: flex-end;
      flex-flow: column;
      min-height: 100vh;
      overflow-y: scroll;
      width: 430px;
      align-self: flex-end;
      padding: 15px;
      z-index: 100;
      //   pointer-events: all;
      //   padding-top: 20px;
    }
  }
}
</style>