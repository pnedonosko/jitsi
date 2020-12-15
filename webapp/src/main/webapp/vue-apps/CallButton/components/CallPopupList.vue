<template>
  <v-app class="VuetifyApp call-popup-list">
    <v-btn :style="{'display': displayButton.button}" @click="openDrawer">See more</v-btn>
    <!-- <call-popup
      :is-dialog-visible="isNotifVisible"
      :caller="caller"
      :avatar="avatar"
      :caller-message="callerMessage"
      :play-ringtone="playRingtone"
      :i18n="i18n"
      @accepted="accepted"
    @rejected="rejected" />-->
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
    // childLength() {
    //   const child = 0;
    //   // EventBus.$on("created", ref => child += ref);
    //   //   console.log(child, "child");
    //   return child;
    // },
    // mapper() {
    //   return Object.values(callPopups);
    // },
    displayButton() {
      //   console.log(this.childLength, "childLength");
      //   return this.childLength > 1 ? { button: "block" } : { button: "none" };
      //   const mapper = Object.values(callPopups);
      //   console.log(this.mapper, this.mapper.length);
      this.hideChild();
      // console.log(this.$refs.callpopuplist);
      return this.$store.state.instance > 0
        ? { button: "inline-flex" }
        : { button: "none" };
    }
  },
  watch: {
    displayButton(newVal, oldVal) {
      // EventBus.$on("created", ref => this.childLength += ref);
      console.log(newVal, oldVal);
    }
  },
  //   mounted() {
  //     // console.log(this.childLength);
  //     // console.log(this.$refs.callpopuplist.childNodes);
  //     // EventBus.$on("created", ref => console.log(ref, "EventBus"))
  //   },
  //   updated() {
  //     // console.log(this.childLength);
  //     // console.log(this.$refs.callpopuplist.childNodes);
  //     // console.log(this.$refs, "REFS");
  //     // EventBus.$on("created", ref => console.log(ref, "EventBus"));
  //   },
  methods: {
    hideChild() {
      if (this.$refs.callpopuplist) {
        // console.log(this.$refs.callpopuplist.children);
        // console.log(this.$refs.callpopuplist.root);
        // console.log(this.$refs.callpopuplist._vnode);
        if (this.$refs.callpopuplist.children) {
          //   console.log(this.$refs.callpopuplist.children);
          if (this.$store.state.instance > 0) {
            // console.log(this.$refs.callpopuplist.children[2].style.display);
            Object.values(this.$refs.callpopuplist.children).map(
              (popup, index) => {
                if (index < 2 && this.$store.state.instance <= 2) {
                  console.log(popup, index, this.$store.state.instance);
                  this.$refs.callpopuplist.children[index].style.display =
                    "flex";
                }
                if (index >= 2 && this.$store.state.instance > 2) {
                  console.log(popup, index, this.$store.state.instance);
                  this.$refs.callpopuplist.children[index].style.display =
                    "none";
                }
              }
            );
            // this.$refs.callpopuplist.children[0].style.display = "flex";
            // this.$refs.callpopuplist.children[1].style.display = "flex";
            // if (this.$store.state.instance > 2) {
            //   this.$refs.callpopuplist.children[2].style.display = "none";
            // }
          }
          //   else if (this.$store.state.instance <= 2) {
          //     this.$refs.callpopuplist.children[0].style.display = "flex";
          //     this.$refs.callpopuplist.children[1].style.display = "flex";
          //   }
        }
      }
    },
    openDrawer() {
        console.log(this.$store.state);
        console.log(this.EventBus.$on("openDropdown"))
        this.$store.commit("openDrawer");
        this.EventBus.$on("openDropdown", meth => meth());
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
      text-align: right;
      margin: 15px;
      margin-bottom: 0px;
      padding: 0;
      align-self: flex-end;
      width: 430px;
      pointer-events: all;
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