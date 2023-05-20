import express from 'express';
import {asyncHandler} from '../tools/async-handler.js';
import {createUserAuthMiddleware} from '../middleware/userAuth.middleware.js';

export function createMarkerRouter({markerModel, userAuthService}) {
  async function createMarkerHandler(req, res) {
    const markerOptions = req.body;

    const userEmail = req.userInfo.email;
    const createStatus = await markerModel.create({
      owner: userEmail,
      lat: markerOptions.lat,
      lon: markerOptions.lon,
    });

    const markerId = createStatus._id.toString();

    res.json({
      id: markerId,
      owner: createStatus._doc.owner,
      lat: createStatus._doc.lat,
      lon: createStatus._doc.lon,
    });
  }

  async function listAllMarkersHandler(req, res) {
    const markerList = await markerModel.find();
    const sanitizedList = markerList.map(marker => {
      return {
        id: marker._id,
        owner: marker.owner,
        lat: marker.lat,
        lon: marker.lon,
      };
    });
    res.json(sanitizedList);
  }

  async function deleteMarkerHandler(req, res) {
    const markerOptions = req.body;
    const deleteResult = await markerModel.deleteOne({
      _id: markerOptions.id,
    });
    res.json({
      deleted: (deleteResult.deletedCount > 0),
    });
  }

  const router = express.Router();
  router.use(createUserAuthMiddleware({userAuthService}));
  router.get('/list-all-markers', asyncHandler(listAllMarkersHandler));
  router.post('/delete-marker', express.json(), asyncHandler(deleteMarkerHandler));
  router.post('/create-marker', express.json(), asyncHandler(createMarkerHandler));
  return router;
}
