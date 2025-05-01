<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar class="bg-primary">
        <q-toolbar-title style="padding: 10px 0 10px 0; height: 65px;">
          <img src="~assets/logo.svg" alt="Logo" style="height: 100%; cursor: pointer" @click="router.push(showLogout ? '/welcome' : '/')"/>
        </q-toolbar-title>
        <q-separator v-show="showLogout" dark vertical />
        <q-btn-dropdown v-show="showLogout" stretch flat label="Menu" dropdown-icon="expand_more">
          <div class="row no-wrap q-pa-md">
            <div class="column">
              <q-list>
                <q-item clickable v-close-popup @click="router.push('/welcome')">
                  <q-item-section>
                    <q-item-label>Welcome</q-item-label>
                    <q-item-label caption>Welcome page.</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="router.push('/docs')">
                  <q-item-section>
                    <q-item-label>Documentation</q-item-label>
                    <q-item-label caption>API documentation with examples.</q-item-label>
                  </q-item-section>
                </q-item>
                <q-item clickable v-close-popup @click="router.push('/settings/api-keys')">
                  <q-item-section>
                    <q-item-label>API Keys</q-item-label>
                    <q-item-label caption>Manage your keys for accessing the API.</q-item-label>
                  </q-item-section>
                </q-item>
              </q-list>
            </div>
            <q-separator vertical inset class="q-mx-lg" />
            <div class="column items-center">
              <q-btn color="primary" @click="handleLogout">Logout</q-btn>
            </div>
          </div>
        </q-btn-dropdown>
        <q-separator v-show="showLogout" dark vertical />
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
    return router.push('/user/login');
  });
}
</script>
