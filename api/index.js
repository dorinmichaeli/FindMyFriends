import express from 'express';
import {loadAppConfig} from './lib/tools/config-loader.js';
import {connectToMongo} from './lib/tools/connect-mongo.js';
import {createMarkerModel} from './lib/models/marker.model.js';
import {notFound404} from './lib/handler/notFound404.handler.js';
import {internalError500} from './lib/handler/internalError500.handler.js';
import {createMarkerRouter} from './lib/routers/marker.router.js';

const MAP_LORD_API_PORT = 3000;

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

  const app = express();

  app.use(createMarkerRouter({markerModel}));
  app.use(notFound404);
  app.use(internalError500);

  const server = app.listen(MAP_LORD_API_PORT, () => {
    const address = JSON.stringify(server.address());
    console.log(`Map Lord API listening on ${address}`);
  });
}

