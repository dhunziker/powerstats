<template>
  <q-page class="row items-center justify-evenly">
    <router-view :apiKeys="apiKeys" @created="onCreatedEvent" @deleted="onDeletedEvent" />
  </q-page>
</template>

<script setup lang="ts">
import type { ApiKey } from '../services/apiKeyService';
import { getApiKeys } from '../services/apiKeyService';
import { onMounted, ref } from 'vue';

const apiKeys = ref<ApiKey[]>([]);

const onCreatedEvent = (apiKey: ApiKey) => {
  apiKeys.value.push(apiKey);
};

const onDeletedEvent = (id: number) => {
  const index = apiKeys.value.findIndex((apiKey) => apiKey.id === id);
  apiKeys.value.splice(index, 1);
};

onMounted(async () => {
  await getApiKeys().then((response) => {
    apiKeys.value = response.data;
  });
});
</script>
