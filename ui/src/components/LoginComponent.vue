<template>
  <q-page class="flex flex-center bg-grey-2" style="width: 100vw;">
    <q-card class="q-pa-md shadow-2 login-card" bordered>
      <q-card-section class="text-center">
        <div v-if="props.isNewUser" class="text-grey-9 text-h5 text-weight-bold">Sign up</div>
        <div v-else class="text-grey-9 text-h5 text-weight-bold">Sign in</div>
        <div v-if="props.isNewUser" class="text-grey-8">Sign up below to register a new account</div>
        <div v-else class="text-grey-8">Sign in below to access your account</div>
      </q-card-section>
      <q-card-section>
        <q-input dense outlined v-model="email" label="Email Address" :rules="emailRules"></q-input>
        <q-input dense outlined class="q-mt-md" v-model="password" type="password" label="Password" :rules="passwordRules"></q-input>
      </q-card-section>
      <q-card-section>
        <q-btn v-if="props.isNewUser" @click="handleRegister" style="border-radius: 8px;" color="dark" rounded size="md" label="Sign up" no-caps class="full-width"></q-btn>
        <q-btn v-else @click="handleLogin" style="border-radius: 8px;" color="dark" rounded size="md" label="Sign in" no-caps class="full-width"></q-btn>
      </q-card-section>
      <q-card-section class="text-center q-pt-none">
        <div v-if="props.isNewUser" class="text-grey-8">Have an account already?
          <a @click="$router.push('login')" class="text-dark text-weight-bold" style="text-decoration: none">Sign in.</a>
        </div>
        <div v-else class="text-grey-8">Don't have an account yet?
          <a @click="$router.push('register')" class="text-dark text-weight-bold" style="text-decoration: none">Sign up.</a>
        </div>
      </q-card-section>
    </q-card>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { login, register } from '../services/userService';
import { useUserStore } from 'stores/user';
import { useQuasar } from 'quasar'
import { patterns } from 'quasar'

const props = defineProps({
  isNewUser: Boolean
})
const email = ref('');
const password = ref('');
const store = useUserStore();
const router = useRouter();
const $q = useQuasar()
const { testPattern } = patterns
const emailRules = [
  (val: string) => (val && val.length > 0 && testPattern.email(val)) || 'Please enter a valid email address'
]
const passwordRules = [
  (val: string) => (val && val.length >= 8) || 'Please enter a password with at least 8 characters'
]

async function handleRegister() {
  try {
    const response = await register(email.value, password.value);
    console.log('Register successful!', response);
    $q.notify({
      type: 'positive',
      message: 'Your email has been registered.'
    })
    await router.push('login');
  } catch (error) {
    console.error('Register failed!', error);
    $q.notify({
      type: 'negative',
      message: 'Your email is already registered.'
    })
  }
}

async function handleLogin() {
  try {
    const response = await login(email.value, password.value);
    console.log('Login successful!', response);
    store.setToken(response.token);
    await router.push('/dashboard');
  } catch (error) {
    console.error('Login failed!', error);
    $q.notify({
      type: 'negative',
      message: 'Your email or password is incorrect.'
    })
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
