package com.datacare.hms.data

import com.datacare.hms.util.Billing

/**
 * Simulated hospital data (Task 2 requires at least 10 records per table).
 * Document IDs are fixed (seed_pat_01 ...) so seeding is idempotent: running it twice overwrites, never duplicates.
 * Pure Kotlin so it can be unit-tested.
 */
object SeedData {

    data class Doc(val id: String, val data: Map<String, Any>)

    private val firstNames = listOf(
        "Thabo", "Naledi", "Sipho", "Lerato", "Pieter", "Aisha", "Johan", "Zanele", "Kagiso", "Ayanda", "Mpho", "Fatima"
    )
    private val lastNames = listOf(
        "Mokoena", "Dlamini", "Nkosi", "Khumalo", "van der Merwe", "Patel", "Botha", "Mahlangu", "Sithole", "Naidoo", "Molefe", "Ismail"
    )
    private val genders = listOf("Male", "Female", "Male", "Female", "Male", "Female", "Male", "Female", "Male", "Male", "Female", "Female")
    private val histories = listOf(
        "Hypertension", "Asthma", "Type 2 diabetes", "No known conditions", "Previous knee surgery", "Migraines",
        "High cholesterol", "Penicillin allergy", "Arthritis", "Anaemia", "Gestational diabetes", "Hay fever"
    )
    private val doctors = listOf(
        "Dr. Sipho Ndlovu", "Dr. Anika Pretorius", "Dr. Rajesh Moodley", "Dr. Thandi Zulu", "Dr. Michael Jacobs", "Dr. Lindiwe Radebe"
    )
    private val wards = listOf("Ward A", "Ward B", "Ward C", "Maternity Ward", "ICU", "Outpatients", "Orthopaedic Ward", "Cardiac Unit")
    private val types = listOf("Consultation", "Follow-up", "Emergency", "Specialist", "Check-up")

    private fun id2(n: Int) = n.toString().padStart(2, '0')
    fun patientId(n: Int) = "PAT-${100000 + n}"

    val patients: List<Doc> = (1..12).map { n ->
        val i = n - 1
        Doc(
            "seed_pat_${id2(n)}",
            mapOf(
                "patientID" to patientId(n),
                "firstName" to firstNames[i],
                "lastName" to lastNames[i],
                "gender" to genders[i],
                "dateOfBirth" to "${1955 + i * 4}-${id2(1 + i % 12)}-${id2(5 + i)}",
                "phone" to "082 555 ${(1000 + n * 37)}",
                "address" to "${10 + n} Voortrekker Road, Alberton, Gauteng",
                "medicalHistory" to histories[i],
                "emergencyContact" to "${lastNames[(i + 3) % 12]} family - 083 444 ${(2000 + n * 11)}",
                "createdBy" to "seed",
                "createdAt" to "2026-09-${id2(1 + i)}T08:00:00.000Z"
            )
        )
    }

    private val statuses = listOf(
        "Confirmed", "Completed", "Confirmed", "Cancelled", "Completed", "Confirmed",
        "Confirmed", "Completed", "Pending", "Confirmed", "Completed", "Confirmed"
    )

    val appointments: List<Doc> = (1..12).map { n ->
        val i = n - 1
        Doc(
            "seed_apt_${id2(n)}",
            mapOf(
                "appointmentID" to "APT-${200000 + n}",
                "patient" to patientId(n),
                "doctor" to doctors[i % doctors.size],
                "date" to "2026-10-${id2(1 + (i * 2) % 28)}",
                "time" to "${id2(8 + i % 9)}:${if (i % 2 == 0) "00" else "30"}",
                "appointmentType" to types[i % types.size],
                "wardAllocation" to wards[i % wards.size],
                "status" to statuses[i],
                "createdBy" to "seed",
                "createdAt" to "2026-09-${id2(10 + i)}T09:00:00.000Z"
            )
        )
    }

    private val meds = listOf(
        "Amlodipine" to "5 mg once daily", "Salbutamol inhaler" to "2 puffs when needed", "Metformin" to "500 mg twice daily",
        "Amoxicillin" to "500 mg three times daily", "Ibuprofen" to "400 mg every 8 hours", "Sumatriptan" to "50 mg at onset",
        "Atorvastatin" to "20 mg at night", "Cetirizine" to "10 mg once daily", "Diclofenac" to "50 mg twice daily",
        "Ferrous sulphate" to "200 mg once daily", "Folic acid" to "5 mg once daily", "Loratadine" to "10 mg once daily"
    )
    private val rxStatus = listOf(
        "Active", "Active", "Active", "Completed", "Completed", "Active",
        "Active", "Completed", "Active", "Active", "Cancelled", "Active"
    )

    val prescriptions: List<Doc> = (1..12).map { n ->
        val i = n - 1
        Doc(
            "seed_rx_${id2(n)}",
            mapOf(
                "prescriptionID" to "RX-${300000 + n}",
                "patientID" to patientId(n),
                "doctorID" to doctors[i % doctors.size],
                "medication" to meds[i].first,
                "dosage" to meds[i].second,
                "notes" to if (i % 3 == 0) "Take with food" else "Review in 2 weeks",
                "prescriptionDate" to "2026-09-${id2(12 + i)}",
                "status" to rxStatus[i],
                "createdBy" to "seed",
                "createdAt" to "2026-09-${id2(12 + i)}T10:00:00.000Z"
            )
        )
    }

    private val payMethods = listOf("Cash", "Card", "Medical Aid", "EFT")
    private val aids = listOf("Discovery Health", "Bonitas", "Momentum Health", "Medshield", "GEMS", "")

    val invoices: List<Doc> = (1..12).map { n ->
        val i = n - 1
        val consult = 450.0 + (i % 4) * 150.0
        val medication = 80.0 + (i % 5) * 65.0
        val outstanding = if (i % 3 == 0) Billing.round2((consult + medication) / 2) else 0.0
        Doc(
            "seed_inv_${id2(n)}",
            mapOf(
                "invoiceNumber" to "INV-${400000 + n}",
                "patientID" to patientId(n),
                "consultationFee" to consult,
                "medicationCost" to medication,
                "totalAmount" to Billing.total(consult, medication),
                "medicalAid" to aids[i % aids.size],
                "paymentMethod" to payMethods[i % payMethods.size],
                "outstandingBalance" to outstanding,
                "status" to Billing.statusFor(outstanding),
                "createdBy" to "seed",
                "createdAt" to "2026-09-${id2(15 + i)}T11:00:00.000Z"
            )
        )
    }

    private val deptData = listOf(
        Triple("Emergency & Trauma", "Dr. Sipho Ndlovu", "Ground Floor, Block A"),
        Triple("Surgery", "Dr. Michael Jacobs", "First Floor, Block B"),
        Triple("Maternity", "Dr. Thandi Zulu", "Second Floor, Block C"),
        Triple("Orthopaedics", "Dr. Rajesh Moodley", "First Floor, Block C"),
        Triple("Cardiology", "Dr. Anika Pretorius", "Second Floor, Block B"),
        Triple("Radiology", "Dr. Lindiwe Radebe", "Basement, Block A"),
        Triple("Pathology", "Dr. Yusuf Cassim", "Basement, Block B"),
        Triple("Pharmacy", "Pharm. Nomsa Dube", "Ground Floor, Block B"),
        Triple("Billing & Admissions", "Mr. Kevin Smit", "Ground Floor, Reception"),
        Triple("Outpatients", "Sr. Busisiwe Khoza", "Ground Floor, Block C")
    )

    val departments: List<Doc> = deptData.mapIndexed { i, (name, head, loc) ->
        Doc(
            "seed_dept_${id2(i + 1)}",
            mapOf(
                "departmentID" to "DEP-${i + 1}",
                "name" to name,
                "head" to head,
                "location" to loc,
                "phone" to "011 555 ${1000 + i * 10}",
                "description" to "$name department of DataCare Hospital"
            )
        )
    }

    private val staffRoles = listOf(
        "Doctor", "Doctor", "Doctor", "Nurse", "Nurse", "Pharmacist", "Receptionist", "Billing Clerk", "Hospital Manager", "System Administrator"
    )

    val staff: List<Doc> = (1..10).map { n ->
        val i = n - 1
        Doc(
            "seed_staff_${id2(n)}",
            mapOf(
                "staffID" to "STF-${500 + n}",
                "firstName" to firstNames[(i + 5) % 12],
                "lastName" to lastNames[(i + 7) % 12],
                "role" to staffRoles[i],
                "department" to deptData[i % deptData.size].first,
                "email" to "staff$n@datacare-hms.example",
                "phone" to "011 555 ${2000 + n * 7}"
            )
        )
    }

    private val inventoryData = listOf(
        Triple("Paracetamol 500mg (box of 100)", "Medication", 85.0), Triple("Amoxicillin 500mg (box of 30)", "Medication", 120.0),
        Triple("Surgical gloves (box)", "Consumables", 95.0), Triple("Syringes 5ml (pack of 100)", "Consumables", 150.0),
        Triple("IV drip set", "Consumables", 45.0), Triple("Face masks N95 (box)", "PPE", 210.0),
        Triple("Bandage roll", "Consumables", 18.0), Triple("Insulin pen", "Medication", 320.0),
        Triple("Oxygen mask", "Equipment", 60.0), Triple("X-ray film (pack)", "Radiology", 400.0)
    )

    val inventory: List<Doc> = inventoryData.mapIndexed { i, (name, cat, cost) ->
        Doc(
            "seed_inv_item_${id2(i + 1)}",
            mapOf(
                "itemID" to "ITM-${700 + i}",
                "itemName" to name,
                "category" to cat,
                "quantity" to (20L + i * 13),
                "reorderLevel" to 25L,
                "unitCost" to cost,
                "supplier" to if (i % 2 == 0) "MediSupply SA" else "HealthPro Distributors"
            )
        )
    }

    private val diagnoses = listOf(
        "Essential hypertension" to "Lifestyle changes and Amlodipine", "Mild asthma" to "Inhaler therapy",
        "Type 2 diabetes mellitus" to "Metformin and diet plan", "Bacterial sinusitis" to "Amoxicillin course",
        "Knee osteoarthritis" to "Physiotherapy and NSAIDs", "Migraine" to "Triptan and trigger diary",
        "Hyperlipidaemia" to "Statin therapy", "Allergic rhinitis" to "Antihistamines",
        "Lower back pain" to "Rest and physiotherapy", "Iron-deficiency anaemia" to "Iron supplements"
    )

    val medicalRecords: List<Doc> = diagnoses.mapIndexed { i, (dx, tx) ->
        Doc(
            "seed_rec_${id2(i + 1)}",
            mapOf(
                "recordID" to "REC-${600 + i}",
                "patientID" to patientId(i + 1),
                "diagnosis" to dx,
                "treatment" to tx,
                "doctor" to doctors[i % doctors.size],
                "recordDate" to "2026-09-${id2(2 + i)}",
                "notes" to "Patient advised to return for review."
            )
        )
    }

    /** Collection name -> documents. */
    val all: Map<String, List<Doc>> = linkedMapOf(
        Collections.PATIENTS to patients,
        Collections.APPOINTMENTS to appointments,
        Collections.PRESCRIPTIONS to prescriptions,
        Collections.BILLING to invoices,
        Collections.DEPARTMENTS to departments,
        Collections.STAFF to staff,
        Collections.INVENTORY to inventory,
        Collections.MEDICAL_RECORDS to medicalRecords
    )
}
