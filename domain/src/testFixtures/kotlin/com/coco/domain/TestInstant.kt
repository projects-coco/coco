package com.coco.domain

import com.coco.domain.clock.ClockLocal
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class TestInstant(private var timeSource: () -> Instant) : Clock() {
    companion object {
        fun setFixedTime(instant: Instant) = TestInstant { instant }.also(ClockLocal::set)

        fun setCurrent() = TestInstant { Instant.now() }.also(ClockLocal::set)

        fun current() = ClockLocal.get() as TestInstant? ?: setCurrent()

        fun adjust(duration: Duration): Instant {
            val prev = current().timeSource
            return { prev().plus(duration.toJavaDuration()) }.also { current().timeSource = it }()
        }

        fun reset() = { Instant.now() }.also { current().timeSource = it }()

        fun fixedTime(instant: Instant) = { instant }.also { current().timeSource = it }
    }

    override fun instant(): Instant = timeSource()

    override fun withZone(zone: ZoneId?): Clock = throw UnsupportedOperationException()

    override fun getZone(): ZoneId = ZoneOffset.UTC
}
