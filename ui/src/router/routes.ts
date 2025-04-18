import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: 'user/login',
  },
  {
    path: '/user',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: 'register', component: () => import('pages/IndexPage.vue'), props: { isNewUser: true } },
      { path: 'login', component: () => import('pages/IndexPage.vue'), props: { isNewUser: false } }
    ],
  },
  {
    path: '/settings',
    redirect: '/settings/api-keys',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      {
        path: 'api-keys',
        component: () => import('pages/ApiKeyPage.vue'),
        children: [
          { path: '', component: () => import('components/ApiKeyListComponent.vue') },
          { path: 'create', component: () => import('components/ApiKeyCreateComponent.vue') },
        ]
      },
    ],
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
];

export default routes;
