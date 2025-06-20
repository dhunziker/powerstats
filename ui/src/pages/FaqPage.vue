<template>
  <q-page class="row items-center justify-evenly">
    <!-- Sidebar for Page Outline -->
    <div class="page-outline">
      <q-list>
        <q-item
          v-for="heading in headings"
          :key="heading.id"
          clickable
          @click="scrollTo(heading.id)"
        >
          <q-item-section>
            <span :class="heading.level">{{ heading.text }}</span>
          </q-item-section>
        </q-item>
      </q-list>
    </div>

    <q-page class="flex">
      <div class="q-pa-md q-gutter-md page-content">
        <div class="row items-center">
          <div class="text-h5">FAQ</div>
          <div style="padding-top: 14px;">
            <div class="text-h6">Where does the data come from?</div>
            <p class="text-body1" style="padding-top: 14px;">
              This page uses data from the OpenPowerlifting project,
              <a href="https://www.openpowerlifting.org">https://www.openpowerlifting.org</a>.
              You may download a copy of the data from
              <a href="https://data.openpowerlifting.org">https://data.openpowerlifting.org</a>.
            </p>
          </div>
          <q-space />
        </div>
      </div>
    </q-page>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';

const headings = ref<{ id: string; text: string; level: string }[]>([]);

onMounted(() => {
  // Extract headings dynamically
  const elements = document.querySelectorAll('.text-h5, .text-h6');
  headings.value = Array.from(elements).map((el) => ({
    id: el.id,
    text: el.textContent || '',
    level: el.classList.contains('text-h5') ? 'level-h5' : 'level-h6',
  }));
});

function scrollTo(id: string) {
  const element = document.getElementById(id);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth' });
  }
}
</script>

<style scoped>
.page-outline {
  position: fixed;
  top: 64px;
  right: 16px;
  width: 300px;
  max-height: calc(100vh - 80px);
  overflow-y: auto;
  background: #f9f9f9;
  border: 1px solid #ddd;
  padding: 8px;
  z-index: 1000;
}

@media (max-width: 768px) {
  .page-outline {
    display: none;
  }
}

.level-h5 {
  font-weight: bold;
}

.level-h6 {
  margin-left: 16px;
  font-size: 0.9em;
}
</style>
