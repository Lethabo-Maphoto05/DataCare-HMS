# Android API integration

The Android client intentionally uses the current web application's API contract rather than the older standalone Android Firestore schema.

## Authentication

1. Firebase Authentication signs the user in.
2. `ApiRepository` obtains a refreshed Firebase ID token.
3. Every API request includes `Authorization: Bearer <Firebase ID token>`.
4. The API verifies the token and loads the user's `users/{uid}` record.
5. `/auth/me` returns the approved account and role. The API is the authority for permissions.

Public patient registration uses Firebase Authentication first. The user verifies their email, signs in again, and then submits the patient details to `/auth/register`. The API creates a pending patient account. The Android app does not provide public role selection or administrator registration.

## API routes used

| Workspace | Routes |
|---|---|
| Shared | `GET /auth/me`, `GET /services`, `GET /notifications`, `PATCH /notifications/:id/read` |
| Admin | `GET /reports/summary`, `GET /admin/registrations`, `PATCH /admin/registrations/:id`, `GET /doctors`, `POST /admin/doctors`, `GET /patients`, `POST /admin/patients`, `GET /admin/wards`, `POST /admin/wards`, `POST /admin/admissions`, `POST /admin/appointments`, `GET /admin/invoices`, `GET /medications`, `POST /prescriptions` |
| Doctor | `GET /appointments/doctor`, `GET /patients`, `GET /medications`, `POST /prescriptions`, `PATCH /appointments/:id/confirm`, `PATCH /appointments/:id/status` |
| Patient | `GET /patients/me`, `GET /doctors`, `POST /appointments`, `GET /appointments/mine`, `GET /prescriptions/mine`, `GET /invoices/mine`, `GET /invoices/:id`, `POST /invoices/:id/pay` |

## Firestore

All business record reads and writes go through the backend's Firebase Admin SDK. Do not publish the old permissive `firestore.rules` file from the original Android ZIP. The server-side API performs the role and ownership checks described in the current web repository README.
