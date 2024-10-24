package com.coco.domain.clock

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

val ClockLocal = ThreadLocal<Clock?>()

fun currentInstant(): Instant = ClockLocal.get()?.instant() ?: Instant.now()

fun currentClock(): Clock = ClockLocal.get() ?: Clock.system(ZoneId.of("Asia/Seoul"))
