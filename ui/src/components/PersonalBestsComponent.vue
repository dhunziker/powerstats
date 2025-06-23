<template>
  <q-carousel
    v-model="slide"
    swipeable
    animated
    control-type="regular"
    control-color="primary"
    arrows
    height="240px"
    class="carousel-container rounded-borders q-table__card"
  >
    <q-carousel-slide
      v-for="(series, index) in chartData"
      :key="index"
      :name="'slide' + index"
      class="slide-container column no-wrap flex-center"
    >
      <div class="absolute-bottom custom-caption">
        <div class="text-subtitle1">{{ props.personalBests[index]?.equipment }}</div>
      </div>
      <ApexCharts
        type="pie"
        height="100%"
        :series="series"
        :options="chartOptions"
      />
    </q-carousel-slide>
  </q-carousel>
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
import ApexCharts from 'vue3-apexcharts';
import { computed, ref } from 'vue';

const props = defineProps<{
  personalBests: PersonalBest[];
}>();
const slide = ref('slide0')
const chartOptions = ref({
  labels: ['Squat', 'Bench', 'Deadlift'],
  legend: {
    show: true,
    position: 'right',
    onItemClick: {
      toggleDataSeries: false
    }
  },
});
const chartData = computed(() =>
  props.personalBests.map(pb => [pb.best3SquatKg || 0, pb.best3BenchKg || 0, pb.best3DeadliftKg || 0])
);
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
.carousel-container {
  max-width: fit-content;
}

.slide-container {
  padding: 8px 60px 6px 60px;
}

.custom-caption {
  text-align: center;
  padding: 3px;
  color: white;
  background-color: rgba(0, 0, 0, .3);
  z-index: 100;
}

.table-container {
  max-width: fit-content;
}
</style>
