<template>
  <v-app class="VuetifyApp call-popup-list call-popup-list-array">
    <v-btn :ripple="false" :style="{'display': displayButton.button}" outlined @click="openDrawer">
      <i class="uiIconIncomingCalls"></i>
      <span class="message">You have {{ storage.instance - 2 }} more incoming calls</span>
      <span class="btn-link">See all</span>
    </v-btn>
    <div ref="callpopuplist" class="incoming-toast-list"></div>
  </v-app>
</template>

<script>
import CallPopup from "./CallPopup.vue";
import { callPopups } from "../main.js";
import { EventBus } from "../main.js";

export default {
  name: "CallPopupList",
  components: {
    CallPopup
  },
  data() {
    return {
      child: 0,
      storage: {}
    };
  },
  computed: {
    displayButton() {
      return this.storage.instance > 2
        ? { button: "inline-flex" }
        : { button: "none" };
    },
  },
  watch: {
    displayButton(newVal, oldVal) {
      console.log(newVal, oldVal);
    }
  },
  mounted() {
    EventBus.$on("instanceCreated", data => {
      this.storage = data.instanceCreated;
    });
  },
  updated() {
    EventBus.$on("instanceCreated", data => {
      this.storage = data.instanceCreated;
    });
  },
  methods: {
    openDrawer() {
      this.storage.isDrawerOpen = "block";
      EventBus.$emit("openDrawer");
    }
  }
};
</script>
<style lang="less">
.call-popup-list-array {
  .incoming-toast-list {
    .call-popup-toast {
      display: none;
      &:nth-child(-n+2) {
        display: flex;
      }
      &:nth-child(2) {
        margin-bottom: 60px;
      }
    }
  }
}
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
      top: 33%;
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