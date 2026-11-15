package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.preferences.ThemePreferences
import com.example.data.repository.LedgerRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.auth.signOutUser
import com.example.ui.ledger.HisabHomeScreen
import com.example.ui.ledger.LedgerViewModel
import com.example.ui.theme.HisabTheme
import com.example.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val context = LocalContext.current
      val themePrefs = remember(context) { ThemePreferences(context) }
      val themeMode by themePrefs.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
      val systemDark = isSystemInDarkTheme()
      val scope = rememberCoroutineScope()

      HisabTheme(themeMode = themeMode) {
        AppRoot(
          themeMode = themeMode,
          onToggleTheme = {
            val currentlyDark = when (themeMode) {
              ThemeMode.DARK -> true
              ThemeMode.LIGHT -> false
              ThemeMode.SYSTEM -> systemDark
            }
            val nextMode = if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
            scope.launch { themePrefs.setThemeMode(nextMode) }
          },
        )
      }
    }
  }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

@Composable
private fun AppRoot(
  themeMode: ThemeMode,
  onToggleTheme: () -> Unit,
  auth: FirebaseAuth = Firebase.auth,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {},
      onUnauthenticated = {},
      scope = scope,
    )
  }

  val user = currentUser
  if (user == null) {
    AuthScreen(onAuthSuccess = {})
  } else {
    val uid = user.uid
    key(uid) {
      AuthorizedLedgerRoot(
        currentUserId = uid,
        themeMode = themeMode,
        onToggleTheme = onToggleTheme,
        onSignOutClick = {
          signOutUser(
            credentialManager = credentialManager,
            scope = scope,
          )
        },
      )
    }
  }
}

@Composable
private fun AuthorizedLedgerRoot(
  currentUserId: String,
  themeMode: ThemeMode,
  onToggleTheme: () -> Unit,
  onSignOutClick: () -> Unit,
) {
  val viewModel: LedgerViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val db = FirebaseFirestore.getInstance(databaseId)
        LedgerViewModel(
          repository = LedgerRepository(db, Firebase.auth),
          currentUserId = currentUserId,
        )
      }
    },
  )

  HisabHomeScreen(
    viewModel = viewModel,
    themeMode = themeMode,
    onToggleTheme = onToggleTheme,
    onSignOutClick = onSignOutClick,
  )
}
