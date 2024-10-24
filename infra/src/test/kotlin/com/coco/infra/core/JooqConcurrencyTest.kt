package com.coco.infra.core

import arrow.core.raise.get
import arrow.core.toOption
import com.coco.domain.core.bindOrNothing
import com.coco.domain.shouldSucceed
import com.coco.infra.DatabaseTestContainer.installDatabase
import com.coco.infra.Sample
import com.coco.infra.SampleSearchDto
import com.coco.infra.SampleSearchRepositoryImpl
import com.coco.infra.genSampleEntity
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class JooqConcurrencyTest :
    FunSpec({
        installDatabase()
        beforeEach {
            SampleSearchRepositoryImpl.retrieveAll().get().forEach {
                SampleSearchRepositoryImpl.delete(it).bindOrNothing()
            }
            SampleSearchRepositoryImpl.save(genSampleEntity(changeableValue1 = "value1")).bindOrNothing()
        }
        test("Jooq Concurrency test - 1") {
            val v1: Sample =
                SampleSearchRepositoryImpl
                    .search(
                        SampleSearchDto(
                            changeableValue1 = "value1".toOption(),
                        ),
                    ).bindOrNothing()!![0]
            val v2: Sample =
                SampleSearchRepositoryImpl
                    .search(
                        SampleSearchDto(
                            changeableValue1 = "value1".toOption(),
                        ),
                    ).bindOrNothing()!![0]

            v1.changeableValue1 = "value1-1"
            SampleSearchRepositoryImpl.save(v1).shouldSucceed()
            v2.changeableValue1 = "value1-2"
            val exception =
                shouldThrow<RuntimeException> {
                    SampleSearchRepositoryImpl.save(v2).bindOrNothing()
                }
            exception.message shouldBe
                "Concurrency error: The data you were attempting to edit has been modified by another operation. Please refresh and retry."
        }
        test("Jooq Concurrency test - 2") {
            val v1: Sample =
                SampleSearchRepositoryImpl
                    .search(
                        SampleSearchDto(
                            changeableValue1 = "value1".toOption(),
                        ),
                    ).bindOrNothing()!![0]
            this.launch {
                val atomicity = JooqAtomicity()
                atomicity {
                    val ref1 = SampleSearchRepositoryImpl.retrieveForUpdate(v1.id).shouldSucceed()
                    delay(200)
                    ref1.changeableValue1 = "value1-1"
                    SampleSearchRepositoryImpl.save(ref1, skipLock = true).shouldSucceed()
                }
            }
            this.launch {
                delay(50)
                val atomicity = JooqAtomicity()
                atomicity {
                    val ref2 = SampleSearchRepositoryImpl.retrieveForUpdate(v1.id).shouldSucceed()
                    ref2.changeableValue1.shouldBe("value1-1")
                }
            }
        }
    })
