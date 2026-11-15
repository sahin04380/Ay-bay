package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val HindSiliguriFamily = FontFamily(
  Font(R.font.hind_siliguri, FontWeight.Normal),
  Font(R.font.hind_siliguri, FontWeight.Medium),
  Font(R.font.hind_siliguri, FontWeight.SemiBold),
  Font(R.font.hind_siliguri, FontWeight.Bold),
)

val NotoSansBengaliFamily = FontFamily(
  Font(R.font.noto_sans_bengali, FontWeight.Normal),
  Font(R.font.noto_sans_bengali, FontWeight.Medium),
  Font(R.font.noto_sans_bengali, FontWeight.SemiBold),
  Font(R.font.noto_sans_bengali, FontWeight.Bold),
)

val HisabTypography = Typography(
  displaySmall = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp,
  ),
  headlineMedium = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp,
    lineHeight = 32.sp,
  ),
  headlineSmall = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 20.sp,
    lineHeight = 28.sp,
  ),
  titleLarge = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 19.sp,
    lineHeight = 26.sp,
  ),
  titleMedium = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 16.sp,
    lineHeight = 22.sp,
  ),
  titleSmall = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  bodyLarge = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
  ),
  bodyMedium = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp,
    lineHeight = 20.sp,
  ),
  bodySmall = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
  labelLarge = TextStyle(
    fontFamily = HindSiliguriFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
  ),
  labelMedium = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 12.sp,
    lineHeight = 16.sp,
  ),
  labelSmall = TextStyle(
    fontFamily = NotoSansBengaliFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
  ),
)
