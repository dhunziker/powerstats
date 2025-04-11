import { defineBoot } from '#q-app/wrappers';
import axios from 'axios';

const api = axios.create({ baseURL: process.env.API_BASE_URL });

export default defineBoot(({ app }) => {
  app.config.globalProperties.$axios = axios;
  app.config.globalProperties.$api = api;
});

export { axios, api };
