package com.datacare.hms.ui

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.datacare.hms.R
import com.datacare.hms.ui.theme.*
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------------------------
// Splash screen: logo fades and scales in on a soft brand gradient.
// ---------------------------------------------------------------------------------------------
@Composable
fun SplashScreen() {
    val scale = remember { Animatable(0.82f) }
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, tween(900, easing = FastOutSlowInEasing)) }
        launch { fade.animateTo(1f, tween(800)) }
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White, Color(0xFFE3F1FB), Color(0xFFD6F0E6)))),
    ) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painterResource(R.drawable.datacare_logo), "DataCare Hospital Management System logo",
                Modifier.height(300.dp).scale(scale.value).alpha(fade.value),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator(Modifier.size(30.dp).alpha(fade.value), color = Green, strokeWidth = 3.dp)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Netcare Alberton Hospital", color = Heading, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Version 1.0", color = MutedText, fontSize = 12.sp)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Onboarding: swipeable pages with full-screen background pictures.
// To use your own photos, replace app/src/main/res/drawable-nodpi/onboarding_1..4.jpg (same names).
// ---------------------------------------------------------------------------------------------
private data class OnboardPage(val image: Int, val icon: ImageVector, val title: String, val text: String)

private val PAGES = listOf(
    OnboardPage(
        R.drawable.onboarding_1, Icons.Default.LocalHospital, "Welcome to DataCare",
        "Your hospital in your pocket. Access care services at Netcare Alberton Hospital anytime, anywhere."
    ),
    OnboardPage(
        R.drawable.onboarding_2, Icons.Default.EventAvailable, "Book Appointments Easily",
        "Schedule, reschedule or cancel appointments with your doctor in just a few taps."
    ),
    OnboardPage(
        R.drawable.onboarding_3, Icons.Default.Medication, "Prescriptions & Medication",
        "Keep track of electronic prescriptions and your full medication history in one place."
    ),
    OnboardPage(
        R.drawable.onboarding_4, Icons.Default.Security, "Billing, Reports & Privacy",
        "View invoices, pay securely and see hospital reports. Your information is protected in line with POPIA."
    )
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pager = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == PAGES.size - 1

    // White status-bar icons over the dark photo scrim; restored when leaving.
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val old = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = false
        onDispose { if (controller != null && old != null) controller.isAppearanceLightStatusBars = old }
    }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(pager, Modifier.fillMaxSize()) { index ->
            val page = PAGES[index]
            Box(Modifier.fillMaxSize()) {
                Image(painterResource(page.image), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0f to Color(0x80041526), 0.35f to Color(0x20041526),
                            0.6f to Color(0x99041526), 1f to Color(0xF0041526)
                        )
                    )
                )
                Column(
                    Modifier.align(Alignment.BottomStart).padding(horizontal = 28.dp).padding(bottom = 190.dp)
                ) {
                    Box(
                        Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) { Icon(page.icon, null, Modifier.size(34.dp), tint = Color.White) }
                    Spacer(Modifier.height(20.dp))
                    Text(page.title, color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, lineHeight = 38.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(page.text, color = Color.White.copy(alpha = 0.92f), fontSize = 17.sp, lineHeight = 25.sp)
                }
            }
        }

        // Skip
        if (!last) {
            TextButton(
                onClick = onFinish,
                modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(8.dp).heightIn(min = MinTouch)
            ) { Text("Skip", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        }

        // Dots + buttons
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(PAGES.size) { i ->
                    val selected = pager.currentPage == i
                    Box(
                        Modifier.height(8.dp).width(if (selected) 28.dp else 8.dp).clip(CircleShape)
                            .background(if (selected) Color.White else Color.White.copy(alpha = 0.4f))
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (pager.currentPage > 0) {
                    OutlinedButton(
                        onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } },
                        modifier = Modifier.heightIn(min = 52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                    ) { Text("Back", fontWeight = FontWeight.SemiBold) }
                }
                Button(
                    onClick = { if (last) onFinish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Heading)
                ) {
                    Text(if (last) "Get Started" else "Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (last) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}