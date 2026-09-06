import { defineBoot } from '#q-app/wrappers';
import { useUserStore } from 'stores/user';
import { identifyLaunchDarklyUser } from '../services/launchdarkly';

export default defineBoot(async ({ store }) => {
  const userStore = useUserStore(store);
  if (userStore.user?.email) {
    await identifyLaunchDarklyUser(userStore.user.email);
  }
});
