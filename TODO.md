# TODO

## Tasks

- (DONE) Create group functionality. A user should be able to create a group and let other users join it by
  giving them the id.
- (DONE) Add group template for social events. A group template should contain predetermined markers set by
  the creator of the template. When creating a group from a template, the markers should be added
  to the group. These markers will have a different color, and no user will be able to remove them.

- (DONE) Make a user's own markers a different color.
- (DONE) Add live communication between server and client to let the server know when a new marker
  has been
  added by another user in their group so that the user can immediately add the marker locally.
- (DONE) Add HTTPS to the API.
- (DONE) Add error handling to the app.
- (DONE) Organize the code in the app.
- (DONE) Add a loader to the app.
- (DONE) Add user authentication via Firebase. (Both in the app and the API.)
- (DONE) Marker ownership. A user should be able to see the name/id of the user that
  created a specific marker. For example each marker should have a name above it, or a unique
  per-user color.
- (DONE) Ask the user for permission to use their location in the PermissionsActivity, rather than
  force the user to add the permission manually in the settings.
- (DONE) Fix every tab in MainActivity missing events, until that tab is visited for the first time
  by the user.

## Secondary tasks

- (DONE) Find out how to change the app's name and icon in the apps view in the device.
- (DONE) Think about how we can emulate/simulate multiple devices for field testing. Do we need to get
  multiple old Android devices or something?

- (N/A) Fix back navigation from MapboxFragment not working.
- (Done) Make HTTPS with self-signed certificates work during development.
- (Done) Implement login via google.

## Future

- (DONE) Implement user groups. A user should only see the markers created by the users in the same
  group
  as them.
- (DONE) See list of users in your group.
- (DONE) Chat room with other users in your group.
- See live locations of all users in your group.

## Low priority

- When trying to log in via Google and the user is not logged in in the device, let the user know
  what happened instead of showing an obscure error.

## Research

- (DONE) Find out how to perform dependency injection on fragments without throwing everything on
  the MainActivity class.
- (DONE) Find out what are *-BoM versions of libraries (e.g. Firebase and JUnit).

## Links of possible interest

- https://developer.android.com/codelabs/nearby-connections#0

## Final Touches

- (DONE) Get rid of the MANAGE ACCOUNT button.