package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.EntryType
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addAndObserveAndUpdateAndDeleteEntry_owner_succeeds() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repo = LedgerRepository(firestore, auth)

    val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.addEntry(
        type = EntryType.INCOME,
        amount = 2500.0,
        dateStr = "2026-11-15",
        description = "পণ্য বিক্রি",
      )
    }
    assertTrue("Expected addEntry to succeed", addResult.isSuccess)
    val entryId = addResult.getOrThrow()
    assertTrue(entryId.isNotBlank())

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeEntries(aliceUid).first { list -> list.any { it.id == entryId } }
    }
    val created = observed.first { it.id == entryId }
    assertEquals(EntryType.INCOME, created.entryType)
    assertEquals(2500.0, created.amount, 0.001)
    assertNotNull(created.createdAt)

    val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.updateEntry(
        entryId = entryId,
        type = EntryType.EXPENSE,
        amount = 850.5,
        dateStr = "2026-11-16",
        description = "দোকান ভাড়া",
      )
    }
    assertTrue("Expected updateEntry to succeed", updateResult.isSuccess)

    val updatedList = withTimeout(FLOW_TIMEOUT_MS) {
      repo.observeEntries(aliceUid).first { list ->
        list.any { it.id == entryId && it.type == EntryType.EXPENSE.wireValue && it.amount == 850.5 }
      }
    }
    assertTrue(updatedList.any { it.id == entryId && it.description == "দোকান ভাড়া" })

    val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repo.deleteEntry(entryId)
    }
    assertTrue("Expected deleteEntry to succeed", deleteResult.isSuccess)
  }

  @Test
  fun crossUserAccess_deniedBySecurityRules() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = LedgerRepository(firestore, auth)
    val entryId = withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.addEntry(
        type = EntryType.INCOME,
        amount = 1000.0,
        dateStr = "2026-11-15",
        description = "Alice entry",
      ).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = LedgerRepository(firestore, auth)

    val getResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getEntry(aliceUid, entryId)
    }
    assertTrue("Expected cross-user get to fail", getResult.isFailure)
    val getException = getResult.exceptionOrNull()?.unwrapFirestoreException()
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, getException?.code)

    var observeError: Throwable? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeEntries(aliceUid).first()
      }
    } catch (e: Exception) {
      observeError = e
    }
    val observeFirestoreException = checkNotNull(observeError?.unwrapFirestoreException()) {
      "Expected nested FirebaseFirestoreException, got $observeError"
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, observeFirestoreException.code)
  }

  @Test
  fun unauthenticatedObserver_deniedBySecurityRules() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    auth.signOut()
    val repo = LedgerRepository(firestore, auth)

    var observeError: Throwable? = null
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeEntries(aliceUid).first()
      }
    } catch (e: Exception) {
      observeError = e
    }
    val firestoreException = checkNotNull(observeError?.unwrapFirestoreException()) {
      "Expected nested FirebaseFirestoreException, got $observeError"
    }
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
  }

  private companion object {
    const val ALICE_EMAIL = "alice_hisab@test.com"
    const val BOB_EMAIL = "bob_hisab@test.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
