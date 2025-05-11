import { defineBoot } from '#q-app/wrappers';
import { useUserStore } from 'stores/user';
import { createAxiosDateTransformer } from 'axios-date-transformer';
import axios from 'axios';

const api = createAxiosDateTransformer(
  { baseURL: process.env.API_BASE_URL }
);

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
    if (error.response && error.response.status === 401) {
      const currentRoute = router.currentRoute.value.fullPath;
      router.push({
        path: '/user/login',
        query: { redirect: currentRoute },
      });
      return new Promise(() => {});
    }
    return Promise.reject(error);
  });
});

export { axios, api };
