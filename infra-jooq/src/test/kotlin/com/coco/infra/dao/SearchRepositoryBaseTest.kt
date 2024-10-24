package com.coco.infra.dao

import arrow.core.Option
import arrow.core.toOption
import com.coco.domain.core.Page
import com.coco.domain.core.PageRequest
import com.coco.domain.core.bindOrNull
import com.coco.domain.core.toOption
import com.coco.domain.model.BinaryId
import com.coco.domain.model.SearchDtoBase
import com.coco.domain.shouldSucceed
import com.coco.infra.DatabaseTestContainer.installDatabase
import com.coco.infra.Sample
import com.coco.infra.genSampleEntity
import com.jooq.tables.records.SampleRecord
import com.jooq.tables.references.SAMPLE_
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkObject
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jooq.impl.DSL
import ulid.ULID
import java.time.Duration
import kotlin.time.toKotlinDuration

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
            .and(changeableValue1.bindOrNull { SAMPLE_.CHANGEABLE_VALUE1.eq(it) })
            .and(changeableValue2.bindOrNull { SAMPLE_.CHANGEABLE_VALUE2.eq(it) })
            .and(changeableValue3.bindOrNull { SAMPLE_.CHANGEABLE_VALUE3.eq(it) })
            .and(int1MinValue.bindOrNull { SAMPLE_.CHANGEABLE_INT1.greaterOrEqual(it) })
            .and(int1MaxValue.bindOrNull { SAMPLE_.CHANGEABLE_INT1.lessOrEqual(it) })
            .and(int2EqualValue.bindOrNull { SAMPLE_.CHANGEABLE_INT2.eq(it) })
    },
)

class SimpleCrudRepositoryBaseTest :
    FunSpec({
        installDatabase()

        val trashValue = "trashValue"
        val value1 = "value1"
        val value2 = "value2"

        afterEach {
            clearAllMocks()
        }

        test("SimpleCrudRepository::retrieveAll(List<ID>) 테스트") {
            val sampleEntity1 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity2 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity3 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity4 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity5 = genSampleEntity(changeableValue1 = value1)
            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity3).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity4).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity5).shouldSucceed()

            val ids = listOf(sampleEntity1.id, sampleEntity2.id, sampleEntity3.id)
            val samples = SampleSearchRepositoryImpl.retrieveAll(ids).shouldSucceed()
            samples.size shouldBe 3
            samples[0].changeableValue1 shouldBe value1
            samples[1].changeableValue1 shouldBe value1
            samples[2].changeableValue1 shouldBe value1
        }

        test("search test - 하나의 field로 검색") {
            val sampleEntity1 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity2 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity3 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity4 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity5 = genSampleEntity(changeableValue1 = value1)
            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity3).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity4).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity5).shouldSucceed()

            val searchDto = SampleSearchDto(changeableValue1 = value1.toOption())
            val pageRequest = PageRequest(pageSize = 10, pageNumber = 1, PageRequest.Sort.of(""))

            val searchEntity: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest).shouldSucceed()
            searchEntity.totalElements shouldBe 5
        }

        test("search test - 두 개의 필드로 검색(equal, min & max)") {
            val sampleEntity1 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity2 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity3 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity4 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 1)
            val sampleEntity5 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 1)
            val sampleEntity6 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 2)
            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity3).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity4).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity5).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity6).shouldSucceed()

            val searchDto =
                SampleSearchDto(
                    changeableValue1 = value1.toOption(),
                    int1MinValue = 1.toOption(),
                    int1MaxValue = 2.toOption(),
                )
            val pageRequest = PageRequest(10, 1, PageRequest.Sort.of(""))

            val searchEntity: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest).shouldSucceed()
            searchEntity.content.count() shouldBe 3
        }

        test("paging test - 두 개의 필드로 검색 & page,sort 검색") {
            mockkObject(Clock.System)

            val sampleEntity1 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity2 = genSampleEntity(changeableValue1 = trashValue)
            val sampleEntity3 = genSampleEntity(changeableValue1 = value1)
            val sampleEntity4 = genSampleEntity(changeableValue1 = trashValue, changeableInt1 = 1)
            val sampleEntity5 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 1)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(10).toKotlinDuration())
            val sampleEntity6 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 2)
            val sampleEntity7 = genSampleEntity(changeableValue1 = trashValue, changeableInt1 = 2)
            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity3).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity4).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity5).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity6).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity7).shouldSucceed()

            val searchDto =
                SampleSearchDto(
                    changeableValue1 = value1.toOption(),
                    int1MinValue = 1.toOption(),
                    int1MaxValue = 2.toOption(),
                )

            val pageRequest1 = PageRequest(3, 1, PageRequest.Sort.of("id, desc"))

            val search: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest1).shouldSucceed()
            search.totalElements shouldBe 2
            search.get(0).changeableInt1 shouldBe 2
            search.get(1).changeableInt1 shouldBe 1

            val pageRequest2 = PageRequest(3, 2, PageRequest.Sort.of("id, desc"))
            val emptySearch = SampleSearchRepositoryImpl.search(searchDto, pageRequest2).shouldSucceed()
            emptySearch.content.count() shouldBe 0
        }

        test("paging test - 세 개의 필드로 검색 & page,sort 검색") {
            mockkObject(Clock.System)

            val sampleEntity1 = genSampleEntity(changeableValue1 = value1, changeableValue2 = value2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(10).toKotlinDuration())
            val sampleEntity2 = genSampleEntity(changeableValue1 = trashValue, changeableValue2 = value2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(20).toKotlinDuration())
            val sampleEntity3 = genSampleEntity(changeableValue1 = value1, changeableValue2 = value2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(30).toKotlinDuration())
            val sampleEntity4 = genSampleEntity(changeableValue1 = trashValue, changeableInt1 = 1)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(40).toKotlinDuration())
            val sampleEntity5 =
                genSampleEntity(
                    changeableValue1 = value1,
                    changeableValue2 = value2,
                    changeableInt1 = 40,
                    changeableInt2 = 1,
                )

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(50).toKotlinDuration())
            val sampleEntity6 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(60).toKotlinDuration())
            val sampleEntity7 = genSampleEntity(changeableValue1 = trashValue, changeableInt1 = 2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(70).toKotlinDuration())
            val sampleEntity8 = genSampleEntity(changeableValue1 = value1, changeableValue2 = value2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(80).toKotlinDuration())
            val sampleEntity9 =
                genSampleEntity(changeableValue1 = value1, changeableValue2 = value2, changeableInt2 = 2)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(90).toKotlinDuration())
            val sampleEntity10 =
                genSampleEntity(
                    changeableValue1 = value1,
                    changeableValue2 = value2,
                    changeableInt1 = 20,
                    changeableInt2 = 2,
                )

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(100).toKotlinDuration())
            val sampleEntity11 =
                genSampleEntity(
                    changeableValue1 = value1,
                    changeableValue2 = value2,
                    changeableInt1 = 30,
                    changeableInt2 = 2,
                )

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(110).toKotlinDuration())
            val sampleEntity12 =
                genSampleEntity(
                    changeableValue1 = value1,
                    changeableValue2 = value2,
                    changeableInt1 = 10,
                    changeableInt2 = 2,
                )

            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity3).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity4).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity5).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity6).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity7).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity8).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity9).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity10).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity11).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity12).shouldSucceed()

            val searchDto =
                SampleSearchDto(
                    changeableValue1 = value1.toOption(),
                    changeableValue2 = value2.toOption(),
                    int2EqualValue = 2.toOption(),
                )

            val pageRequest1 = PageRequest(10, 1, PageRequest.Sort.of("id, desc"))

            val search: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest1).shouldSucceed()
            search.get(0)
            search.totalElements shouldBe 4
            search.get(0).changeableInt1 shouldBe 10
            search.get(1).changeableInt1 shouldBe 30
            search.get(2).changeableInt1 shouldBe 20
            search.get(3).changeableInt1 shouldBe null

            val pageRequest2 = PageRequest(2, 1, PageRequest.Sort.of("id, desc"))
            val search2: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest2).shouldSucceed()
            search2.content.count() shouldBe 2
            search2.get(0).changeableInt1 shouldBe 10
            search2.get(1).changeableInt1 shouldBe 30

            val pageRequest3 = PageRequest(2, 2, PageRequest.Sort.of("id, desc"))
            val search3: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest3).shouldSucceed()
            search3.content.count() shouldBe 2
            search3.get(0).changeableInt1 shouldBe 20
            search3.get(1).changeableInt1 shouldBe null
        }

        // Fuck it. This is test for test
        test("코드상 먼저 나오는 entity 앞에 Duration을 더 길게 주면, 실제로 ULID가 더 늦은 시간으로 생성되는지") {
            mockkObject(Clock.System)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(10).toKotlinDuration())
            val sampleEntity1 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 1)

            every { Clock.System.now() } returns
                @Suppress("DEPRECATION_ERROR")
                Instant
                    .now()
                    .plus(Duration.ofMinutes(1).toKotlinDuration())
            val sampleEntity2 = genSampleEntity(changeableValue1 = value1, changeableInt1 = 2)

            SampleSearchRepositoryImpl.save(sampleEntity1).shouldSucceed()
            SampleSearchRepositoryImpl.save(sampleEntity2).shouldSucceed()

            val searchDto = SampleSearchDto(changeableValue1 = value1.toOption())
            val pageRequest = PageRequest(10, 1, PageRequest.Sort.of("id, desc"))

            val searchEntity: Page<Sample> = SampleSearchRepositoryImpl.search(searchDto, pageRequest).shouldSucceed()
            searchEntity.content.count() shouldBe 2
            searchEntity.get(0).changeableInt1 shouldBe 1
            searchEntity.get(1).changeableInt1 shouldBe 2
        }
    })
