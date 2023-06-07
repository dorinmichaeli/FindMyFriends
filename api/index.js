import fs from 'node:fs';
import https from 'node:https';
import express from 'express';
import {loadAppConfig} from './lib/tools/config-loader.js';
import {connectToMongo} from './lib/tools/connect-mongo.js';
import {createMarkerModel} from './lib/models/marker.model.js';
import {notFound404} from './lib/handler/notFound404.handler.js';
import {internalError500} from './lib/handler/internalError500.handler.js';
import {createMarkerRouter} from './lib/routers/marker.router.js';
import {createUserAuthService} from './lib/services/userAuth.service.js';

const MAP_LORD_API_PORT = 3000;
const SERVER_HOSTNAME = 'maplord.api';
const SSL_CERTIFICATE_KEY = './keys/maplord.api.key';
const SSL_CERTIFICATE_CRT = './keys/maplord.api.crt';

main().catch(err => {
  console.error('Uncaught exception in main() function:', err);
});

async function main() {
  // Load the application's config.
  const config = await loadAppConfig();
  // Connect to the application's database.
  const client = await connectToMongo(config);
  // Create the marker model.
  const markerModel = createMarkerModel(client);

  // Create the user auth service.
  const userAuthService = createUserAuthService(config);

  const app = express();

  // Register route handlers.
  app.use(createMarkerRouter({markerModel, userAuthService}));
  app.use(notFound404);
  app.use(internalError500);

  // Launch the HTTPS server.
  const sslOptions = {
    key: fs.readFileSync(SSL_CERTIFICATE_KEY),
    cert: fs.readFileSync(SSL_CERTIFICATE_CRT),
  };
  const server = https.createServer(sslOptions, app);
  server.listen(MAP_LORD_API_PORT, SERVER_HOSTNAME, () => {
    const address = JSON.stringify(server.address());
    console.log(`Map Lord API listening on ${address}`);
  });
}

