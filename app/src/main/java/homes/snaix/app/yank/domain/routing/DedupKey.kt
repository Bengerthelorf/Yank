package homes.snaix.app.yank.domain.routing

import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.schema.TicketSubType
import homes.snaix.app.yank.domain.schema.subType
import java.util.UUID

fun dedupKey(r: Recognition): String = when (r) {
    is Recognition.Queue   -> "排队|${r.brand.orEmpty()}|${r.number.orEmpty()}"
    is Recognition.Pickup  -> "取餐|${r.brand.orEmpty()}|${r.number.orEmpty()}"
    is Recognition.Voucher -> "券码|${r.store.orEmpty()}|${r.number.orEmpty()}"
    is Recognition.Express -> "快递|${r.station.orEmpty()}|${r.number.orEmpty()}"
    is Recognition.Ticket  -> when (r.subType) {
        TicketSubType.TRAIN   -> "票券|火车|${r.trainNo.orEmpty()}|${r.trainDate.orEmpty()}"
        TicketSubType.FLIGHT  -> "票券|航班|${r.flightNo.orEmpty()}|${r.departDate.orEmpty()}"
        TicketSubType.MOVIE   -> "票券|电影|${r.movie.orEmpty()}|${r.date.orEmpty()}|${r.time.orEmpty()}"
        TicketSubType.GENERIC -> "票券|通用|${r.store.orEmpty()}|${r.date.orEmpty()}|${r.time.orEmpty()}"
    }
    is Recognition.Todo -> "待办|${r.title}|${r.date}|${r.time}"
    is Recognition.Note -> UUID.randomUUID().toString()  // notes never dedup
}
