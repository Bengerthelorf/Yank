// app/src/main/java/homes/snaix/app/yank/domain/schema/Recognition.kt
package homes.snaix.app.yank.domain.schema

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
sealed class Recognition {
    abstract val type: String

    @Serializable @SerialName("排队")
    data class Queue(
        val number: String? = null,
        val store: String? = null,
        val brand: String? = null,
        val price: String? = null,
    ) : Recognition() { override val type = "排队" }

    @Serializable @SerialName("取餐")
    data class Pickup(
        val number: String? = null,
        val store: String? = null,
        val brand: String? = null,
        val product: String? = null,
        val price: String? = null,
    ) : Recognition() { override val type = "取餐" }

    @Serializable @SerialName("券码")
    data class Voucher(
        val number: String? = null,
        val store: String? = null,
        val price: String? = null,
    ) : Recognition() { override val type = "券码" }

    @Serializable @SerialName("快递")
    data class Express(
        val number: String? = null,
        val brand: String? = null,
        val address: String? = null,
        val station: String? = null,
        val tracking: String? = null,
        val remark: String? = null,
    ) : Recognition() { override val type = "快递" }

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
    ) : Recognition() { override val type = "票券" }

    @Serializable @SerialName("待办")
    data class Todo(
        val title: String,
        val date: String,
        val time: String,
        val pinLeadMinutes: Int = 60,
        val location: String? = null,
        val remark: String? = null,
    ) : Recognition() { override val type = "待办" }

    @Serializable @SerialName("notes")
    data class Note(
        val title: String? = null,
        val number: String? = null,
        val date: String? = null,
        val time: String? = null,
    ) : Recognition() { override val type = "notes" }
}
