import { defineBoot } from '#q-app/wrappers'
import { H } from 'highlight.run';
import pkg from '../../package.json';

export default defineBoot( (/* { app, router, ... } */) => {
  const environment = process.env.NODE_ENV || 'development';
  H.init('odz3k13e', {
    environment: environment,
    version: pkg.version,
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
