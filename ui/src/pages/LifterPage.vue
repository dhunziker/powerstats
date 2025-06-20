<template>
  <q-page class="row items-center justify-evenly">
    <q-page class="flex">
      <div class="q-pa-md q-gutter-md wide-page-content">
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

const route = useRoute();
const name = ref<string>(route.params.name as string);
const sex = ref<string>('');
const personalBests = ref<PersonalBest[]>([]);
const competitionResults = ref<CompetitionResult[]>([]);

onMounted(async () => {
  await findPersonalBests(name.value).then((response) => {
    // const lifter = response.data
    // chartData.series = [lifter.best3SquatKg || 0, lifter.best3BenchKg || 0, lifter.best3DeadliftKg || 0];
    personalBests.value = response.data
    sex.value = personalBests.value[0]?.sex || '';
  });
  await findCompetitionResults(name.value).then((response) => {
    // const lifter = response.data
    // chartData.series = [lifter.best3SquatKg || 0, lifter.best3BenchKg || 0, lifter.best3DeadliftKg || 0];
    competitionResults.value = response.data
  });
});
</script>
