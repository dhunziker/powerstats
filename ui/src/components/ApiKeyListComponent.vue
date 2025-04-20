<template>
  <q-page class="flex">
    <div class="q-pa-md q-gutter-md page-content">
      <div class="row items-center">
        <div class="text-h5">API Keys</div>
        <q-space />
        <q-btn color="primary" @click="router.push('/settings/api-keys/create')" label="New"/>
      </div>
      <div v-if="props.apiKeys.length" class="text-subtitle1">This is a list of API keys associated with your account. Remove any keys that you do not recognize.</div>
      <div v-else class="text-subtitle1">There are no API keys associated with your account.</div>
      <q-list>
        <q-item class="item-border" v-for="(apiKey) in props.apiKeys" :key="apiKey.id">
          <api-key-component :api-key="apiKey" >
            {{ 'BCrypt: ' + String.fromCharCode(...apiKey.keyHash) }}
          </api-key-component>
          <q-item-section top side>
            <div class="text-grey-8 q-gutter-xs">
              <q-btn @click="handleDelete(apiKey.id)" class="gt-xs" size="20px" flat dense icon="las la-trash" />
            </div>
          </q-item-section>
        </q-item>
      </q-list>
    </div>
  </q-page>
</template>

<script setup lang="ts">
import type { ApiKey} from '../services/apiKeyService';
import { deleteApiKey } from '../services/apiKeyService';
import { useQuasar } from 'quasar';
import { useRouter } from 'vue-router';
import ApiKeyComponent from 'components/ApiKeyComponent.vue';

interface Props {
  apiKeys: ApiKey[];
}

const $q = useQuasar();
const router = useRouter();
const emit = defineEmits(['deleted']);
const props = withDefaults(defineProps<Props>(), {
  apiKeys: () => []
});

async function handleDelete(id: number) {
  await deleteApiKey(id)
    .then(() => {
      $q.notify({
        type: 'positive',
        message: 'API key deleted successfully.'
      });
      emit('deleted', id);
    })
    .catch(() => {
      $q.notify({
        type: 'negative',
        message: 'Failed to delete API key, please try again later.'
      });
    });
}
</script>
