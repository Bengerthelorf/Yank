// app/src/main/java/homes/snaix/app/yank/domain/schema/TicketSubType.kt
package homes.snaix.app.yank.domain.schema

enum class TicketSubType { TRAIN, FLIGHT, MOVIE, GENERIC }

val Recognition.Ticket.subType: TicketSubType
    get() = when {
        flightNo != null -> TicketSubType.FLIGHT
        trainNo != null -> TicketSubType.TRAIN
        movie != null -> TicketSubType.MOVIE
        else -> TicketSubType.GENERIC
    }
