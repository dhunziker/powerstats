import { defineBoot } from '#q-app/wrappers';
import { useUserStore } from 'stores/user';
import axios from 'axios';

const api = axios.create({ baseURL: process.env.API_BASE_URL });

export default defineBoot(async ({ app, router, store }) => {
  app.config.globalProperties.$axios = axios;
  app.config.globalProperties.$api = api;
  api.interceptors.request.use(async (config) => {
    const userStore = useUserStore(store);
    if (userStore.user) {
      config.headers.authorization = 'Bearer ' + userStore.user.token;
    }
    return config;
  }, null);
  api.interceptors.response.use(null, function (error) {
    if (error.response.status === 401) {
      router.push('/user/login');
    }
    return Promise.reject(error);
  });
});

export { axios, api };
