# Shared DataCare database

The Android application does not define a separate database or a second set of collections. It uses the same Node.js API and Cloud Firestore data used by the web application.

The backend's current collections include:

`users`, `patients`, `doctors`, `services`, `medications`, `wards`, `admissions`, `appointments`, `prescriptions`, `prescription_items`, `invoices`, `invoice_items`, `payments`, `notifications`, `audit_logs` and `_counters`.

The API generates record IDs and validates relationships. The Android client sends API request bodies using the same snake_case field names as the web application. It does not seed demonstration records or write directly to Firestore.

## Data flow

`Kotlin app → Firebase ID token → Express API → Firebase Admin SDK → existing Firestore database`

The server is responsible for role permissions, account approval, patient/doctor ownership and record updates. Do not deploy the old `firestore.rules` file from the original Android project; it allowed all signed-in users to write every hospital collection and is not part of this API-based design.

Use fictional data only. This academic prototype is not approved for real patient records.
