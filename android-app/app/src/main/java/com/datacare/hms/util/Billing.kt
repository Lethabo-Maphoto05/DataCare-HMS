package com.datacare.hms.util

import kotlin.math.max
import kotlin.math.round

/** Same rules as the web app's saveBilling(): total = consultation + medication; outstanding > 0 => "Outstanding". */
object Billing {
    fun round2(v: Double): Double = round(v * 100.0) / 100.0

    fun total(consultationFee: Double, medicationCost: Double): Double =
        round2(consultationFee + medicationCost)

    fun statusFor(outstanding: Double): String = if (outstanding > 0.0) "Outstanding" else "Paid"

    /** Outstanding balance after a payment; never below zero. */
    fun balanceAfterPayment(outstanding: Double, payment: Double): Double =
        round2(max(0.0, outstanding - payment))

    fun paymentError(amount: Double?, outstanding: Double): String? = when {
        amount == null || amount <= 0.0 -> "Enter an amount greater than 0"
        amount > outstanding + 0.005 -> "Amount is more than the outstanding balance"
        else -> null
    }
}
