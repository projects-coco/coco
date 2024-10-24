package com.coco.infra

import arrow.core.Option
import com.coco.domain.core.bindOrNull
import com.coco.domain.core.toOption
import com.coco.domain.model.BinaryId
import com.coco.domain.model.SearchDtoBase
import com.coco.infra.dao.SearchRepositoryBase
import com.jooq.tables.records.SampleRecord
import com.jooq.tables.references.SAMPLE_
import org.jooq.impl.DSL
import ulid.ULID

data class SampleSearchDto(
    val changeableValue1: Option<String> = null.toOption(),
    val changeableValue2: Option<String> = null.toOption(),
    val changeableValue3: Option<String> = null.toOption(),
    val int1MinValue: Option<Int> = null.toOption(),
    val int1MaxValue: Option<Int> = null.toOption(),
    val int2EqualValue: Option<Int> = null.toOption(),
) : SearchDtoBase

object SampleSearchRepositoryImpl : SearchRepositoryBase<BinaryId, Sample, SampleRecord, SampleSearchDto>(
    table = SAMPLE_,
    toJooq = {
        SampleRecord(
            id = id.value.toBytes(),
            changeableValue1 = changeableValue1,
            changeableValue2 = changeableValue2,
            changeableValue3 = changeableValue3,
            changeableInt1 = changeableInt1,
            changeableInt2 = changeableInt2,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    },
    toDomain = {
        Sample(
            BinaryId.unsafeMake(ULID.fromBytes(id)),
            changeableValue1 = changeableValue1,
            changeableValue2 = changeableValue2,
            changeableValue3 = changeableValue3,
            changeableInt1 = changeableInt1,
            changeableInt2 = changeableInt2,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    },
    selectConditionBuilder = {
        DSL
            .trueCondition()
            .and(
                changeableValue1.bindOrNull {
                    SAMPLE_.CHANGEABLE_VALUE1
                        .eq(it)
                },
            ).and(
                changeableValue2.bindOrNull {
                    SAMPLE_.CHANGEABLE_VALUE2
                        .eq(it)
                },
            ).and(
                changeableValue3.bindOrNull {
                    SAMPLE_.CHANGEABLE_VALUE3
                        .eq(it)
                },
            ).and(
                int1MinValue.bindOrNull {
                    SAMPLE_.CHANGEABLE_INT1
                        .greaterOrEqual(it)
                },
            ).and(
                int1MaxValue.bindOrNull {
                    SAMPLE_.CHANGEABLE_INT1
                        .lessOrEqual(it)
                },
            ).and(
                int2EqualValue.bindOrNull {
                    SAMPLE_.CHANGEABLE_INT2
                        .eq(it)
                },
            )
    },
)
