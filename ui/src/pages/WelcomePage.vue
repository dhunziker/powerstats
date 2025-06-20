<template>
  <q-page class="row items-center justify-evenly">
    <q-page class="flex">
      <div class="q-pa-md q-gutter-md page-content">
        <div class="row items-center">
          <div class="text-h5">Welcome to PowerStats!</div>
          <div class="text-body1" style="padding-top: 14px">
            Try out the lifter search below to find a lifter and view their stats.
          </div>
        </div>
        <div class="autocomplete-container">
          <q-input
            :loading="isLoading"
            v-model="searchQuery"
            debounce="300"
            label="Search for a lifter"
            outlined
            clearable
            @update:model-value="(v) => onInputChange(String(v || ''))"
          />
          <q-list v-if="lifters.length" class="autocomplete-list">
            <q-item v-for="lifter in lifters" :key="lifter" clickable @click="selectLifter(lifter)">
              <q-item-section>{{ lifter }}</q-item-section>
            </q-item>
          </q-list>
        </div>
      </div>
    </q-page>
  </q-page>
</template>

<script setup lang="ts">
import { findLifters } from '../services/lifterService';
import { useRouter } from 'vue-router';
import { ref } from 'vue';

const router = useRouter();
const searchQuery = ref('');
const lifters = ref<string[]>([]);
const isLoading = ref(false);

async function onInputChange(namePattern: string) {
  if (namePattern?.length > 2) {
    isLoading.value = true;
    await findLifters(namePattern)
      .then((response) => {
        lifters.value = response.data;
      })
      .finally(() => {
        isLoading.value = false;
      });
  } else {
    lifters.value = [];
  }
}

async function selectLifter(name: string) {
  searchQuery.value = name;
  lifters.value = [];
  await router.push('/lifter/' + encodeURIComponent(name));
}
</script>

<style scoped>
.autocomplete-container {
  position: relative;
  max-width: 400px;
}

.autocomplete-list {
  position: absolute;
  top: 100%;
  left: 0;
  width: 100%;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  margin-top: 0;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
