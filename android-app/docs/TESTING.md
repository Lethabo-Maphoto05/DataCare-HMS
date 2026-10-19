# Verification and testing

## Automated (run on every push by GitHub Actions)
`./gradlew testDebugUnitTest` - 26 unit tests in `app/src/test`:

| Class | What it proves |
|---|---|
| ValidatorsTest | email, password, SA phone, date, time and money checks accept good input and reject bad input |
| BillingTest | total, Paid/Outstanding rule, payment never drives balance below zero, payment validation |
| RolesTest | role permissions equal the website's map; only admins reach User Management |
| SeedDataTest | every table has >= 10 records, unique IDs, invoices obey billing rules, field names match the website |
| ReportsTest | revenue/outstanding/collected maths, counts sorted, empty data safe |

## Manual test cases (run on the real phone and record results)
| # | Test | Steps | Expected |
|---|---|---|---|
| 1 | Register | Create Account with valid data | "Registration complete", user appears in Firebase Authentication and `users` |
| 2 | Register invalid | Empty fields / bad email / 3-char password | Red messages under fields, nothing created, no crash |
| 3 | Duplicate email | Register same email twice | "This email is already registered." |
| 4 | Login | Correct credentials | Dashboard for that role |
| 5 | Wrong password | Wrong password | "Invalid email or password." |
| 6 | Google SSO | Continue with Google | Signed in, user appears in Authentication |
| 7 | Role menu | Login as Pharmacist, then Patient | Bottom bar differs per role |
| 8 | Add patient | Valid form | Appears in list and in Firestore `patients` |
| 9 | Patient invalid | Phone "123", future DOB | Field errors |
| 10 | Book appointment | Valid form, then Reschedule, then Cancel | Status Confirmed -> new date -> Cancelled |
| 11 | Past date | Appointment dated yesterday | "The date cannot be in the past" |
| 12 | Prescription | New, then Mark Completed | Moves to History |
| 13 | Invoice | Fees 500 + 150, outstanding 200 | Total R650.00, status Outstanding |
| 14 | Payment | Pay 200 | Status Paid, outstanding R0.00 |
| 15 | Overpayment | Pay more than outstanding | Error message, nothing changes |
| 16 | Reports | Open all three reports | Numbers match the lists |
| 17 | Settings | Dark mode, Extra large text | Applied immediately, kept after restart |
| 18 | Change password | Wrong current, then correct | Error, then success |
| 19 | Offline | Turn off data, tap Save | "Network error..." message, no crash |
| 20 | Web <-> app | Add record on website | Appears on phone (and the reverse) |
| 21 | Logout | More > Logout | Returns to login |
