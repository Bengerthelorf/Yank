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
    is Recognition.Note -> title ?: body?.lineSequence()?.firstOrNull()?.take(40).orEmpty()
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
        TicketSubType.GENERIC -> listOfNotNull(date, time).joinToString(" ").ifEmpty { null }
    }
    is Recognition.Todo -> "$date $time"
    is Recognition.Note -> {
        // Prefer the body's first line as the secondary preview when a title
        // is present (so the card actually shows summary content). Fall back
        // to date/time when there's no body to preview.
        val bodyLine = body?.lineSequence()?.firstOrNull()?.trim()?.take(80)
        when {
            !title.isNullOrBlank() && !bodyLine.isNullOrBlank() -> bodyLine
            !date.isNullOrBlank() -> "$date ${time.orEmpty()}".trim()
            else -> null
        }
    }
}
