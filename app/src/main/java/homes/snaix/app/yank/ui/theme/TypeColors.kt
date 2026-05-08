package homes.snaix.app.yank.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamicColorScheme
import com.materialkolor.PaletteStyle
import com.materialkolor.hct.Hct

@Immutable
data class TypeColorRole(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
)

@Immutable
data class TypeColors(
    val queue: TypeColorRole,
    val pickup: TypeColorRole,
    val voucher: TypeColorRole,
    val express: TypeColorRole,
    val ticket: TypeColorRole,
    val todo: TypeColorRole,
    val notesContainer: Color,
    val onNotesContainer: Color,
)

val LocalTypeColors = compositionLocalOf<TypeColors> { error("TypeColors not provided") }

private val TypeHues = mapOf(
    "queue" to 200.0,
    "pickup" to 140.0,
    "voucher" to 340.0,
    "express" to 240.0,
    "ticket" to 290.0,
    "todo" to 50.0,
)

@Composable
fun rememberTypeColorsFor(scheme: ColorScheme, isDark: Boolean): TypeColors = remember(scheme, isDark) {
    fun roleFor(hue: Double): TypeColorRole {
        val seed = Color(Hct.from(hue, 60.0, 60.0).toInt())
        val cs = dynamicColorScheme(
            seedColor = seed,
            isDark = isDark,
            isAmoled = false,
            style = PaletteStyle.TonalSpot,
        )
        return TypeColorRole(
            color = cs.primary,
            onColor = cs.onPrimary,
            container = cs.primaryContainer,
            onContainer = cs.onPrimaryContainer,
        )
    }
    TypeColors(
        queue = roleFor(TypeHues.getValue("queue")),
        pickup = roleFor(TypeHues.getValue("pickup")),
        voucher = roleFor(TypeHues.getValue("voucher")),
        express = roleFor(TypeHues.getValue("express")),
        ticket = roleFor(TypeHues.getValue("ticket")),
        todo = roleFor(TypeHues.getValue("todo")),
        notesContainer = scheme.surfaceVariant,
        onNotesContainer = scheme.onSurfaceVariant,
    )
}
