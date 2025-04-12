<template>
  <q-page class="row items-center justify-evenly">
    <user-component
      :is-new-user="props.isNewUser"
      active
    ></user-component>
  </q-page>
</template>

<script setup lang="ts">
  import UserComponent from 'components/UserComponent.vue';

  const props = defineProps({
    isNewUser: Boolean
  })
</script>
