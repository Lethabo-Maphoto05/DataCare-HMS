# How to link the DataCare HMS Android app with Firebase

The app is **already pre-linked** to the website's Firebase project (`datacare-hms`) through `FirebaseConfig.kt`, so email/password login and the database work straight away. Follow these steps to finish the link properly (Android registration, Google single sign-on, database rules).

Useful facts for this project:
- Package name: **com.datacare.hms**
- Firebase project: **datacare-hms** (same as the website)
- Config file: `app/src/main/java/com/datacare/hms/FirebaseConfig.kt`

---

## Part A - Open the Firebase project
1. Go to https://console.firebase.google.com and sign in with the Google account that owns the website's project.
2. Click the project **datacare-hms**.
   (If you want a brand-new project instead, see Part G.)

## Part B - Register the Android app
1. On the project overview page click the **Android icon** (or Project settings > General > **Add app > Android**).
2. **Android package name:** `com.datacare.hms` (must be exactly this).
3. **App nickname:** DataCare HMS Android (optional).
4. **Debug signing certificate SHA-1:** get it from Android Studio:
   - Open the project, click the **Gradle** elephant tab on the right.
   - Open `app > Tasks > android > signingReport` (double-click).
   - In the Run panel find **Variant: debug** and copy the **SHA1** line (looks like `A1:B2:C3:...`).
   - Paste it into the Firebase box.
5. Click **Register app**.
6. Click **Download google-services.json**.
7. Copy that file into the project's **`app/`** folder, next to `app/build.gradle.kts` (path: `DataCare-HMS-Android/app/google-services.json`).
8. Skip Firebase's "Add Firebase SDK" instructions - **they are already done** in this project's Gradle files. Click Next / Continue to console.
9. In Android Studio click **File > Sync Project with Gradle Files**. The build detects the file automatically and switches on the Google Services plugin.

> Each team member running the app from their own computer has a different debug SHA-1. Add every member's SHA-1 in Project settings > Your apps > com.datacare.hms > **Add fingerprint**.

## Part C - Turn on Authentication
1. Left menu: **Build > Authentication** > **Get started** (if shown).
2. Tab **Sign-in method**.
3. Click **Email/Password** > switch **Enable** on > Save.
   (This is what Create Account and Login use. Firebase stores only a salted hash of each password.)

## Part D - Turn on Google single sign-on (SSO)
1. In **Sign-in method** click **Google** > **Enable**.
2. Choose a **Project support email** > Save.
3. Open the Google provider again and expand **Web SDK configuration**. Copy the **Web client ID** (ends with `.apps.googleusercontent.com`).
4. In Android Studio open `app/src/main/java/com/datacare/hms/FirebaseConfig.kt` and paste it:
   ```kotlin
   const val GOOGLE_WEB_CLIENT_ID = "123456789-abcdefg.apps.googleusercontent.com"
   ```
5. Make sure your phone has a Google account added (Settings > Accounts).
6. Re-download `google-services.json` if you added the SHA-1 *after* Part B, and replace the old one.

## Part E - Set up the database (Cloud Firestore)
1. Left menu: **Build > Firestore Database**. If it isn't created, click **Create database**, choose a location (e.g. `africa-south1` Johannesburg if offered, otherwise the closest), start in **production mode**.
   (The website's project normally has this already.)
2. Open the **Rules** tab, replace everything with the contents of **`firestore.rules`** from this project, then click **Publish**.
   These rules require login, and stop people changing their own role.
3. The collections (`users, patients, appointments, prescriptions, billing, departments, staff, inventory, medicalRecords`) are created automatically when the app writes to them.

## Part F - Test the link
1. Run the app on your phone (green Run button).
2. **Create Account** with a role of **System Administrator** > then Login.
3. Check Firebase console > **Authentication > Users**: your new user must appear.
4. Check **Firestore Database > Data > users**: a document for that user must appear with the chosen role.
5. In the app go to **More > Settings > Load Demo Data**. Check Firestore: `patients`, `appointments`, `prescriptions`, `billing`, `departments`, `staff`, `inventory`, `medicalRecords` each now have 10+ documents.
6. Add a patient on the phone, then open the website's Patients page - it should show the same record (and the other way round).

These screens (Authentication users and Firestore data) are what the Task 2 demo video must show.

## Part G - Optional: use your own new Firebase project instead
Use this only if you don't want to share the website's project.
1. Firebase console > **Add project** > name it > create.
2. Do Parts B to E in the new project.
3. Open **Project settings > General**, scroll to *Your apps* and copy: API key, project ID, sender ID, storage bucket. Put them in `FirebaseConfig.kt`:
   ```kotlin
   const val API_KEY = "..."
   const val PROJECT_ID = "..."
   const val SENDER_ID = "..."
   const val STORAGE_BUCKET = "..."
   const val APPLICATION_ID = "1:...:android:..."   // from the Android app entry
   ```
   (If `google-services.json` is in `app/`, it overrides these automatically.)
4. The website would then use a different database, so also update `js/firebase.js` if you want both to share it.

## Troubleshooting
| Problem | Fix |
|---|---|
| "Invalid email or password" for a correct login | Email/Password not enabled (Part C), or wrong account |
| "You do not have permission to do that" | Firestore rules too strict/not published, or you aren't logged in (Part E) |
| Google button says "not configured" | Web client ID not pasted (Part D step 4) |
| Google sign-in fails / dialog closes | SHA-1 missing in Firebase, or no Google account on the phone (Parts B, D) |
| "Network error" | Phone has no internet |
| Build error about `google-services.json` | File is in the wrong folder - it must be in `app/`, not the project root |
| Data doesn't appear | Check you opened the same Firebase project (`datacare-hms`) and look in Firestore > Data |
