package com.datacare.hms.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datacare.hms.AppViewModel
import com.datacare.hms.R
import com.datacare.hms.data.Roles
import com.datacare.hms.util.Validators
import kotlinx.coroutines.launch

@Composable
fun AuthHost(vm: AppViewModel, snackbar: SnackbarHostState) {
    var showRegister by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize().verticalScroll(rememberScrollState())) {
            AuthHero(compact = showRegister)
            Surface(
                Modifier.fillMaxWidth().offset(y = (-28).dp),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 6.dp
            ) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 22.dp).navigationBarsPadding().imePadding()) {
                    if (showRegister) RegisterForm(vm) { showRegister = false } else LoginForm(vm) { showRegister = true }
                }
            }
        }
    }
}

/** Photo header with the logo, shown above the login and register forms. */
@Composable
private fun AuthHero(compact: Boolean) {
    Box(Modifier.fillMaxWidth().height(if (compact) 190.dp else 260.dp)) {
        Image(painterResource(R.drawable.onboarding_1), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color(0x99062A4D), Color(0x55062A4D), Color(0xCC062A4D)))
            )
        )
        Row(
            Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White, shadowElevation = 8.dp) {
                Image(
                    painterResource(R.drawable.datacare_logo), "DataCare logo",
                    Modifier.height(if (compact) 72.dp else 96.dp).padding(6.dp), contentScale = ContentScale.Fit
                )
            }
            Column {
                Text("DataCare", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Hospital Management System", color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("Netcare Alberton Hospital", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LoginForm(vm: AppViewModel, onCreateAccount: () -> Unit) {
    var email by rememberSaveable { mutableStateOf(vm.prefs.rememberedEmail) }
    var password by rememberSaveable { mutableStateOf("") }
    var emailErr by remember { mutableStateOf<String?>(null) }
    var pwErr by remember { mutableStateOf<String?>(null) }
    val busy by vm.busy.collectAsState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    fun submit() {
        emailErr = Validators.emailError(email)
        pwErr = if (password.isEmpty()) "Password is required" else null
        if (emailErr == null && pwErr == null) vm.signIn(email, password)
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Sign in to your DataCare Hospital Management account.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        DcTextField("Email Address", email, { email = it; emailErr = null }, icon = Icons.Default.Email, error = emailErr, keyboardType = KeyboardType.Email)
        DcTextField("Password", password, { password = it; pwErr = null }, icon = Icons.Default.Lock, error = pwErr, password = true, imeAction = ImeAction.Done)
        TextButton(
            onClick = {
                val e = Validators.emailError(email)
                if (e != null) emailErr = "Enter your email above first" else vm.resetPassword(email)
            },
            modifier = Modifier.align(Alignment.End)
        ) { Text("Forgot password?") }
        PrimaryButton(
            if (busy) "Signing in..." else "Login",
            { submit() },
            Modifier.fillMaxWidth(),
            Icons.AutoMirrored.Filled.Login,
            enabled = !busy
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(Modifier.weight(1f))
            Text("  or  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(Modifier.weight(1f))
        }
        SecondaryButton("Continue with Google", {
            scope.launch {
                when (val r = requestGoogleIdToken(ctx)) {
                    is GoogleResult.Token -> vm.signInWithGoogle(r.idToken)
                    is GoogleResult.Failed -> vm.showMessage(r.reason)
                    GoogleResult.Cancelled -> Unit
                }
            }
        }, Modifier.fillMaxWidth(), Icons.Default.AccountCircle)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onCreateAccount) { Text("Create Account", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun RegisterForm(vm: AppViewModel, onLogin: () -> Unit) {
    var first by rememberSaveable { mutableStateOf("") }
    var last by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("") }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    val busy by vm.busy.collectAsState()

    fun submit() {
        val e = mutableMapOf<String, String>()
        Validators.requiredError("First name", first)?.let { e["first"] = it }
        Validators.requiredError("Last name", last)?.let { e["last"] = it }
        Validators.emailError(email)?.let { e["email"] = it }
        Validators.passwordError(password)?.let { e["password"] = it }
        if (role.isBlank()) e["role"] = "Select your role"
        errors = e
        if (e.isEmpty()) vm.register(first, last, email, password, role) { ok -> if (ok) onLogin() }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Create Account", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(
            "Register a new account for the DataCare Hospital Management System.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DcTextField("First Name", first, { first = it; errors = errors - "first" }, Modifier.weight(1f), Icons.Default.Person, errors["first"])
            DcTextField("Last Name", last, { last = it; errors = errors - "last" }, Modifier.weight(1f), Icons.Default.Person, errors["last"])
        }
        DcTextField("Email Address", email, { email = it; errors = errors - "email" }, icon = Icons.Default.Email, error = errors["email"], keyboardType = KeyboardType.Email)
        DcTextField("Password (min 6 characters)", password, { password = it; errors = errors - "password" }, icon = Icons.Default.Lock, error = errors["password"], password = true)
        DropdownField("User Role", role, Roles.all, { role = it; errors = errors - "role" }, errors["role"])
        Spacer(Modifier.height(6.dp))
        PrimaryButton(if (busy) "Creating account..." else "Create Account", { submit() }, Modifier.fillMaxWidth(), Icons.Default.PersonAdd, enabled = !busy)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account?", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onLogin) { Text("Login", fontWeight = FontWeight.Bold) }
        }
    }
}