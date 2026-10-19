package com.datacare.hms.data

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** All Firestore access lives here, so screens never touch Firebase directly. */
class Repository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    /** Live list of a collection. A permission/network error yields an empty list instead of crashing. */
    fun <T : Any> listen(collection: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
        val registration = db.collection(collection).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot.documents.mapNotNull {
                try {
                    it.toObject(clazz)
                } catch (e: Exception) {
                    null // skip malformed documents rather than crash
                }
            }
            trySend(items)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getOrCreateProfile(user: FirebaseUser): UserProfile {
        val ref = db.collection(Collections.USERS).document(user.uid)
        val snap = ref.get().await()
        if (snap.exists()) {
            val parsed = try {
                snap.toObject(UserProfile::class.java)
            } catch (e: Exception) {
                null
            }
            if (parsed != null) return parsed.copy(uid = user.uid)
            // Fallback: read fields one by one so an odd field type never blocks login.
            return UserProfile(
                uid = user.uid,
                firstName = snap.get("firstName")?.toString().orEmpty(),
                lastName = snap.get("lastName")?.toString().orEmpty(),
                email = snap.get("email")?.toString() ?: user.email.orEmpty(),
                role = snap.get("role")?.toString()?.takeIf { it in Roles.all } ?: "Patient",
                createdAt = snap.get("createdAt")?.toString().orEmpty()
            )
        }
        // First sign-in with Google (or an account that has no profile document yet).
        val parts = (user.displayName ?: "").trim().split(" ", limit = 2)
        val profile = UserProfile(
            uid = user.uid,
            firstName = parts.getOrNull(0).orEmpty(),
            lastName = parts.getOrNull(1).orEmpty(),
            email = user.email.orEmpty(),
            role = "Patient",
            createdAt = com.datacare.hms.util.Format.nowIso()
        )
        ref.set(profile).await()
        return profile
    }

    suspend fun createProfile(profile: UserProfile) {
        db.collection(Collections.USERS).document(profile.uid).set(profile).await()
    }

    suspend fun updateName(uid: String, first: String, last: String) {
        db.collection(Collections.USERS).document(uid)
            .set(mapOf("firstName" to first, "lastName" to last), SetOptions.merge()).await()
    }

    suspend fun add(collection: String, data: Map<String, Any?>) {
        db.collection(collection).add(data).await()
    }

    suspend fun update(collection: String, docId: String, data: Map<String, Any?>) {
        db.collection(collection).document(docId).set(data, SetOptions.merge()).await()
    }

    /** Writes the simulated data set (idempotent: fixed document IDs). */
    suspend fun seed() {
        val batch = db.batch()
        SeedData.all.forEach { (collection, docs) ->
            docs.forEach { d -> batch.set(db.collection(collection).document(d.id), d.data) }
        }
        batch.commit().await()
    }
}
