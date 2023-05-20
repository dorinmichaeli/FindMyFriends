import express from 'express';
import {asyncHandler} from '../tools/async-handler.js';

export function createMarkerRouter({markerModel}) {
  async function createMarkerHandler(req, res) {
    const markerOptions = req.body;
    const createStatus = await markerModel.create({
      label: new Date().toISOString(),
      lat: markerOptions.lat,
      lon: markerOptions.lon,
    });
    const markerId = createStatus._id.toString();
    res.json({
      id: markerId,
      label: createStatus._doc.label,
      lat: createStatus._doc.lat,
      lon: createStatus._doc.lon,
    });
  }

  async function listAllMarkersHandler(req, res) {
    const markerList = await markerModel.find();
    const sanitizedList = markerList.map(marker => {
      return {
        id: marker._id,
        label: marker.label,
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
  router.get('/list-all-markers', asyncHandler(listAllMarkersHandler));
  router.post('/delete-marker', express.json(), asyncHandler(deleteMarkerHandler));
  router.post('/create-marker', express.json(), asyncHandler(createMarkerHandler));
  return router;
}
