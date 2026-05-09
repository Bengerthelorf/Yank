package homes.snaix.app.yank.ui.records

import androidx.annotation.StringRes
import homes.snaix.app.yank.R

/**
 * Single source of truth for the six *recordable* type discriminators that
 * the model is allowed to emit. The seventh schema discriminator, `notes`,
 * is intentionally excluded — Notes live on a dedicated screen and never
 * participate in chip filtering or color theming. Each enum value carries:
 *   - `discriminator`: the literal Chinese string that appears in the JSON
 *     contract (model output) and is stored in [HistoryEntity.type]. Do NOT
 *     localize — this is a domain value, not a UI string.
 *   - `labelRes`: the localized chip / card label.
 *
 * Color roles are looked up via [TypeColors.roleFor] so the per-type palette
 * lives in `ui/theme/TypeColors.kt` and never gets duplicated in callers.
 */
enum class RecordType(
    val discriminator: String,
    @StringRes val labelRes: Int,
) {
    Queue("排队", R.string.type_label_queue),
    Pickup("取餐", R.string.type_label_pickup),
    Voucher("券码", R.string.type_label_voucher),
    Express("快递", R.string.type_label_express),
    Ticket("票券", R.string.type_label_ticket),
    Todo("待办", R.string.type_label_todo);

    companion object {
        fun fromDiscriminator(s: String): RecordType? =
            entries.firstOrNull { it.discriminator == s }
    }
}
