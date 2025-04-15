import { defineStore } from 'pinia';
import { login } from '../services/userService';

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
        (response) =>
          (this.user = {
            email: response.email,
            token: response.token,
          }),
      );
    },

    async logout() {
      await new Promise((resolve) => {
        this.user = null;
        resolve(null);
      });
    },
  },
  persist: true,
});
