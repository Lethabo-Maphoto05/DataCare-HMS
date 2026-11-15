# DataCare HMS Android — quick user guide

## Sign in

Use the same Firebase email/password account as the DataCare web application. The API checks the account role, email verification, approval and active status before loading the workspace.

## Create a patient account

1. Choose **Create a patient account**.
2. Enter your name, email, South African cell number, 13-digit ID and a strong password.
3. Open the verification email and verify the address.
4. Return to the app and log in.
5. Complete the patient registration form and re-enter the password you created. The API validates password strength but does not store the password. The account remains pending until an administrator approves it.

There is no public role picker. An administrator creates doctor accounts. Administrator accounts must already be provisioned in the existing Firebase project.

## Admin workspace

- Dashboard: counts, pending registrations and notifications.
- Patients: add a patient record, view the patient directory, add ward beds and allocate patients.
- Appointments: book an appointment for a patient.
- Prescriptions: create a prescription and its consultation invoice.
- Billing: view hospital invoices.
- Services: view the service catalogue.
- Reports: hospital activity summary.
- User Management: create doctor accounts and view doctors.

## Doctor workspace

- Dashboard: assigned appointment overview and notifications.
- Services: view the service catalogue.
- Appointments: confirm pending requests, adjust the date/time/location, and mark confirmed visits completed.
- Prescriptions: select an assigned patient, consultation service and medicine to issue a prescription and create a bill.

## Patient workspace

- Dashboard: patient details, appointment/invoice counts and notifications.
- Services: view the service catalogue.
- Appointments: request an appointment and view its status.
- Prescriptions: view prescriptions issued by the doctor.
- Billing: view invoices, inspect line items and submit the same demonstration payment or medical-aid record as the website.

**Online Demo is not a real payment.** No bank card is charged and no money is transferred.

## Refresh and shared data

Use the refresh icon in the top bar to reload the latest data from the API. Changes saved through the Android app are stored by the same backend used by the web app, and changes made on the website appear after refreshing Android.

## Help

Open the navigation drawer and choose **Need Help? Contact IT Support**.
