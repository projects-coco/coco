/*
 * This file is generated by jOOQ.
 */
package com.jooq.tables.records


import com.jooq.tables.Sample

import java.time.LocalDateTime

import org.jooq.Record1
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class SampleRecord private constructor() : UpdatableRecordImpl<SampleRecord>(Sample.SAMPLE_) {

    open var id: ByteArray
        set(value): Unit = set(0, value)
        get(): ByteArray = get(0) as ByteArray

    open var changeableValue1: String?
        set(value): Unit = set(1, value)
        get(): String? = get(1) as String?

    open var changeableValue2: String?
        set(value): Unit = set(2, value)
        get(): String? = get(2) as String?

    open var changeableValue3: String?
        set(value): Unit = set(3, value)
        get(): String? = get(3) as String?

    open var changeableInt1: Int?
        set(value): Unit = set(4, value)
        get(): Int? = get(4) as Int?

    open var changeableInt2: Int?
        set(value): Unit = set(5, value)
        get(): Int? = get(5) as Int?

    open var createdAt: LocalDateTime
        set(value): Unit = set(6, value)
        get(): LocalDateTime = get(6) as LocalDateTime

    open var updatedAt: LocalDateTime
        set(value): Unit = set(7, value)
        get(): LocalDateTime = get(7) as LocalDateTime

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<ByteArray?> = super.key() as Record1<ByteArray?>

    /**
     * Create a detached, initialised SampleRecord
     */
    constructor(id: ByteArray, changeableValue1: String?, changeableValue2: String?, changeableValue3: String?, changeableInt1: Int?, changeableInt2: Int?, createdAt: LocalDateTime, updatedAt: LocalDateTime): this() {
        this.id = id
        this.changeableValue1 = changeableValue1
        this.changeableValue2 = changeableValue2
        this.changeableValue3 = changeableValue3
        this.changeableInt1 = changeableInt1
        this.changeableInt2 = changeableInt2
        this.createdAt = createdAt
        this.updatedAt = updatedAt
        resetChangedOnNotNull()
    }
}
