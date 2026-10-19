# DataCare HMS - Android app

Mobile companion to the DataCare Hospital Management System web app (Netcare Alberton Hospital).
Kotlin + Jetpack Compose, using the **same Firebase project** (`datacare-hms`) as the website, so users and records are shared.

## Open it in Android Studio
1. Install **Android Studio Ladybug (2024.2)** or newer.
2. `File > Open` and choose this **DataCare-HMS-Android** folder. Wait for Gradle sync.
3. Plug in a real phone (Settings > About phone > tap *Build number* 7 times > Developer options > **USB debugging**), pick it in the device list and press **Run**.
   The demo video must be recorded on a real phone, not an emulator.

Requirements: JDK 17 (bundled with Android Studio), Android 7.0+ (API 24+) phone.

## Splash, onboarding and look
- **Splash screen:** animated DataCare logo (2.4 s) on every launch.
- **Onboarding:** 4 swipeable pages (Welcome, Appointments, Prescriptions, Billing & Privacy) shown once, with Skip / Back / Next / Get Started.
  Re-watch it any time: More > Settings > **Replay welcome tour** (useful for the demo video).
- **Your own background pictures:** replace the four files in `app/src/main/res/drawable-nodpi/`
  (`onboarding_1.jpg` ... `onboarding_4.jpg`) with your photos, **keeping the same names** (portrait, ~1080x1920, under 500 KB each).
  If your photos are PNG, delete the matching .jpg first (two files with the same name break the build).
  The login header also uses `onboarding_1`. A dark overlay keeps the text readable on any photo.

## Firebase set-up (one time, ~10 minutes)
The app already contains the web app's Firebase config (`FirebaseConfig.kt`), so email/password login and Firestore work as soon as you run it. To finish the Task 2 requirements:

1. **Email/Password sign-in** - Firebase console > Authentication > Sign-in method > enable *Email/Password* (already on if the website login works).
2. **Single sign-on (Google)**
   - Enable *Google* in Sign-in method and copy the **Web client ID** (Web SDK configuration).
   - Paste it into `FirebaseConfig.GOOGLE_WEB_CLIENT_ID`.
   - Project settings > Your apps > **Add app > Android**, package name `com.datacare.hms`, add the **SHA-1** of your debug key
     (Android Studio: Gradle panel > `app > Tasks > android > signingReport`). **Every team member who runs the app needs their own SHA-1 added.**
   - Download `google-services.json` into `android/app/`. The build detects it automatically (it also fixes API-key restrictions if you have any).
   - Until the Web client ID is set, the Google button simply shows a friendly "not configured" message; nothing crashes.
See `docs/FIREBASE_SETUP.md` for the full step-by-step Firebase guide.

3. **Firestore rules** - review `firestore.rules` and publish it (Firestore Database > Rules). It requires sign-in and stops users from changing their own role.
4. **Support email** - set `FirebaseConfig.SUPPORT_EMAIL` to the real IT-support address.

## Demo data (at least ten records per table)
Log in as a **System Administrator** > More > Settings > **Load Demo Data**.
This writes 12 patients, 12 appointments, 12 prescriptions, 12 invoices and 10 departments, 10 staff, 10 inventory items, 10 medical records.
It uses fixed document IDs, so pressing it twice never duplicates anything. The data is defined in `data/SeedData.kt` and checked by a unit test.

## What matches the website
| Website (HTML/CSS/JS) | Android app |
|---|---|
| Login / Create Account, 8 roles, password >= 6 | `ui/AuthScreens.kt`, same fields and copy, same `users` collection |
| `rolePermissions` in `js/main.js` | `data/Roles.kt` - identical permission map; bottom bar and screens follow it |
| Dashboard: 4 stat cards, quick actions, appointment chart | `ui/screens/Dashboard.kt` |
| Services page (6 services) | `ui/screens/Services.kt` |
| Patient page (add + search + list) | `ui/screens/Patients.kt` (fields: patientID, firstName, lastName, gender, dateOfBirth, phone, address, medicalHistory, emergencyContact) |
| Appointments (ID, patient, doctor, date, time, type, ward, status) | `ui/screens/Appointments.kt` + Reschedule / Cancel |
| Prescriptions (ID, patientID, doctorID, medication, dosage, notes, date, status) | `ui/screens/Prescriptions.kt` |
| Billing (invoice no., fees, medical aid, method, outstanding, status) | `ui/screens/Billing.kt` + Make Payment, Claims and Payment History views |
| Reports (patients, appointments, prescriptions, revenue + charts) | `ui/screens/Reports.kt` (Patient Statistics / Operational / Financial) |
| Brand colours `#0878D1`, `#15956B`, `#D84A4A`, `#7858BF`; DataCare logo | `ui/theme/Theme.kt`, `res/drawable-nodpi/datacare_logo.png` |

Firestore collections: `users, patients, appointments, prescriptions, billing` (written by the website) plus `departments, staff, inventory, medicalRecords` (ERD entities). Field names are identical, so a record created on the website shows on the phone and vice-versa.

## Task 2 checklist
| Requirement | Where |
|---|---|
| Registration + login, password encrypted | Firebase Authentication hashes/salts passwords; the app never stores them |
| Single sign-on | "Continue with Google" (Credential Manager -> Firebase) |
| Settings screen | More > Settings: theme, text size, alerts, change password, profile |
| Handles invalid input without crashing | `util/Validators.kt` on every form, field-level error messages, try/catch around every Firebase call (`AppViewModel.launchAction`) |
| Android code, GUI, input controls, modules | `app/src/main/java/com/datacare/hms/` |
| DBMS with >= 10 records per table | Cloud Firestore + `SeedData.kt`; schema in `docs/DATABASE.md` |
| Verification and testing | `app/src/test` (26 unit tests) + manual cases in `docs/TESTING.md` |
| Help system + user documentation | In-app Help & Support (FAQ), `docs/USER_GUIDE.md` |
| GitHub Actions: automated tests + build | `.github/workflows/android.yml` |
| Demo video (real phone, voice-over, show Auth/API/DB) | See "Recording the demo" below |

## Automated tests and CI
- Local: `./gradlew testDebugUnitTest` (Windows: `gradlew.bat testDebugUnitTest`).
- GitHub: push the repo; the workflow runs the unit tests, builds the debug APK and uploads both the test report and the APK as artifacts.
- Keep your Azure DevOps board updated (user stories per screen, tasks, test cases) - that part lives outside the code.

## Recording the demo (checklist)
1. Record the phone screen with voice-over; show every screen, a validation error (e.g. bad email), Settings changes and Logout.
2. Register a new user, then show it in **Firebase console > Authentication** (the "online-hosted authentication service").
3. Create an appointment / prescription / invoice on the phone, then show it appear in **Firestore** (the database). Add one on the website and show it on the phone.
4. The "API" is Firebase's REST/SDK backend: show the project's *Usage* tab or the Network traffic to `firestore.googleapis.com` / `identitytoolkit.googleapis.com`, and say so in the voice-over.

## Known limitations (be upfront about these in the report)
- The Task 1 sitemap lists **Admission** and **Discharge** under Patients, but the website has no such feature or fields, so they are not in the app. Add `admissionStatus` fields to both platforms if the lecturer expects them.
- The website lets every role with access see all records; the app does the same. Patient-only filtering ("My ...") needs a link between a login and a patient ID, which the current data model does not have.
- Alerts are derived from live data (upcoming appointments, unpaid invoices); there is no push-notification service yet.
- Do not ship real patient data in this prototype; use the simulated data (POPIA).
