import { defineStore } from 'pinia';

export const useUserStore = defineStore('userStore', {
  state: () => ({
    token: ''
  }),
  getters: {
    // token: (state) => state.token
  },
  actions: {
    setToken(token: string) {
      this.token = token;
    }
  }
});
