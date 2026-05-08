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
    val notes: TypeColorRole,
)

val LocalTypeColors = compositionLocalOf<TypeColors> { error("TypeColors not provided") }

private const val QueueHue = 200.0
private const val PickupHue = 140.0
private const val VoucherHue = 340.0
private const val ExpressHue = 240.0
private const val TicketHue = 290.0
private const val TodoHue = 50.0

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
        queue = roleFor(QueueHue),
        pickup = roleFor(PickupHue),
        voucher = roleFor(VoucherHue),
        express = roleFor(ExpressHue),
        ticket = roleFor(TicketHue),
        todo = roleFor(TodoHue),
        notes = TypeColorRole(
            color = scheme.onSurfaceVariant,
            onColor = scheme.surfaceVariant,
            container = scheme.surfaceVariant,
            onContainer = scheme.onSurfaceVariant,
        ),
    )
}
