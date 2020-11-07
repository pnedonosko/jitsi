<template>
  <div id="call-popup">
    <v-row justify="center">
      <v-dialog v-model="dialog" width="430">
        <v-card>
          <v-avatar 
            color="#578dc9" 
            width="70" 
            height="70">
            <img :src="avatar" :alt="caller" >
          </v-avatar>
          <v-card-text>{{ caller }} started a Meeting with you</v-card-text>
          <v-card-actions>
            <v-btn
              class="ma-2"
              color="success"
              elevation="0"
              fab
              dark
              @click="passAssepted(); closePopup()">
              <i class="uiIconSocPhone"></i>
            </v-btn>Join
            <v-spacer />
            <v-btn
              class="ma-2"
              outlined
              fab
              color="#999"
              @click="passRejected(); closePopup()">
              <i class="uiIconClose"></i>
            </v-btn>Ignore
          </v-card-actions>
        </v-card>
      </v-dialog>
    </v-row>
  </div>
</template>
<script>
export default {
  name: "CallPopup",
  props: {
    dialog: {
      type: Boolean,
      required: true,
      default: false
    },
    caller: {
      type: String,
      required: true
    },
    avatar: {
      type: String,
      required: true
    }
  },
  methods: {
    closePopup() {
      this.dialog = false;
    },
    passAssepted() {
      this.$emit("accepted");
    },
    passRejected() {
      this.$emit("rejected");
    }
  }
};
</script>
<style scoped lang="less">
[class^="uiIcon"] {
  content: "e937";
}
.VuetifyApp {
  .spacer {
    flex-grow: unset !important;
    width: 12%;
  }
  .v-card__actions {
    padding: 8px 2px !important;
  }
  .v-dialog {
    border-radius: 2px;
    // border: 1px solid red !important;
    height: 160px;
    .v-sheet.v-card {
      border-radius: 2px;
      height: 160px;
      border: 1px solid #292929;
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      grid-template-rows: repeat(2, 80px);
      grid-auto-rows: 10px;
      .v-avatar {
        border-radius: 50% !important;
        align-self: center;
        justify-self: center;
        top: 15px;
        left: -15px;
      }
      .v-card__text {
        grid-column: 2 / span 2;
        grid-row: 1 / span 1;
        padding: 20px 10px 20px;
      }
      .v-card__actions {
        grid-column: 2 / span 2;
        grid-row: 2 / span 1;
        .v-btn {
          padding: 0;
          height: 50px;
          widows: 50px;
          border: 1px solid;
          .v-btn__content {
            .uiIconClose::before {
              font-size: 20px !important;
            }
          }
        }
      }
    }
    //   @media (max-width: 959px) {
    //     .v-dialog {
    //       border-radius: 2px;
    //       border: 1px solid red;
    //       height: 160px;
    //     }
    //   }
  }
}
</style>