package com.datacare.hms

/**
 * Firebase settings copied from the DataCare web app (js/firebase.js) so the Android app
 * talks to the SAME Firebase project: same users, same Firestore collections.
 *
 * If you register an Android app in the Firebase console and add app/google-services.json,
 * that file takes over automatically and these values are ignored.
 */
object FirebaseConfig {
    const val API_KEY = "AIzaSyCrwObDZLNWKK470DGG7s3RCj192OWTcLc"
    const val PROJECT_ID = "datacare-hms-8fefe"
    const val APPLICATION_ID = "1:386921283076:android:4b711c19c044908ec87f51"
    const val STORAGE_BUCKET = "datacare-hms-8fefe.firebasestorage.app"
    const val SENDER_ID = "386921283076"

    /**
     * OAuth *Web* client ID used for Google single sign-on.
     * Firebase console -> Authentication -> Sign-in method -> Google -> Web SDK configuration.
     * Leave blank to disable the Google button gracefully.
     */
    const val GOOGLE_WEB_CLIENT_ID = "386921283076-fhsaftlmdsk127ufatasnbbvldhbkacc.apps.googleusercontent.com"

    /** Shown in the Help screen ("Contact IT Support"). Replace with the real address. */
    const val SUPPORT_EMAIL = "mudau5090@gmail.com"
}
