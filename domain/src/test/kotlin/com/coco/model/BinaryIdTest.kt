package com.coco.model

import com.coco.domain.model.BinaryId
import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BinaryIdTest : FunSpec({
    test("BinaryId::equals") {
        val binaryId1 = BinaryId.new()
        val binaryId2 = BinaryId.unsafeMake(binaryId1.value)
        val binaryId3 = BinaryId.unsafeMake(binaryId1.value.toBytes())
        assertEquals(binaryId1, binaryId2)
        assertEquals(binaryId1, binaryId3)
        assertTrue { binaryId1 == binaryId2 }
        assertTrue { binaryId1 == binaryId3 }
    }
})
