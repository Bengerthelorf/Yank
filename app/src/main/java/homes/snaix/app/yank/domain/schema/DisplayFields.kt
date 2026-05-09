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
        val bodyLine = body?.lineSequence()?.firstOrNull()?.trim()?.take(80)
        when {
            !title.isNullOrBlank() && !bodyLine.isNullOrBlank() -> bodyLine
            !date.isNullOrBlank() -> "$date ${time.orEmpty()}".trim()
            else -> null
        }
    }
}

fun Recognition.withPrimary(newPrimary: String): Recognition = when (this) {
    is Recognition.Queue   -> copy(number = newPrimary)
    is Recognition.Pickup  -> copy(number = newPrimary)
    is Recognition.Voucher -> copy(number = newPrimary)
    is Recognition.Express -> copy(number = newPrimary)
    is Recognition.Ticket -> when (subType) {
        TicketSubType.TRAIN   -> copy(trainNo = newPrimary)
        TicketSubType.FLIGHT  -> copy(flightNo = newPrimary)
        TicketSubType.MOVIE   -> copy(movie = newPrimary)
        TicketSubType.GENERIC -> copy(store = newPrimary)
    }
    is Recognition.Todo -> copy(title = newPrimary)
    is Recognition.Note -> copy(title = newPrimary)
}

fun Recognition.rawTextBlob(): String = when (this) {
    is Recognition.Queue   -> listOfNotNull(number, store, brand, price).joinToString(" ")
    is Recognition.Pickup  -> listOfNotNull(number, store, brand, product, price).joinToString(" ")
    is Recognition.Voucher -> listOfNotNull(number, store, price).joinToString(" ")
    is Recognition.Express -> listOfNotNull(number, brand, address, station, tracking, remark).joinToString(" ")
    is Recognition.Ticket  -> listOfNotNull(trainNo, fromStation, toStation, flightNo, departureAirport, arrivalAirport, store, movie, date, time, gate).joinToString(" ")
    is Recognition.Todo    -> listOfNotNull(title, date, time, location, remark).joinToString(" ")
    is Recognition.Note    -> listOfNotNull(title, body, date, time).joinToString(" ")
}
