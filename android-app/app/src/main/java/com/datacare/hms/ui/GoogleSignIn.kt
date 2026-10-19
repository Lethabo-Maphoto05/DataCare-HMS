package com.datacare.hms.ui

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.datacare.hms.FirebaseConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed interface GoogleResult {
    data class Token(val idToken: String) : GoogleResult
    data object Cancelled : GoogleResult
    data class Failed(val reason: String) : GoogleResult
}

/** Single sign-on: asks Android's Credential Manager for a Google ID token (the caller hands it to Firebase). */
suspend fun requestGoogleIdToken(activityContext: Context): GoogleResult {
    if (FirebaseConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
        return GoogleResult.Failed("Google sign-in is not configured yet. Add the Web client ID in FirebaseConfig.kt.")
    }
    return try {
        val option = GetSignInWithGoogleOption.Builder(FirebaseConfig.GOOGLE_WEB_CLIENT_ID).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val result = CredentialManager.create(activityContext).getCredential(activityContext, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            GoogleResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
        } else {
            GoogleResult.Failed("Unsupported credential type.")
        }
    } catch (e: GetCredentialCancellationException) {
        GoogleResult.Cancelled
    } catch (e: NoCredentialException) {
        GoogleResult.Failed("No Google account found on this phone. Add one in Settings and try again.")
    } catch (e: GetCredentialException) {
        GoogleResult.Failed("Google sign-in failed. Please try again.")
    } catch (e: Exception) {
        GoogleResult.Failed("Google sign-in failed. Please try again.")
    }
}
