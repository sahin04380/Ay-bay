package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.EntryType
import com.example.data.model.LedgerEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LedgerRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    db = FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    auth = Firebase.auth,
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun observeEntries(userId: String): Flow<List<LedgerEntry>> {
    val path = "users/$userId/entries"
    return db.collection("users")
      .document(userId)
      .collection("entries")
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          LedgerEntry::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
          handleFirestoreError(exceptionToLog, OperationType.LIST, path, auth)
        }
        throw error
      }
  }

  suspend fun getEntry(ownerUserId: String, entryId: String): Result<LedgerEntry?> {
    val docRef = db.collection("users")
      .document(ownerUserId)
      .collection("entries")
      .document(entryId)
    return try {
      val snapshot = docRef.get().await()
      val entry = snapshot.toObject(
        LedgerEntry::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
      )
      Result.success(entry)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun addEntry(
    type: EntryType,
    amount: Double,
    dateStr: String,
    description: String,
  ): Result<String> {
    val uid = requireUserId()
    val docRef = db.collection("users").document(uid).collection("entries").document()
    val payload = mapOf(
      "userId" to uid,
      "type" to type.wireValue,
      "amount" to amount,
      "dateStr" to dateStr,
      "description" to description.trim(),
      "clientOrder" to System.currentTimeMillis(),
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun updateEntry(
    entryId: String,
    type: EntryType,
    amount: Double,
    dateStr: String,
    description: String,
  ): Result<Unit> {
    val uid = requireUserId()
    require(entryId.isNotBlank()) { "Entry ID must not be blank for update" }
    val docRef = db.collection("users").document(uid).collection("entries").document(entryId)
    val updates = mapOf(
      "type" to type.wireValue,
      "amount" to amount,
      "dateStr" to dateStr,
      "description" to description.trim(),
      "updatedAt" to FieldValue.serverTimestamp(),
    )
    return try {
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path, auth)
      Result.failure(e)
    }
  }

  suspend fun deleteEntry(entryId: String): Result<Unit> {
    val uid = requireUserId()
    require(entryId.isNotBlank()) { "Entry ID must not be blank for delete" }
    val docRef = db.collection("users").document(uid).collection("entries").document(entryId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path, auth)
      Result.failure(e)
    }
  }
}
