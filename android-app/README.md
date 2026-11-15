# DataCare HMS — Android (Kotlin)

Android companion for the current DataCare HMS web application. This version uses **Firebase Authentication for sign-in** and the **same Node.js / Express API as the website for all hospital records**. It does not read or write Firestore directly.

## What is included

- Firebase email/password login using the existing DataCare Firebase project.
- Patient self-registration with email verification and administrator approval.
- Administrator, doctor and patient workspaces; no public role picker.
- Role-specific navigation matching `web/main.js`:
  - **Admin:** Dashboard, Patients, Appointments, Prescriptions, Billing, Services, Reports, User Management.
  - **Doctor:** Dashboard, Services, Appointments, Prescriptions.
  - **Patient:** Dashboard, Services, Appointments, Prescriptions, Billing.
- Admin workflows: approve/reject patient registrations, add patient records, create doctor accounts, add ward beds, allocate patients, book appointments and create prescriptions/bills.
- Doctor workflows: view assigned appointments, confirm/complete appointments, issue prescriptions and create bills.
- Patient workflows: view profile, request appointments, view prescriptions/invoices and submit the same demonstration payment / medical-aid selections as the web app.
- Notifications and the IT Support dialog.

## Important architecture

Both platforms use the same Firebase Authentication project and the same API:

`Android app / Web app → Firebase ID token → Node.js API → Firebase Admin SDK → Cloud Firestore`

The API verifies Firebase ID tokens and enforces account status, role permissions and patient/doctor ownership. The Android app does not contain a Firebase service-account key and does not use the old direct-Firestore repository or demo seed data.

The Android package name is `com.datacare.hms`. The included `app/google-services.json` is configured for the existing DataCare Firebase project and this package.

## Requirements

- Android Studio (current stable version recommended)
- JDK 17 (Android Studio's bundled JDK is suitable)
- Android SDK Platform 35
- Node.js API running or deployed at a reachable HTTPS address
- Internet access on the Android device/emulator

## Run with the Android emulator

1. Open Android Studio.
2. Select **Open** and choose this `DataCare-HMS-Android` folder.
3. Wait for Gradle Sync to finish.
4. In a separate PowerShell window, start the existing DataCare API:

   ```powershell
   cd "C:\Users\Hluks\Music\DataCare-HMS_Firestore_Updated_Corrected\DataCare-HMS\api"
   npm.cmd start
   ```

5. Keep the API terminal open. The Android emulator can reach the computer's localhost through `10.0.2.2`.
6. The default API address is already set in `app/build.gradle.kts`:

   `http://10.0.2.2:3000/api/`

7. Start an Android emulator, select it in Android Studio and press **Run**.

The web app does not need to be open for Android to work. The API must be running. If you also run the website, both apps read and update the same Firestore records through that API.

## Run on a physical Android phone

The emulator address `10.0.2.2` does not work on a physical phone. Choose one of these approaches:

### Option A — USB debugging

1. Connect the phone to the computer using USB and enable USB debugging.
2. In Android Studio's Terminal, run:

   ```powershell
   adb reverse tcp:3000 tcp:3000
   ```

3. In `app/build.gradle.kts`, change `API_BASE_URL` to:

   ```kotlin
   buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:3000/api/\"")
   ```

4. Sync Gradle and run the app again. Keep the API running on the computer.

### Option B — Same Wi-Fi network

1. Connect the phone and computer to the same trusted Wi-Fi network.
2. Find the computer's local IPv4 address using `ipconfig` in PowerShell.
3. Change `API_BASE_URL` in `app/build.gradle.kts` to `http://YOUR-PC-IP:3000/api/` (replace the example with the computer's actual IPv4 address).
4. Allow Node.js through Windows Firewall on the private network if Windows asks.
5. Sync Gradle and run the app again.

For use outside your local network, deploy the API to HTTPS and set `API_BASE_URL` to the deployed `/api/` address. Do not expose the local development API directly to the public internet.

## Account workflow

- **Patients:** Create an account in the app, verify the email from the Firebase email, then sign in and complete the patient registration details. The account remains pending until an administrator approves it.
- **Doctors:** An administrator creates doctor accounts from **User Management**. The doctor signs in with the temporary password supplied by the administrator.
- **Administrators:** Use the existing administrator account already provisioned in the DataCare Firebase project. Administrator access is not available through public registration.

The app calls `/api/auth/me` after sign-in. If the API says the account is pending or disabled, the app does not open the workspace.

## Build the app

From Android Studio use **Build → Make Project**. To create a debug APK, use **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

Or from the project root on Windows:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

The debug APK is normally created at `app/build/outputs/apk/debug/app-debug.apk`.

## Notes and safety

- Online Demo payment is a demonstration record only. It does not charge a bank card or transfer money.
- Use fictional patient details for testing. Do not load real patient medical information into this academic prototype.
- Keep the API service-account key and API `.env` file only on the backend computer/server. Never copy them into this Android project or upload them to GitHub.
- The Android app allows cleartext HTTP for local development. Use an HTTPS API URL for any deployed or public environment.
- The Android screens refresh from the API after actions and when the refresh button is pressed. This provides shared data; it is not a Firestore realtime-listener implementation.
