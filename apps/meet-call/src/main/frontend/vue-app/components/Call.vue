<template>
  <div id="call">
    <h2>{{ title }}</h2>
  </div>
</template>

<script>
export default {
  name : "Call",
  data () {
    return {
      title: "eXo Jitsi Call",
      backendResponse: [],
      errors: []
    };
  },
  methods: {
    call () {
      return "";
    }
  }
};
</script>