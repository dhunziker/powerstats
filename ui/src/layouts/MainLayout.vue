<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar class="bg-primary text-white">
        <q-avatar @click="router.push(showLogout ? '/settings' : '/')" style="cursor: pointer">
          <img src="~assets/logo.png" alt="Logo" />
        </q-avatar>
        <q-toolbar-title>Lorem Ipsum</q-toolbar-title>
        <q-space />
        <q-separator v-show="showLogout" dark vertical />
        <q-btn-dropdown v-show="showLogout" stretch flat label="Menu" dropdown-icon="expand_more">
          <div class="row no-wrap q-pa-md">
            <div class="column">
              <q-list>
                <q-item-label header>Settings</q-item-label>
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
