# Developer notes

## Android Studio

### Importing image assets

[Link](https://stackoverflow.com/a/57330638)

### Shortcuts: Code editing

Find text in all files: `Ctrl + Shift + F`  
Find file by name: `Ctrl + Shift + N`  
Move to tab on the right: `Alt + Right Arrow`  
Move to tab on the left: `Alt + Left Arrow`  
Add cursor to next occurrence of selected text: `Alt + J`
Add cursors to all occurrences of selected text: `Ctrl + Shift + Alt + J`

### Shortcuts: Run/debug

Run: `Shift + F10`
Debug: `Shift + F9`

## Using Kotlin APIs in Java code

Mapbox examples are usually in Kotlin because Mapbox itself is written in Kotlin, and as such we
sometimes run into issues when trying to convert the Kotlin code example to Java for our use.

### Extension methods

Sometimes we'll see examples of method calls in Kotlin code that when we try to write in Java we
find out that the methods don't exist. This is because Kotlin has a concept of extension methods,
which are methods that are defined outside of the class they are called on.

For example [here](https://docs.mapbox.com/android/maps/examples/default-point-annotation/) on this
line:

```kotlin
val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
```

The `createPointAnnotationManager` method is not defined on the `AnnotationApi` class, and the
equivalent code in Java would be:

```java
PointAnnotationManager pointAnnotationManager=PointAnnotationManagerKt
        .createPointAnnotationManager(annotationApi,(AnnotationConfig)null);
```

[Link](https://stackoverflow.com/a/28364983)

## Interesting things

- The `Snackbar` class allows us to make messages pop up from the bottom of the screen. It's
  similar to a `Toast` but it's more customizable and can be dismissed by the user.
- The `Toast` class allows us to make messages pop up from the bottom of the screen. It's similar
  to a `Snackbar` but it's less customizable and can't be dismissed by the user. (?)

## Mapbox stuff

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

[Link](https://docs.mapbox.com/android/maps/guides/#mapbox-android-plugin


## Firebase issues

### com.google.android.gms.common.api.UnsupportedApiCallException: Missing Feature{name=auth_api_credentials_begin_sign_in, version=8}

- Removing the SHA1 fingerprint from the Firebase console and adding it again may fix the issue.
- Using a different emulated device altogether may also fix the issue. Wiping the data of an existing one that shows this buy DOESN'T FIX THE ISSUE.
- Clean, Rebuild, wipe Gradle global cache, sync gradle files - none of these seem to help much.
