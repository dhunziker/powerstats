import { defineBoot } from '#q-app/wrappers'
import { version } from '../../package.json';
import { H } from 'highlight.run';

export default defineBoot( (/* { app, router, ... } */) => {
  const environment = process.env.NODE_ENV || 'development';
  H.init('odz3k13e', {
    environment: environment,
    version: version,
    networkRecording: {
      enabled: true,
      recordHeadersAndBody: true,
      urlBlocklist: [
        // insert full or partial urls that you don't want to record here
        // Out of the box, Highlight will not record these URLs (they can be safely removed):
        "https://www.googleapis.com/identitytoolkit",
        "https://securetoken.googleapis.com",
      ],
    },
  });
})
