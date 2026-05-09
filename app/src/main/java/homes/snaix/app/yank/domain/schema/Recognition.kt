package homes.snaix.app.yank.domain.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class Recognition {

    @Serializable @SerialName("排队")
    data class Queue(
        val number: String? = null,
        val store: String? = null,
        val brand: String? = null,
        val price: String? = null,
    ) : Recognition()

    @Serializable @SerialName("取餐")
    data class Pickup(
        val number: String? = null,
        val store: String? = null,
        val brand: String? = null,
        val product: String? = null,
        val price: String? = null,
    ) : Recognition()

    @Serializable @SerialName("券码")
    data class Voucher(
        val number: String? = null,
        val store: String? = null,
        val price: String? = null,
    ) : Recognition()

    @Serializable @SerialName("快递")
    data class Express(
        val number: String? = null,
        val brand: String? = null,
        val address: String? = null,
        val station: String? = null,
        val tracking: String? = null,
        val remark: String? = null,
    ) : Recognition()

    @Serializable @SerialName("票券")
    data class Ticket(
        // train
        val trainNo: String? = null,
        val fromStation: String? = null,
        val toStation: String? = null,
        val trainDate: String? = null,
        val trainDepartTime: String? = null,
        val trainArrivalTime: String? = null,
        val carriageNo: String? = null,
        val trainSeatNo: String? = null,
        // flight
        val flightNo: String? = null,
        val departureAirport: String? = null,
        val arrivalAirport: String? = null,
        val departDate: String? = null,
        val boardingTime: String? = null,
        val departTime: String? = null,
        val arrivalTime: String? = null,
        val seatNo: String? = null,
        // movie / generic
        val store: String? = null,
        val movie: String? = null,
        val date: String? = null,
        val time: String? = null,
        val seats: List<String>? = null,
        val theater: String? = null,
        // shared
        val gate: String? = null,
        val price: String? = null,
    ) : Recognition()

    @Serializable @SerialName("待办")
    data class Todo(
        val title: String,
        val date: String,
        val time: String,
        val pinLeadMinutes: Int = 60,
        val location: String? = null,
        val remark: String? = null,
    ) : Recognition()

    @Serializable @SerialName("notes")
    data class Note(
        val title: String? = null,
        // Wire name was historically `number`; @JsonNames keeps old rawJson parsing.
        @SerialName("body")
        @kotlinx.serialization.json.JsonNames("body", "number")
        val body: String? = null,
        val date: String? = null,
        val time: String? = null,
    ) : Recognition()
}

// Computed extension, not a property — a stored property would collide with
// the @JsonClassDiscriminator("type") field when encoding.
val Recognition.type: String
    get() = when (this) {
        is Recognition.Queue   -> "排队"
        is Recognition.Pickup  -> "取餐"
        is Recognition.Voucher -> "券码"
        is Recognition.Express -> "快递"
        is Recognition.Ticket  -> "票券"
        is Recognition.Todo    -> "待办"
        is Recognition.Note    -> "notes"
    }
