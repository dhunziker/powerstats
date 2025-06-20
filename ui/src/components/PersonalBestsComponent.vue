<template>
  <div class="table-container">
    <q-table
      title="Personal Bests"
      :rows="props.personalBests"
      :columns="columns"
      row-key="equipment"
      hide-pagination
    />
  </div>
</template>

<script setup lang="ts">
import type { PersonalBest } from 'src/services/lifterService';
import type { QTableColumn } from 'quasar';

const props = defineProps<{
  personalBests: PersonalBest[];
}>();
const columns: QTableColumn<PersonalBest>[] = [
  { name: 'equipment', label: 'Equipment', field: 'equipment', align: 'left' },
  { name: 'best3SquatKg', label: 'Squat', field: 'best3SquatKg', align: 'left' },
  { name: 'best3BenchKg', label: 'Bench', field: 'best3BenchKg', align: 'left' },
  { name: 'best3DeadliftKg', label: 'Deadlift', field: 'best3DeadliftKg', align: 'left' },
  { name: 'totalKg', label: 'Total', field: 'totalKg', align: 'left' },
  { name: 'dots', label: 'Dots', field: 'dots', align: 'left' }
]
</script>

<style scoped>
.table-container {
  max-width: fit-content;
}
</style>
