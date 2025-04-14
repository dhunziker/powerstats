import { defineStore } from 'pinia';
import { login, logout } from '../services/userService';
import { api } from 'boot/axios';

interface User {
  email: string;
  token: string;
}

export const useUserStore = defineStore('userStore', {
  state: () => ({
    user: null as User | null,
  }),
  getters: {},
  actions: {
    async login(email: string, password: string) {
      await login(email, password)
        .then(
          (response) =>
            (this.user = {
              email: response.email,
              token: response.token,
            }),
        )
        .then((user) => (api.defaults.headers.common['Authorization'] = 'Bearer ' + user.token));
    },

    async logout() {
      await logout(this.user!.email).then(() => {
        this.user = null;
        api.defaults.headers.common['Authorization'] = null;
      });
    },
  },
});
