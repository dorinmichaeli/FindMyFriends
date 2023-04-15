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
PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt
        .createPointAnnotationManager(annotationApi, (AnnotationConfig) null);
```

[Link](https://stackoverflow.com/a/28364983)
