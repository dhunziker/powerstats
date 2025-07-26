import { defineStore } from 'pinia';
import { activate, login } from '../services/userService';
import { H } from 'highlight.run';

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
          H.identify(response.data.email, {});
          this.user = {
            email: response.data.email,
            token: response.data.token,
          }
        }
      );
    },

    async logout() {
      await new Promise((resolve) => {
        this.user = null;
        resolve(null);
      });
    },

    async activate(token: string) {
      await activate(token).then(
        (response) =>
          (this.user = {
            email: response.data.email,
            token: response.data.token,
          })
      );
    },
  },
  persist: true,
});
