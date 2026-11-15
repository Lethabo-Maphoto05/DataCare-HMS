# DataCare HMS Android project

## Project structure
The Compose entry screen is located at:
`app/src/main/java/com/datacare/hms/ui/DataCareApp.kt`

Its package is `com.datacare.hms.ui` and it declares:
`@Composable fun DataCareApp(vm: AppViewModel)`

`MainActivity.kt` imports that exact function.

## Corrections applied to the supplied project
- Corrected the Firebase network exception import in `AppViewModel.kt` to `com.google.firebase.FirebaseNetworkException`.
- Removed the duplicate `KeyboardType` import from `DataCareApp.kt`.

## Open and build in Android Studio
1. Extract this ZIP into a new folder.
2. In Android Studio choose **File > Open** and select the extracted `DataCare-HMS-Android-Completed` root folder (the folder containing `settings.gradle.kts`).
3. Wait for Gradle sync to finish.
4. Choose **Build > Make Project**.
5. If Android Studio reports an error, copy the first Kotlin/compiler error from the Build window.

## API address
The supplied Gradle configuration uses `http://10.0.2.2:3000/api/`, which is the Android Emulator's route to port 3000 on the development PC. The Node API must be running on the PC.
For a physical phone, this emulator-only address will not work; configure an HTTPS API URL or use the PC's reachable LAN address and ensure both devices are on the same network.

## Important verification note
The source corrections above are applied, but a full Gradle build could not be run in this environment because the Gradle 8.9 distribution could not be downloaded (network/DNS unavailable). Build and run the extracted project in Android Studio to complete environment-specific verification.
