import mongoose from 'mongoose';

const markerSchema = new mongoose.Schema({
  lat: {type: Number, required: true},
  lon: {type: Number, required: true},
});

export function createMarkerModel(client) {
  return client.model('Marker', markerSchema);
}
