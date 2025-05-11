import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/welcome',
  },
  {
    path: '/user',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: 'register', component: () => import('pages/LoginPage.vue'), props: { isNewUser: true } },
      { path: 'login', component: () => import('pages/LoginPage.vue'), props: { isNewUser: false } },
      { path: 'activate/:activationKey', component: () => import('pages/ActivationPage.vue') },
    ],
  },
  {
    path: '/welcome',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/WelcomePage.vue') }
    ],
  },
  {
    path: '/docs',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/ApiDocsPage.vue') }
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
