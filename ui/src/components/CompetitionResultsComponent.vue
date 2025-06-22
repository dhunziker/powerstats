<template>
  <q-table
    title="Competition Results"
    :rows="props.competitionResults"
    :columns="columns"
    hide-pagination
    :rows-per-page-options="[0]"
  >
    <template v-slot:header="props">
      <q-tr :props="props">
        <q-th
          v-for="col in filteredColumns"
          :key="col.name"
          :colspan="colspan(col.name)"
          style="text-align: left"
        >
          {{ col.label }}
        </q-th>
      </q-tr>
    </template>
  </q-table>
</template>

<script setup lang="ts">
import type { CompetitionResult } from 'src/services/lifterService';
import type { QTableColumn } from 'quasar';

const props = defineProps<{
  competitionResults: CompetitionResult[];
}>();
const columns: QTableColumn<CompetitionResult>[] = [
  { name: 'place', label: 'Place', field: 'place', align: 'left' },
  { name: 'federation', label: 'Fed', field: 'federation', align: 'left' },
  { name: 'date', label: 'Date', field: (event) => event.date.toDateString(), align: 'left' },
  { name: 'meetCountry', label: 'Location', field: 'meetCountry', align: 'left' },
  { name: 'meetName', label: 'Competition', field: 'meetName', align: 'left' },
  { name: 'division', label: 'Division', field: 'division', align: 'left' },
  { name: 'age', label: 'Age', field: 'age', align: 'left' },
  { name: 'equipment', label: 'Equipment', field: 'equipment', align: 'left' },
  { name: 'weightClassKg', label: 'Class', field: 'weightClassKg', align: 'left' },
  { name: 'bodyweightKg', label: 'Weight', field: 'bodyweightKg', align: 'left' },
  { name: 'squat1Kg', label: 'Squat', field: (event) => event.squat1Kg ?? event.squat2Kg ?? event.squat3Kg ?? event.squat4Kg ?? event.best3SquatKg, align: 'left' },
  { name: 'squat2Kg', label: 'Squat', field: 'squat2Kg', align: 'left' },
  { name: 'squat3Kg', label: 'Squat', field: 'squat3Kg', align: 'left' },
  { name: 'squat4Kg', label: 'Squat', field: 'squat4Kg', align: 'left' },
  { name: 'bench1Kg', label: 'Bench', field: (event) => event.bench1Kg ?? event.bench2Kg ?? event.bench3Kg ?? event.bench4Kg ?? event.best3BenchKg, align: 'left' },
  { name: 'bench2Kg', label: 'Bench', field: 'bench2Kg', align: 'left' },
  { name: 'bench3Kg', label: 'Bench', field: 'bench3Kg', align: 'left' },
  { name: 'bench4Kg', label: 'Bench', field: 'bench4Kg', align: 'left' },
  { name: 'deadlift1Kg', label: 'Deadlift', field: (event) => event.deadlift1Kg ?? event.deadlift2Kg ?? event.deadlift3Kg ?? event.deadlift4Kg ?? event.best3DeadliftKg, align: 'left' },
  { name: 'deadlift2Kg', label: 'Deadlift', field: 'deadlift2Kg', align: 'left' },
  { name: 'deadlift3Kg', label: 'Deadlift', field: 'deadlift3Kg', align: 'left' },
  { name: 'deadlift4Kg', label: 'Deadlift', field: 'deadlift4Kg', align: 'left' },
  { name: 'totalKg', label: 'Total', field: 'totalKg', align: 'left' },
  { name: 'dots', label: 'Dots', field: 'dots', align: 'left' },
];
const filteredColumns: QTableColumn<CompetitionResult>[] = columns.filter((col) =>
  col.name.match(/^(?!.*[2-4]Kg$).*$/),
);

function colspan(columnName: string) {
  return columnName.match(/^((squat|bench|deadlift)[1-4]Kg$).*$/) ? 4 : 1;
}
</script>
