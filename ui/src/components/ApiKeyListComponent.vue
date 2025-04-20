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
          <q-item-section avatar top>
            <q-icon v-if="(apiKey.expiryDate > new Date())" name="las la-key" class="text-positive" size="48px" />
            <q-icon v-else name="las la-key" class="text-negative" size="48px">
              <q-tooltip>
                Expired on {{ date.formatDate(apiKey.expiryDate, 'DD MMM YYYY, HH:mm:ss') }}
              </q-tooltip>
            </q-icon>
          </q-item-section>
          <q-item-section top>
            <q-item-label lines="1">
              <span class="text-weight-medium">{{ apiKey.name }}</span>
            </q-item-label>
            <q-item-label caption lines="1">
              {{ 'BCrypt: ' + String.fromCharCode(...apiKey.keyHash) }}
            </q-item-label>
            <q-item-label lines="1" class="q-mt-xs text-body2 text-weight-bold text-primary">
              <span>Created on {{ date.formatDate(apiKey.creationDate, 'DD MMM YYYY, HH:mm:ss') }}</span>
            </q-item-label>
          </q-item-section>
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
import { useQuasar, date } from 'quasar';
import { useRouter } from 'vue-router';

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

<style lang="scss" scoped>
.item-border {
  border: 1px solid #ccc;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 5px;
}
</style>
