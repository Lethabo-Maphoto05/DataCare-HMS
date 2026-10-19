# Database design (Cloud Firestore)

Firestore is a document database: each **collection** below is a "table" and each **document** a "record". Field names are identical to the website. Simulated data (>= 10 per table) comes from `SeedData.kt`.

| Collection (table) | Key | Fields | Seed records |
|---|---|---|---|
| `users` | doc id = Firebase Auth uid | uid, firstName, lastName, email, role, createdAt | created by registration |
| `patients` | patientID (PAT-nnnnnn) | firstName, lastName, gender, dateOfBirth, phone, address, medicalHistory, emergencyContact, createdBy, createdAt | 12 |
| `appointments` | appointmentID (APT-nnnnnn) | patient, doctor, date, time, appointmentType, wardAllocation, status, createdBy, createdAt | 12 |
| `prescriptions` | prescriptionID (RX-nnnnnn) | patientID, doctorID, medication, dosage, notes, prescriptionDate, status, createdBy, createdAt | 12 |
| `billing` | invoiceNumber (INV-nnnnnn) | patientID, consultationFee, medicationCost, totalAmount, medicalAid, paymentMethod, outstandingBalance, status, createdBy, createdAt | 12 |
| `departments` | departmentID | name, head, location, phone, description | 10 |
| `staff` | staffID | firstName, lastName, role, department, email, phone | 10 |
| `inventory` | itemID | itemName, category, quantity, reorderLevel, unitCost, supplier | 10 |
| `medicalRecords` | recordID | patientID, diagnosis, treatment, doctor, recordDate, notes | 10 |

## Relationships (logical, as in the Task 1 ERD)
- `appointments.patient`, `prescriptions.patientID`, `billing.patientID`, `medicalRecords.patientID` -> `patients.patientID`
- `users.role` -> one of the 8 roles (UserRole); drives screen access
- `staff.department` -> `departments.name`

## Business rules
- `totalAmount = consultationFee + medicationCost`
- `status = "Outstanding"` if `outstandingBalance > 0`, otherwise `"Paid"`
- New appointments start as `Confirmed`; new prescriptions start as `Active`
- Passwords are **not** stored in Firestore; Firebase Authentication stores a salted hash

## Security
`firestore.rules` requires sign-in for all hospital data, lets only the owner or a System Administrator read a profile, and blocks users from changing their own role.
