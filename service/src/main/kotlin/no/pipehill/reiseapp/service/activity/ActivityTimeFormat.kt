package no.pipehill.reiseapp.service.activity

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val activityTimeFormat = DateTimeFormatter.ofPattern("d.MMM HH:mm", Locale.ENGLISH)

internal fun LocalDateTime.toActivityTimeString(): String =
    format(activityTimeFormat).lowercase(Locale.ROOT)
