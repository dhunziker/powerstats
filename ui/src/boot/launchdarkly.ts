import { defineBoot } from '#q-app/wrappers';
import { useUserStore } from 'stores/user';
import { identifyLaunchDarklyUser } from '../services/launchdarkly';

export default defineBoot(({ store }) => {
  const userStore = useUserStore(store);
  if (userStore.user?.email) {
    identifyLaunchDarklyUser(userStore.user.email);
  }
});
