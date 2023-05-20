# TODO

## Tasks

- (DONE) Add error handling to the app.
- (DONE) Organize the code in the app.
- (DONE) Add a loader to the app.
- (Partially done) Add user authentication via Firebase. (Both in the app and the API.)
- (Partially done) Marker ownership. A user should be able to see the name/id of the user that
  created a specific marker. For example each marker should have a name above it, or a unique
  per-user color.

## Future

- Add live communication between server and client to let the server know when a new marker has been
  added by another user in their group so that the user can immediately add the marker locally.
- Implement user groups. A user should only see the markers created by the users in the same group
  as them.
- See list of users in your group.
- Chat room with other users in your group.
- See live locations of all users in your group.
- Think about how we can emulate/simulate multiple devices for field testing. Do we need to get
  multiple old Android devices or something?

## Bugs

- Navigating backwards from the MapboxFragment takes you back to the LoaderFragment, which then
  tries yet again to take you back to the MapboxFragment. The "navigate back" button should be
  overridden to take the user back to FirstFragment or SecondFragment.

## Research

- Find out how to change the app's name and icon in the apps view in the device.
- Find out if we really need both COARSE_LOCATION and FINE_LOCATION permissions in the manifest
  file. Android Studio says we need both, currently we're using only FINE_LOCATION and it seems to
  work.
- Find out how to perform dependency injection on fragments without throwing everything on the
  MainActivity class.
- Find out what are *-BoM versions of libraries (e.g. Firebase and JUnit).
