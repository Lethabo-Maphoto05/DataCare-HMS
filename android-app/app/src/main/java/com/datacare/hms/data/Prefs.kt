package com.datacare.hms.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** User settings kept on the device (theme, text size, notifications, remembered email). */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("datacare_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(sp.getString("theme", "system") ?: "system")
    private val _textScale = MutableStateFlow(sp.getFloat("textScale", 1.0f))
    private val _notifications = MutableStateFlow(sp.getBoolean("notifications", true))

    private val _onboardingDone = MutableStateFlow(sp.getBoolean("onboardingDone", false))
    val onboardingDone: StateFlow<Boolean> = _onboardingDone

    val theme: StateFlow<String> = _theme
    val textScale: StateFlow<Float> = _textScale
    val notifications: StateFlow<Boolean> = _notifications

    fun setTheme(v: String) { sp.edit().putString("theme", v).apply(); _theme.value = v }
    fun setTextScale(v: Float) { sp.edit().putFloat("textScale", v).apply(); _textScale.value = v }
    fun setOnboardingDone(v: Boolean) { sp.edit().putBoolean("onboardingDone", v).apply(); _onboardingDone.value = v }
    fun setNotifications(v: Boolean) { sp.edit().putBoolean("notifications", v).apply(); _notifications.value = v }

    var rememberedEmail: String
        get() = sp.getString("email", "") ?: ""
        set(v) { sp.edit().putString("email", v).apply() }
}
