import { initialize, type LDClient, type LDContext } from 'launchdarkly-js-client-sdk';
import Observability from '@launchdarkly/observability';
import SessionReplay from '@launchdarkly/session-replay';
import pkg from '../../package.json';

const CLIENT_SIDE_ID = process.env.LAUNCHDARKLY_CLIENT_ID as string;
const environment = process.env.NODE_ENV || 'development';

const anonymousContext: LDContext = {
  kind: 'user',
  anonymous: true,
};

export const ldClient: LDClient = initialize(CLIENT_SIDE_ID, anonymousContext, {
  application: { id: 'powerstats-ui', version: pkg.version },
  plugins: [
    new Observability({
      serviceName: 'powerstats-ui',
      environment,
      version: pkg.version,
      networkRecording: {
        enabled: true,
        recordHeadersAndBody: true,
        urlBlocklist: [
          'https://www.googleapis.com/identitytoolkit',
          'https://securetoken.googleapis.com',
        ],
      },
    }),
    new SessionReplay({
      serviceName: 'powerstats-ui',
      environment,
      version: pkg.version,
      contextFriendlyName: (ctx: LDContext) =>
        'email' in ctx && typeof ctx.email === 'string' ? ctx.email : undefined,
    }),
  ],
});

async function identifyContext(context: LDContext): Promise<void> {
  await ldClient.identify(context).catch(() => undefined);
}

export async function identifyLaunchDarklyUser(email: string): Promise<void> {
  await identifyContext({ kind: 'user', key: email, email });
}

export async function identifyLaunchDarklyAnonymous(): Promise<void> {
  await identifyContext(anonymousContext);
}
