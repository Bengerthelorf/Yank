@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package homes.snaix.app.yank.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import homes.snaix.app.yank.R

private fun robotoFlex(weight: Int) = Font(
    resId = R.font.roboto_flex,
    weight = FontWeight(weight),
    style = FontStyle.Normal,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private val RobotoFlex = FontFamily(
    robotoFlex(300),
    robotoFlex(400),
    robotoFlex(500),
    robotoFlex(600),
    robotoFlex(700),
    robotoFlex(800),
)

val YankTypography = Typography(
    // M3E "expressive" type scale — favors stronger weight contrast for hierarchy.
    // Display: emotionally weighty, used sparingly (e.g. empty state hero, splash).
    displayLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W800, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W700, fontSize = 45.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W700, fontSize = 36.sp, lineHeight = 44.sp),
    // Headline: page titles + prominent section anchors.
    headlineLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W700, fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W700, fontSize = 28.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 24.sp, lineHeight = 32.sp),
    // Title: card / list-item headers, dialog titles.
    titleLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    // Body: paragraphs and most readable copy.
    bodyLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W400, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W400, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    // Label: chips, segmented buttons, tab labels.
    labelLarge = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = RobotoFlex, fontWeight = FontWeight.W600, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
)
