package homes.snaix.app.yank.ui.records

import androidx.annotation.StringRes
import homes.snaix.app.yank.R

// Six recordable types. The seventh schema discriminator `notes` is
// intentionally excluded — Notes live on a separate screen and never
// participate in chip filtering or color theming.
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
