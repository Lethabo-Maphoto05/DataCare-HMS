# DataCare HMS Android — verification checklist

Run the tests from the project root:

```powershell
.\gradlew.bat testDebugUnitTest
```

Build a debug APK:

```powershell
.\gradlew.bat assembleDebug
```

## Manual integration checks

Use synthetic test accounts and fictional patient details only. Start the same API that the web application uses before testing.

| # | Test | Expected result |
|---|---|---|
| 1 | Sign in with an existing approved admin account | Admin workspace opens |
| 2 | Sign in with an approved doctor account | Doctor workspace opens |
| 3 | Sign in with an approved patient account | Patient workspace opens |
| 4 | Try a wrong password | Clear login error; no crash |
| 5 | Register a patient with a new email | Verification email is sent |
| 6 | Verify email, sign in and complete registration | API creates a pending patient registration |
| 7 | Admin approves the pending registration | Patient receives an approval notification and can enter the workspace after signing in again |
| 8 | Admin creates a doctor account | Doctor appears in the shared doctor list and can sign in with the temporary password |
| 9 | Admin adds a patient record | Patient appears in the admin directory and on the web app after refresh |
| 10 | Admin adds a ward bed and allocates a patient | Bed status changes to Occupied in the shared database |
| 11 | Patient requests an appointment | Doctor sees the pending request |
| 12 | Doctor confirms or completes an appointment | Patient sees the updated status after refresh |
| 13 | Doctor/admin issues a prescription | Patient sees the prescription and the API creates an invoice |
| 14 | Patient opens and pays an invoice using Online Demo | API records a demonstration payment and marks the invoice Paid |
| 15 | Patient chooses Medical Aid | Required medical-aid fields are validated and the API records the demonstration claim |
| 16 | Mark a notification as read | Notification state updates in the shared API |
| 17 | Change data on the website and refresh Android | Latest API data appears in Android |
| 18 | Change data on Android and refresh the website | Latest API data appears on the website |
| 19 | Sign in with a pending or disabled account | Workspace remains blocked with a clear message |
| 20 | Stop the API and try to refresh | A connection error is shown; app does not crash |

The automated test suite currently checks that the three Android navigation maps match the current web role navigation. The manual integration checks above still need to be run against your Firebase project and running API.
