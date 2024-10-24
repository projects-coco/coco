package com.coco.infra.core

import com.coco.domain.shouldSucceed
import com.coco.infra.DatabaseTestContainer.installDatabase
import com.coco.infra.SampleSearchRepositoryImpl
import com.coco.infra.genSampleEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.assertFails
import kotlin.test.fail

class JooqAtomicityTest :
    FunSpec({
        installDatabase()

        val atomicity = JooqAtomicity()

        test("transaction fail test") {
            val beforeValue = "before"
            val sampleEntity = genSampleEntity(changeableValue1 = beforeValue)
            SampleSearchRepositoryImpl.save(sampleEntity).shouldSucceed()
            val changeValue = "after"
            assertFails {
                atomicity {
                    sampleEntity.changeableValue1 = changeValue
                    val newSaved = SampleSearchRepositoryImpl.save(sampleEntity)
                    newSaved.shouldSucceed()
                    fail("transaction is failed")
                }
            }

            val newSample = SampleSearchRepositoryImpl.retrieve(sampleEntity.id).shouldSucceed()
            newSample.changeableValue1 shouldBe beforeValue
            newSample.changeableValue1 shouldNotBe changeValue
        }

        test("transaction succeed test") {
            val ref1 = genSampleEntity("before")
            val changeValue = "after"

            atomicity {
                ref1.changeableValue1 = changeValue
                val newSaved = SampleSearchRepositoryImpl.save(ref1)
                newSaved.shouldSucceed()
            }

            val ref2 = SampleSearchRepositoryImpl.retrieve(ref1.id).shouldSucceed()
            ref2.changeableValue1 shouldBe changeValue
        }
    })
