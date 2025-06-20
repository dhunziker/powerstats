<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar class="bg-primary">
        <q-toolbar-title style="padding: 10px 0 10px 0; height: 65px;">
          <img src="~assets/logo.svg" alt="Logo" style="height: 100%; cursor: pointer" @click="router.push('/')"/>
        </q-toolbar-title>
        <q-separator dark vertical />
        <q-btn-dropdown stretch flat label="Menu" dropdown-icon="expand_more">
          <div class="row no-wrap q-pa-md">
            <div class="column">
              <q-list>
                <q-item clickable v-close-popup @click="router.push('/welcome')">
                  <q-item-section>
                    <q-item-label>Welcome</q-item-label>
                    <q-item-label caption>Welcome page.</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="router.push('/faq')">
                  <q-item-section>
                    <q-item-label>FAQ</q-item-label>
                    <q-item-label caption>Frequently asked questions.</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="router.push('/docs')">
                  <q-item-section>
                    <q-item-label>API Documentation</q-item-label>
                    <q-item-label caption>API documentation with examples.</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="router.push('/settings/api-keys')">
                  <q-item-section>
                    <q-item-label>API Key Management</q-item-label>
                    <q-item-label caption>Manage your keys for accessing the API.</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </div>
            <q-separator vertical inset class="q-mx-lg" />
            <div class="column items-center" v-show="!showLogout">
              <q-btn color="primary" @click="showLogin">Login</q-btn>
            </div>
            <div class="column items-center" v-show="showLogout">
              <q-btn color="primary" @click="handleLogout">Logout</q-btn>
            </div>
          </div>
        </q-btn-dropdown>
        <q-separator dark vertical />
      </q-toolbar>
    </q-header>
    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { useUserStore } from 'stores/user';
import { useRouter } from 'vue-router';
import { computed } from 'vue';

const store = useUserStore();
const router = useRouter();
const showLogout = computed(() => store.user);

async function handleLogout() {
  await store.logout().then(() => {
    console.log('Logout successful!');
    return showLogin();
  });
}

async function showLogin() {
  const currentRoute = router.currentRoute.value.fullPath;
  return router.push({
    path: '/user/login',
    query: { redirect: currentRoute },
  });
}
</script>
