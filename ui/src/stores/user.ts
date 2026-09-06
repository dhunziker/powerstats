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
      await login(email, password).then(
        (response) => {
          identifyLaunchDarklyUser(response.data.email);
          this.user = {
            email: response.data.email,
            token: response.data.token,
          };
        }
      );
    },

    async logout() {
      this.user = null;
      identifyLaunchDarklyAnonymous();
    },

    async activate(token: string) {
      await activate(token).then((response) => {
        identifyLaunchDarklyUser(response.data.email);
        this.user = {
          email: response.data.email,
          token: response.data.token,
        };
      });
    },
  },
  persist: true,
});
