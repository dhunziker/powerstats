<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar class="bg-primary text-white">
        <q-avatar>
          <img src="~assets/logo.png" alt="Logo" />
        </q-avatar>
        <q-toolbar-title>PowerStats</q-toolbar-title>
        <q-btn @click="handleLogout" v-show="showLogout">Logout</q-btn>
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

const showLogout = computed(() => store.user)

async function handleLogout() {
  await store.logout().then(() => {
    console.log('Logout successful!');
    return router.push('/user/login');
  });
}
</script>
