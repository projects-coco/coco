package com.coco.infra

import com.coco.domain.model.BinaryId
import com.coco.domain.model.EntityBase
import java.time.LocalDateTime

class Sample(
    id: BinaryId,
    changeableValue1: String? = null,
    changeableValue2: String? = null,
    changeableValue3: String? = null,
    changeableInt1: Int? = null,
    changeableInt2: Int? = null,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) : EntityBase<BinaryId>(id, createdAt, updatedAt) {
    var changeableValue1 = changeableValue1
    var changeableValue2 = changeableValue2
    var changeableValue3 = changeableValue3
    var changeableInt1 = changeableInt1
    var changeableInt2 = changeableInt2
}

fun genSampleEntity(
    changeableValue1: String? = null,
    changeableValue2: String? = null,
    changeableValue3: String? = null,
    changeableInt1: Int? = null,
    changeableInt2: Int? = null,
) = Sample(
    BinaryId.new(),
    changeableValue1 = changeableValue1,
    changeableValue2 = changeableValue2,
    changeableValue3 = changeableValue3,
    changeableInt1 = changeableInt1,
    changeableInt2 = changeableInt2,
    LocalDateTime.now(),
    LocalDateTime.now(),
)
