<template>
  <v-dialog 
    v-model="showJitsi"
    content-class="Jitsi">
    <template v-slot:activator="{ on }">
      <a 
        data-placement="bottom" 
        rel="tooltip"  
        v-on="on">
        <span>Hello</span>
      </a>
    </template>
  </v-dialog>
</template>

<script>

export default {
  data: function() {
    return {
      showJitsi: false, // show or hide cloud drive drawer
    };
  },

  methods: {
  }
};
</script>