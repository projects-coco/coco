package com.coco.domain.utils

import com.coco.domain.clock.currentClock
import java.time.LocalDateTime

fun nullSafeLocalDateTime(payload: LocalDateTime?): LocalDateTime = payload ?: LocalDateTime.now(currentClock())
