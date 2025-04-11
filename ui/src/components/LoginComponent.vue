<template>
  <q-page class="flex flex-center bg-grey-2" style="width: 100vw;">
    <q-card class="q-pa-md shadow-2 login-card" bordered>
      <q-card-section class="text-center">
        <div class="text-grey-9 text-h5 text-weight-bold">Sign in</div>
        <div class="text-grey-8">Sign in below to access your account</div>
      </q-card-section>
      <q-card-section>
        <q-input dense outlined v-model="email" label="Email Address"></q-input>
        <q-input dense outlined class="q-mt-md" v-model="password" type="password" label="Password"></q-input>
      </q-card-section>
      <q-card-section>
        <q-btn @click="handleLogin" style="border-radius: 8px;" color="dark" rounded size="md" label="Sign in" no-caps class="full-width"></q-btn>
      </q-card-section>
      <q-card-section class="text-center q-pt-none">
        <div class="text-grey-8">Don't have an account yet?
          <a href="#" class="text-dark text-weight-bold" style="text-decoration: none">Sign up.</a></div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { login } from '../services/userService';
import { useUserStore } from 'stores/user';

const email = ref('');
const password = ref('');
const store = useUserStore();

async function handleLogin() {
  try {
    const response = await login(email.value, password.value);
    console.log('Login successful!', response);
    store.setToken(response.token)
  } catch (error) {
    console.error('Login failed!', error);
    // Handle login error (e.g., show error message)
  }
}
</script>

<style>
.login-card {
  width: 25rem;
  border-radius: 8px;
  box-shadow: 0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1);
}
</style>
