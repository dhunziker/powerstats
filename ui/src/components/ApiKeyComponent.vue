<template>
  <q-item-section avatar top>
    <q-icon v-if="(props.apiKey.expiryDate > new Date())" name="las la-key" class="text-positive" size="48px" />
    <q-icon v-else name="las la-key" class="text-negative" size="48px">
      <q-tooltip>
        Expired on {{ date.formatDate(props.apiKey.expiryDate, 'DD MMM YYYY, HH:mm:ss') }}
      </q-tooltip>
    </q-icon>
  </q-item-section>
  <q-item-section top>
    <q-item-label lines="1">
      <span class="text-weight-medium">{{ props.apiKey.name }}</span>
    </q-item-label>
    <q-item-label caption lines="1">
      Public Key: <span class="text-weight-medium">{{ props.apiKey.publicKey }}</span>
    </q-item-label>
    <q-item-label caption lines="1">
      <slot />
    </q-item-label>
    <q-item-label lines="1" class="q-mt-xs text-body2 text-weight-bold text-primary">
      <span>Created on {{ date.formatDate(apiKey.creationDate, 'DD MMM YYYY, HH:mm:ss') }}</span>
    </q-item-label>
  </q-item-section>
</template>

<script setup lang="ts">
import type { ApiKey } from '../services/apiKeyService';
import { date } from 'quasar';

interface Props {
  apiKey: ApiKey;
}

const props = defineProps<Props>();
</script>
