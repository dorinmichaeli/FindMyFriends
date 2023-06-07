# TODO

## Tasks

- Add HTTPS to the API.

- Think about how we can emulate/simulate multiple devices for field testing. Do we need to get
  multiple old Android devices or something?
- Add live communication between server and client to let the server know when a new marker has been
  added by another user in their group so that the user can immediately add the marker locally.

- (DONE) Add error handling to the app.
- (DONE) Organize the code in the app.
- (DONE) Add a loader to the app.
- (DONE) Add user authentication via Firebase. (Both in the app and the API.)
- (DONE) Marker ownership. A user should be able to see the name/id of the user that
  created a specific marker. For example each marker should have a name above it, or a unique
  per-user color.


## Secondary tasks

- Implement login via google.
- Fix back navigation from MapboxFragment not working.
- Find out how to change the app's name and icon in the apps view in the device.

- (Done) Make HTTPS with self-signed certificates work during development.

## Future

- Implement user groups. A user should only see the markers created by the users in the same group
  as them.
- See list of users in your group.
- Chat room with other users in your group.
- See live locations of all users in your group.

## Research

- (DONE) Find out how to perform dependency injection on fragments without throwing everything on
  the MainActivity class.
- Find out what are *-BoM versions of libraries (e.g. Firebase and JUnit).

## Links of possible interest

- https://developer.android.com/codelabs/nearby-connections#0
