package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class ThemeMode {
  SYSTEM,
  LIGHT,
  DARK,
}

@Immutable
data class HisabSemanticColors(
  val income: Color,
  val incomeContainer: Color,
  val expense: Color,
  val expenseContainer: Color,
  val balance: Color,
  val balanceContainer: Color,
  val isDark: Boolean,
)

private val LightSemanticColors = HisabSemanticColors(
  income = IncomeLight,
  incomeContainer = IncomeSoftLight,
  expense = ExpenseLight,
  expenseContainer = ExpenseSoftLight,
  balance = BalanceLight,
  balanceContainer = BalanceSoftLight,
  isDark = false,
)

private val DarkSemanticColors = HisabSemanticColors(
  income = IncomeDark,
  incomeContainer = IncomeSoftDark,
  expense = ExpenseDark,
  expenseContainer = ExpenseSoftDark,
  balance = BalanceDark,
  balanceContainer = BalanceSoftDark,
  isDark = true,
)

val LocalHisabColors = staticCompositionLocalOf { LightSemanticColors }

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimaryLight,
  onPrimary = EmeraldOnPrimaryLight,
  primaryContainer = EmeraldContainerLight,
  onPrimaryContainer = EmeraldOnContainerLight,
  secondary = RoyalBlueSecondaryLight,
  onSecondary = RoyalBlueOnSecondaryLight,
  secondaryContainer = RoyalBlueContainerLight,
  onSecondaryContainer = RoyalBlueOnContainerLight,
  tertiary = WarmGoldTertiaryLight,
  tertiaryContainer = WarmGoldContainerLight,
  background = BgLight,
  onBackground = OnSurfaceLight,
  surface = SurfaceLight,
  onSurface = OnSurfaceLight,
  surfaceVariant = SurfaceVariantLight,
  onSurfaceVariant = OnSurfaceVariantLight,
  outline = OutlineLight,
  outlineVariant = OutlineLight,
  error = ExpenseLight,
)

private val DarkColorScheme = darkColorScheme(
  primary = EmeraldPrimaryDark,
  onPrimary = EmeraldOnPrimaryDark,
  primaryContainer = EmeraldContainerDark,
  onPrimaryContainer = EmeraldOnContainerDark,
  secondary = RoyalBlueSecondaryDark,
  onSecondary = RoyalBlueOnSecondaryDark,
  secondaryContainer = RoyalBlueContainerDark,
  onSecondaryContainer = RoyalBlueOnContainerDark,
  tertiary = WarmGoldTertiaryDark,
  tertiaryContainer = WarmGoldContainerDark,
  background = BgDark,
  onBackground = OnSurfaceDark,
  surface = SurfaceDark,
  onSurface = OnSurfaceDark,
  surfaceVariant = SurfaceVariantDark,
  onSurfaceVariant = OnSurfaceVariantDark,
  outline = OutlineDark,
  outlineVariant = OutlineDark,
  error = ExpenseDark,
)

@Composable
fun HisabTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val useDarkTheme = when (themeMode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }

  val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme
  val semanticColors = if (useDarkTheme) DarkSemanticColors else LightSemanticColors

  CompositionLocalProvider(LocalHisabColors provides semanticColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = HisabTypography,
      content = content,
    )
  }
}
