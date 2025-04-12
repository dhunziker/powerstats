import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('layouts/LoginLayout.vue'),
    redirect: 'user/login',
    children: [],
  },
  {
    path: '/user',
    component: () => import('layouts/LoginLayout.vue'),
    children: [
      { path: 'register', component: () => import('pages/IndexPage.vue'), props: { isNewUser: true } },
      { path: 'login', component: () => import('pages/IndexPage.vue'), props: { isNewUser: false } }
    ],
  },
  {
    path: '/dashboard',
    component: () => import('layouts/MainLayout.vue'),
    children: [{ path: '', component: () => import('pages/DashboardPage.vue') }],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
