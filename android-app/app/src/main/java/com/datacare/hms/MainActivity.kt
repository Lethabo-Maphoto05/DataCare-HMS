package com.datacare.hms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.datacare.hms.ui.AuthHost
import com.datacare.hms.ui.MainShell
import com.datacare.hms.ui.OnboardingScreen
import com.datacare.hms.ui.SplashScreen
import kotlinx.coroutines.delay
import androidx.compose.runtime.saveable.rememberSaveable
import com.datacare.hms.ui.theme.DataCareTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme by vm.prefs.theme.collectAsState()
            val scale by vm.prefs.textScale.collectAsState()
            val auth by vm.authState.collectAsState()
            val snackbar = remember { SnackbarHostState() }

            // Show every message from the ViewModel as a snackbar.
            LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }

            val onboardingDone by vm.prefs.onboardingDone.collectAsState()
            // Splash is always shown for a moment so the logo is seen, then the app picks the right screen.
            var splashDone by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(2400)
                splashDone = true
            }

            DataCareTheme(theme, scale) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val s = auth
                    when {
                        !splashDone || s is AuthState.Loading -> SplashScreen()
                        s is AuthState.SignedIn -> MainShell(vm, s.profile, snackbar)
                        !onboardingDone -> OnboardingScreen(onFinish = { vm.prefs.setOnboardingDone(true) })
                        else -> AuthHost(vm, snackbar)
                    }
                }
            }
        }
    }
}
