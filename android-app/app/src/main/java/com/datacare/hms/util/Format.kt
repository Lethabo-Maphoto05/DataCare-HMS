package com.datacare.hms.util

import java.util.Locale

object Format {
    fun currency(v: Double): String = String.format(Locale.US, "R%,.2f", v)

    fun newId(prefix: String): String = "$prefix-${(100000..999999).random()}"

    /** ISO-8601 UTC timestamp (java.time needs API 26; minSdk is 24, so SimpleDateFormat is used). */
    fun nowIso(): String {
        val f = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        f.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return f.format(java.util.Date())
    }

    /** "Good morning" / "Good afternoon" / "Good evening" for an hour 0-23. */
    fun greetingFor(hour: Int): String = when (hour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    fun greeting(): String = greetingFor(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))

    fun today(): String {
        val c = java.util.Calendar.getInstance()
        return String.format(
            Locale.US, "%04d-%02d-%02d",
            c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1, c.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
}
