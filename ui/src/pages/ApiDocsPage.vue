<template>
  <api-docs-component :url=apiDocsUrl />
</template>

<script setup lang="ts">
import ApiDocsComponent from 'components/ApiDocsComponent.vue';
const apiDocsUrl = `${process.env.API_BASE_URL}/docs/docs.yaml`;
</script>
