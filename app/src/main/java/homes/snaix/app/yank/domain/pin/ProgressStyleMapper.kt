package homes.snaix.app.yank.domain.pin

import homes.snaix.app.yank.domain.schema.Recognition

data class ProgressConfig(
    val percent: Int,
    val isStatic: Boolean,
    val anchorEventTime: Long?,
    val pinTime: Long?,
)

object ProgressStyleMapper {
    fun configFor(r: Recognition, eventTime: Long?, pinTime: Long?, now: Long): ProgressConfig = when (r) {
        is Recognition.Ticket, is Recognition.Todo -> {
            val anchor = eventTime
            if (anchor == null || pinTime == null) ProgressConfig(50, true, null, null)
            else {
                val span = (anchor - pinTime).coerceAtLeast(1)
                val elapsed = (now - pinTime).coerceIn(0, span)
                ProgressConfig(((elapsed * 100) / span).toInt(), false, anchor, pinTime)
            }
        }
        else -> ProgressConfig(50, true, null, null)
    }
}
