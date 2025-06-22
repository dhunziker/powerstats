<template>
  <q-page class="row items-center justify-evenly">
    <q-page class="flex">
      <div v-if="!$q.loading.isActive" class="q-pa-md q-gutter-md wide-page-content">
        <div class="row items-center">
          <div class="text-h5">{{ name }} ({{ sex }})</div>
        </div>
        <personal-bests-component :personalBests="personalBests"/>
        <competition-results-component :competitionResults="competitionResults"/>
      </div>
    </q-page>
  </q-page>
</template>

<script setup lang="ts">
import PersonalBestsComponent from 'components/PersonalBestsComponent.vue';
import CompetitionResultsComponent from 'components/CompetitionResultsComponent.vue';
import type { PersonalBest, CompetitionResult } from '../services/lifterService';
import  { findPersonalBests, findCompetitionResults } from '../services/lifterService';
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useQuasar } from 'quasar';

const $q = useQuasar();
const route = useRoute();
const name = ref<string>(route.params.name as string);
const sex = ref<string>('');
const personalBests = ref<PersonalBest[]>([]);
const competitionResults = ref<CompetitionResult[]>([]);

onMounted(async () => {
  $q.loading.show();
  await Promise.all([
    findPersonalBests(name.value),
    findCompetitionResults(name.value)
  ]).then(([personalBestsResponse, competitionResultsResponse]) => {
    sex.value = personalBestsResponse.data[0]?.sex || '';
    personalBests.value = personalBestsResponse.data
    competitionResults.value = competitionResultsResponse.data
  }).finally(() => $q.loading.hide());
});
</script>
