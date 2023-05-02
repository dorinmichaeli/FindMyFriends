import express from 'express';
import {loadAppConfig} from './lib/tools/config-loader.js';
import {connectToMongo} from './lib/tools/connect-mongo.js';
import {createMarkerModel} from './lib/models/marker.model.js';
import {asyncHandler} from './lib/tools/async-handler.js';

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

  app.get('/healthcheck', (req, res) => {
    res.end('Healthy!');
  });

  app.post('/create-marker', express.json(), asyncHandler(async (req, res) => {
    const markerOptions = req.body;
    const createStatus = await markerModel.create({
      lat: markerOptions.lat,
      lon: markerOptions.lon,
    });
    const markerId = createStatus._id.toString();
    res.json({
      id: markerId,
      lat: createStatus._doc.lat,
      lon: createStatus._doc.lon,
    });
  }));

  app.get('/list-all-markers', asyncHandler(async (req, res) => {
    const markerList = await markerModel.find();
    const sanitizedList = markerList.map(marker => {
      return {
        id: marker._id,
        lat: marker.lat,
        lon: marker.lon,
      };
    });
    res.json(sanitizedList);
  }));

  app.post('/delete-marker', express.json(), asyncHandler(async (req, res) => {
    const markerOptions = req.body;
    const deleteResult = await markerModel.deleteOne({
      _id: markerOptions.id,
    });
    res.json({
      deleted: (deleteResult.deletedCount > 0),
    });
  }));

  app.use((req, res) => {
    res.status(404).end('404 not found');
  });

  app.use((err, req, res, next) => {
    console.error('Uncaught error in one of the route handlers:', err);
    res.status(500).end('500 internal server error');
  });

  const server = app.listen(MAP_LORD_API_PORT, () => {
    const address = JSON.stringify(server.address());
    console.log(`Map Lord API listening on ${address}`);
  });
}

