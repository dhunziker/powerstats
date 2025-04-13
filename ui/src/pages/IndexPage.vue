<template>
  <q-page class="row items-center justify-evenly">
    <login-component
      :is-new-user="props.isNewUser"
      active
    ></login-component>
  </q-page>
</template>

<script setup lang="ts">
  import LoginComponent from 'components/LoginComponent.vue';

  const props = defineProps({
    isNewUser: Boolean
  })
</script>
