<template>
  <q-page class="row items-center justify-evenly">
    <login-component :is-new-user="props.isNewUser" />
  </q-page>
</template>

<script setup lang="ts">
import LoginComponent from 'components/LoginComponent.vue';
import { useUserStore } from 'stores/user';
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  isNewUser: Boolean
});
const store = useUserStore();
const router = useRouter();

onMounted(() => {
  if (store.user) {
    return router.push('/welcome');
  }
});
</script>
