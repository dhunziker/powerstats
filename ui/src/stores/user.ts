import { defineStore } from 'pinia';
import { activate, login } from '../services/userService';
import {
  identifyLaunchDarklyAnonymous,
  identifyLaunchDarklyUser,
} from '../services/launchdarkly';

interface User {
  email: string;
  token: string;
}

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null as User | null,
  }),
  getters: {},
  actions: {
    async login(email: string, password: string) {
      const response = await login(email, password);
      await identifyLaunchDarklyUser(response.data.email);
      this.user = {
        email: response.data.email,
        token: response.data.token,
      };
    },

    async logout() {
      this.user = null;
      await identifyLaunchDarklyAnonymous();
    },

    async activate(token: string) {
      const response = await activate(token);
      await identifyLaunchDarklyUser(response.data.email);
      this.user = {
        email: response.data.email,
        token: response.data.token,
      };
    },
  },
  persist: true,
});
