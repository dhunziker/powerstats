<template>
  <q-page class="flex">
    <div class="q-pa-md q-gutter-md page-content">
      <div class="row items-center">
        <div class="text-h5">Create New API Key</div>
      </div>
      <q-card>
        <q-card-section v-show="!keyCreated" style="padding-bottom: 0;">
          <q-input v-model="keyName" label="Key Name" outlined :rules="keyNameRules" />
        </q-card-section>
        <q-card-section v-show="keyCreated">
          <q-item class="item-border" v-for="(apiKey) in apiKeys" :key="apiKey.id">
            <api-key-component :api-key="apiKey" >
              {{ 'Plaintext: ' + key }}
            </api-key-component>
            <q-item-section top side>
              <div class="text-grey-8 q-gutter-xs">
                <q-btn @click="handleCopy(key)" size="20px" flat dense icon="las la-copy" />
              </div>
            </q-item-section>
          </q-item>
        </q-card-section>
        <q-card-section v-show="keyCreated" style="padding-top: 0;">
          <div class="text-negative">Don't forget to copy the plaintext key. After leaving this screen, you'll no longer be able to retrieve it.</div>
        </q-card-section>
        <q-card-actions align="right">
          <q-btn v-show="!keyCreated" label="Cancel" @click="router.push('/settings/api-keys')" />
          <q-btn v-show="keyCreated" label="Back" color="primary" @click="router.push('/settings/api-keys')" />
          <q-btn v-show="!keyCreated" label="Create" color="primary" :disable="!isKeyNameValid" @click="handleCreate" />
        </q-card-actions>
      </q-card>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import type { ApiKey} from '../services/apiKeyService';
import { createApiKey } from '../services/apiKeyService';
import { useQuasar, copyToClipboard } from 'quasar';
import { useRouter } from 'vue-router';
import { ref, computed } from 'vue';
import ApiKeyComponent from 'components/ApiKeyComponent.vue';

const $q = useQuasar();
const router = useRouter();
const emit = defineEmits(['created']);
const apiKeys = ref<ApiKey[]>([]);
const key = ref('');
const keyName = ref('');
const keyNameRules = [
  (val: string) => (val && val.length > 0) || 'Please enter a valid key name'
]
const isKeyNameValid = computed(() => keyNameRules.every(rule => rule(keyName.value) === true));
const keyCreated = ref(false);

async function handleCreate() {
  await createApiKey(keyName.value)
    .then((response) => {
      $q.notify({
        type: 'positive',
        message: `API key created successfully.`,
      });
      apiKeys.value.push(response.apiKey);
      key.value = response.key;
      keyName.value = '';
      keyCreated.value = true;
      emit('created', response.apiKey);
    })
    .catch(() => {
      $q.notify({
        type: 'negative',
        message: 'Failed to create API key, please try again later.',
      });
    });
}

async function handleCopy(key: string) {
  await copyToClipboard(key)
    .then(() => {
      $q.notify({
        type: 'positive',
        message: `API key copied to clipboard.`,
      });
    })
}
</script>
