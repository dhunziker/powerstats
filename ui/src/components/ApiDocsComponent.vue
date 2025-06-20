<template>
  <div id="swagger-ui"></div>
</template>

<script setup lang="ts">
import { SwaggerUIBundle, SwaggerUIStandalonePreset } from 'swagger-ui-dist'
import 'swagger-ui-dist/swagger-ui.css'
import { onMounted } from 'vue';

const props = defineProps<{
  url: string;
}>();

onMounted( () => {
  SwaggerUIBundle({
    dom_id: '#swagger-ui',
    url: props.url,
    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
  })
});
</script>
