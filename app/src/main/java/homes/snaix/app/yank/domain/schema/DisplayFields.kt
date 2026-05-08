// app/src/main/java/homes/snaix/app/yank/domain/schema/DisplayFields.kt
package homes.snaix.app.yank.domain.schema

fun Recognition.displayPrimary(): String = when (this) {
    is Recognition.Queue -> number.orEmpty()
    is Recognition.Pickup -> number.orEmpty()
    is Recognition.Voucher -> number.orEmpty()
    is Recognition.Express -> number.orEmpty()
    is Recognition.Ticket -> when (subType) {
        TicketSubType.TRAIN -> trainNo.orEmpty()
        TicketSubType.FLIGHT -> flightNo.orEmpty()
        TicketSubType.MOVIE -> movie.orEmpty()
        TicketSubType.GENERIC -> store ?: movie ?: time.orEmpty()
    }
    is Recognition.Todo -> title
    is Recognition.Note -> title ?: number?.take(20).orEmpty()
}

fun Recognition.displaySecondary(): String? = when (this) {
    is Recognition.Queue -> listOfNotNull(brand, store).joinToString(" · ").ifEmpty { null }
    is Recognition.Pickup -> listOfNotNull(brand, store).joinToString(" · ").ifEmpty { null }
    is Recognition.Voucher -> store
    is Recognition.Express -> station ?: address
    is Recognition.Ticket -> when (subType) {
        TicketSubType.TRAIN -> "${fromStation.orEmpty()} → ${toStation.orEmpty()}".takeIf { fromStation != null || toStation != null }
        TicketSubType.FLIGHT -> "${departureAirport.orEmpty()} → ${arrivalAirport.orEmpty()}".takeIf { departureAirport != null || arrivalAirport != null }
        TicketSubType.MOVIE -> store
        TicketSubType.GENERIC -> "$date $time".trim()
    }
    is Recognition.Todo -> "$date $time"
    is Recognition.Note -> date?.let { "$it ${time.orEmpty()}".trim() }
}
