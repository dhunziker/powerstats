<template>
  <q-page class="row items-center justify-evenly" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useQuasar } from 'quasar';
import { useUserStore } from 'stores/user';

const store = useUserStore();
const route = useRoute();
const router = useRouter();
const $q = useQuasar();

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, ms);
  });
}

onMounted(async () => {
  $q.loading.show({
    message: 'Account activation in progress. Please wait...',
  });
  const activationKey = route.params.activationKey as string;
  await store
    .activate(activationKey)
    .then(() => delay(3000))
    .then(() => router.push('/user/login'))
    .finally(() => $q.loading.hide());
});
</script>
