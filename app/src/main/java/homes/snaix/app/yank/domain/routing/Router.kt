package homes.snaix.app.yank.domain.routing

import homes.snaix.app.yank.Defaults
import homes.snaix.app.yank.data.db.DedupEntity
import homes.snaix.app.yank.data.db.HistoryEntity
import homes.snaix.app.yank.data.db.Source
import homes.snaix.app.yank.data.repo.HistoryRepository
import homes.snaix.app.yank.domain.schema.Recognition
import homes.snaix.app.yank.domain.schema.TicketSubType
import homes.snaix.app.yank.domain.schema.displayPrimary
import homes.snaix.app.yank.domain.schema.displaySecondary
import homes.snaix.app.yank.domain.schema.subType
import homes.snaix.app.yank.domain.time.EventClock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

interface PinPublisher {
    suspend fun publish(history: HistoryEntity, notificationId: Int, recognition: Recognition, payload: String?)
    suspend fun cancel(notificationId: Int)
}

interface PinScheduler {
    suspend fun scheduleArchive(historyId: String, archiveAt: Long, notificationId: Int)
    suspend fun scheduleTodoPin(historyId: String, pinTime: Long, notificationId: Int)
    suspend fun cancelTodo(historyId: String)
}

class Router(
    private val repo: HistoryRepository,
    private val publisher: PinPublisher,
    private val scheduler: PinScheduler,
    private val clock: EventClock = EventClock(),
    private val nextNotificationId: () -> Int,
    private val now: () -> Long = System::currentTimeMillis,
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) {

    suspend fun route(
        results: List<Recognition>,
        screenshotPath: String?,
        zxingPayloads: List<String>,
        source: Source,
    ): List<ResultEvent> = results.map { route(it, screenshotPath, zxingPayloads, source) }

    private suspend fun route(
        r: Recognition,
        screenshotPath: String?,
        zxingPayloads: List<String>,
        source: Source,
    ): ResultEvent {
        val key = dedupKey(r)
        val tNow = now()
        val existing = repo.findFreshDedup(key, tNow - Defaults.DEDUP_WINDOW_MS)
        val nid = existing?.notificationId ?: nextNotificationId()
        val historyId = existing?.historyId ?: UUID.randomUUID().toString()

        val eventTimeMs = computeEventTime(r)
        val archiveAtMs = computeArchiveAt(r, tNow, eventTimeMs)

        val entity = HistoryEntity(
            id = historyId,
            type = r.type,
            displayPrimary = r.displayPrimary(),
            displaySecondary = r.displaySecondary(),
            rawText = r.rawTextBlob(),
            rawJson = json.encodeToString(r),
            zxingPayloads = zxingPayloads.takeIf { it.isNotEmpty() }?.joinToString(","),
            screenshotPath = screenshotPath,
            createdAt = tNow,
            eventTime = eventTimeMs,
            archiveAt = archiveAtMs,
            archived = (r is Recognition.Todo && eventTimeMs != null && eventTimeMs < tNow),
            source = source,
        )
        repo.upsert(entity)
        if (existing == null) repo.insertDedup(DedupEntity(key, nid, historyId, tNow))
        else repo.touchDedup(key, tNow)

        return when (r) {
            is Recognition.Note -> ResultEvent.NoteSaved(historyId)
            is Recognition.Todo -> {
                if (entity.archived) {
                    ResultEvent.TodoArchivedExpired(historyId)
                } else {
                    val pinAt = clock.pinTime(eventTimeMs!!, r.pinLeadMinutes)
                    if (pinAt <= tNow) {
                        publisher.publish(entity, nid, r, zxingPayloads.firstOrNull())
                        scheduler.scheduleArchive(historyId, archiveAtMs, nid)
                        ResultEvent.PinPublished(historyId, nid)
                    } else {
                        scheduler.cancelTodo(historyId)
                        scheduler.scheduleTodoPin(historyId, pinAt, nid)
                        scheduler.scheduleArchive(historyId, archiveAtMs, nid)
                        ResultEvent.TodoScheduled(historyId, pinAt)
                    }
                }
            }
            else -> {
                publisher.publish(entity, nid, r, zxingPayloads.firstOrNull())
                scheduler.scheduleArchive(historyId, archiveAtMs, nid)
                if (existing == null) ResultEvent.PinPublished(historyId, nid)
                else ResultEvent.DedupReplaced(historyId, nid)
            }
        }
    }

    private fun computeEventTime(r: Recognition): Long? = when (r) {
        is Recognition.Ticket -> when (r.subType) {
            TicketSubType.TRAIN  -> clock.toEpochMillis(r.trainDate, r.trainDepartTime)
            TicketSubType.FLIGHT -> clock.toEpochMillis(r.departDate, r.boardingTime ?: r.departTime)
            TicketSubType.MOVIE  -> clock.toEpochMillis(r.date, r.time)
            TicketSubType.GENERIC -> clock.toEpochMillis(r.date, r.time)
        }
        is Recognition.Todo -> clock.toEpochMillis(r.date, r.time)
        else -> null
    }

    private fun computeArchiveAt(r: Recognition, tNow: Long, eventTime: Long?): Long {
        val oneHour = 60 * 60_000L
        return when (r) {
            is Recognition.Queue, is Recognition.Pickup, is Recognition.Voucher ->
                tNow + 6 * oneHour
            is Recognition.Express ->
                tNow + 48 * oneHour
            is Recognition.Ticket, is Recognition.Todo ->
                (eventTime ?: tNow) + oneHour
            is Recognition.Note ->
                tNow + Long.MAX_VALUE / 2  // notes never auto-archive
        }
    }
}

private fun Recognition.rawTextBlob(): String = when (this) {
    is Recognition.Queue   -> listOfNotNull(number, store, brand, price).joinToString(" ")
    is Recognition.Pickup  -> listOfNotNull(number, store, brand, product, price).joinToString(" ")
    is Recognition.Voucher -> listOfNotNull(number, store, price).joinToString(" ")
    is Recognition.Express -> listOfNotNull(number, brand, address, station, tracking, remark).joinToString(" ")
    is Recognition.Ticket  -> listOfNotNull(trainNo, fromStation, toStation, flightNo, departureAirport, arrivalAirport, store, movie, date, time, gate).joinToString(" ")
    is Recognition.Todo    -> listOfNotNull(title, date, time, location, remark).joinToString(" ")
    is Recognition.Note    -> listOfNotNull(title, number, date, time).joinToString(" ")
}
