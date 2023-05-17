# Learning how to use Mapbox!

## Launching the API

Launching MongoDB and Mongo Express GUI:
```sh
cd api/mongo
docker-compose up -d
```

Launching the API:
```sh
cd api
npm ci
npm start
```

### Launching the app in Android Studio emulator


## Mapbox

### Installation guide used

https://docs.mapbox.com/android/maps/guides/install/

### Tokens

Public access token:
```
pk.eyJ1IjoiZG9yaW5taWNoYWVsaSIsImEiOiJjbGdjOTkyN2MxaDQ4M2pvNnRnZHZ5bTB3In0.MzwD06EklMmKoV2IAOH3xQ
```

Secret access token:
```
sk.eyJ1IjoiZG9yaW5taWNoYWVsaSIsImEiOiJjbGdjZDJ1Y3QwMThuM2hsZWt3MDdzYW1zIn0.fYflhkOpOYREpWdTXwP1CQ
```

### API reference:

[Link](https://docs.mapbox.com/android/maps/api/10.12.1/)

### About plugins

Whenever some code example mentions MapBox "plugins", it probably uses the old API (v9).  
In the newer API (v10) the plugins are now part of the core API.

[Link](https://docs.mapbox.com/android/maps/guides/#mapbox-android-plugins)