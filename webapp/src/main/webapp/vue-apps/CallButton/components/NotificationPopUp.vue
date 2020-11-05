<template>
  <v-row justify="center">
    <!-- <v-btn color="primary" dark @click.stop="dialog = true">Open Dialog</v-btn> -->
    <v-dialog v-model="dialog" width="390">
      <v-card>
        <!-- <v-card-title class="headline">UserName</v-card-title> -->
        <v-avatar color="indigo">
          <img :src="avatar" :alt="caller" />
        </v-avatar>
        <v-card-text>{{ caller }} started a Meeting with you</v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>

          <v-btn color="success" fab dark @click="callbackFunc(true); closePopup()">
            <i class="uiIconSocPhone"></i>
          </v-btn>Join
          <v-spacer></v-spacer>
          <v-btn class="ma-2" outlined fab color="teal" @click="callbackFunc(false); closePopup()">
            <i class="uiIconClose"></i>
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-row>
</template>
<script>
export default {
  name: "NotificationPopUp",
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
    },
    callbackFunc: {
      type: Function,
      required: true
    }
  },
  methods: {
    closePopup() {
      this.dialog = false;
    }
  }
};
</script>
<style scoped>
[class^="uiIcon"] {
  content: "e937";
}
</style>