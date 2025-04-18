<template>
  <q-page class="flex">
    <div class="q-pa-md q-gutter-md">
      <div class="row items-center">
        <div class="text-h4">Create New API Key</div>
      </div>
      <q-card>
        <q-card-section>
          <q-input v-model="keyName" label="API Key Name" outlined :rules="keyNameRules" />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" color="negative" @click="router.push('/settings/api-keys')" />
          <q-btn flat label="Create" color="positive" :disable="!isKeyNameValid" @click="handleCreate" />
        </q-card-actions>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import { createApiKey } from '../services/apiKeyService';
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import { ref, computed } from 'vue';

const $q = useQuasar();
const router = useRouter();
const emit = defineEmits(['created']);
const key = ref('');
const keyName = ref('');
const keyNameRules = [
  (val: string) => (val && val.length > 0) || 'Please enter a valid key name'
]
const isKeyNameValid = computed(() => keyNameRules.every(rule => rule(keyName.value) === true));

async function handleCreate() {
  await createApiKey(keyName.value)
    .then((response) => {
      $q.notify({
        type: 'positive',
        message: `API key created successfully.`,
      });
      key.value = response.key;
      keyName.value = '';
      emit('created', response.apiKey);
      return router.push('/settings/api-keys');
    })
    .catch(() => {
      $q.notify({
        type: 'negative',
        message: 'Failed to create API key, please try again later.',
      });
    });
}
</script>

<style lang="scss" scoped>
.flex > div {
  width: 100%;
}
</style>
