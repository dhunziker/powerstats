<template>
  <div class="q-pa-md">
    <p>API Keys</p>
    <q-table
      :rows="apiKeys"
      :columns="columns"
      row-key="id"
    />
  </div>
</template>

<script setup lang="ts">
import type { ApiKey} from 'src/services/apiKeyService';
import { getApiKeys } from 'src/services/apiKeyService';
import { ref } from 'vue';

const columns = ref([
  {
    name: 'id',
    label: 'ID',
    field: 'id',
    required: true,
    // align: 'left',
    sortable: true
  },
  {
    name: 'accountId',
    label: 'Account ID',
    field: 'accountId',
    required: true,
    // align: 'left',
    sortable: true
  },
  {
    name: 'key',
    label: 'Key',
    field: 'key',
    required: true,
    // align: 'left',
    sortable: true
  },
  {
    name: 'creationDate',
    label: 'Creation Date',
    field: 'creationDate',
    required: true,
    // align: 'left',
    sortable: true
  },
  {
    name: 'expiryDate',
    label: 'Expiry Date',
    field: 'expiryDate',
    required: true,
    // align: 'left',
    sortable: true
  }
])
const apiKeys = ref<ApiKey[]>([]);
getApiKeys().then((result) => {
  apiKeys.value = result;
}).catch((error) => {
  console.error('Error fetching API keys:', error);
});
</script>
