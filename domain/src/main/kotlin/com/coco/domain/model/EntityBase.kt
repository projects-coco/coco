package com.coco.domain.model

import arrow.core.Either
import arrow.core.right
import com.coco.domain.core.Reason
import com.coco.domain.core.ValidType
import ulid.ULID
import java.nio.ByteBuffer
import java.time.LocalDateTime

interface EntityId<T> {
    val value: T

    fun toBytes(): ByteArray
}

@JvmInline
value class BinaryId private constructor(
    override val value: ULID,
) : EntityId<ULID>,
    Comparable<BinaryId> {
    companion object : ValidType<String, BinaryId>({ BinaryId(ULID.parseULID(it)) }) {
        override fun validate(value: String): Either<Reason, BinaryId> =
            when {
                else -> BinaryId(ULID.parseULID(value)).right()
            }

        fun unsafeMake(payload: ByteArray): BinaryId = BinaryId(ULID.fromBytes(payload))

        fun unsafeMake(payload: ULID): BinaryId = BinaryId(payload)

        fun new(): BinaryId = BinaryId(ULID.nextULID())
    }

    override fun toBytes(): ByteArray = value.toBytes()

    override fun toString(): String = value.toString()

    fun toHexString(): String = "0x${this.toBytes().joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }}"

    override fun compareTo(other: BinaryId): Int = compareValuesBy(this, other, BinaryId::value)
}

inline fun <T : EntityBase<BinaryId>> List<T>.findByBinarySearch(
    key: BinaryId,
    crossinline selector: (T) -> BinaryId,
): T {
    val index = this.binarySearchBy(key) { selector(it) }
    return this[index]
}

@JvmInline
value class LongId private constructor(
    override val value: Long,
) : EntityId<Long> {
    companion object : ValidType<Long, LongId>(::LongId) {
        override fun validate(value: Long): Either<Reason, LongId> =
            when {
                else -> LongId(value).right()
            }

        fun new(): LongId = LongId(System.currentTimeMillis())
    }

    override fun toBytes(): ByteArray {
        val buffer: ByteBuffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(value)
        return buffer.array()
    }

    override fun toString(): String = value.toString()
}

abstract class EntityBase<T : EntityId<*>>(
    id: T,
    createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) {
    var id: T = id
        private set
    var createdAt: LocalDateTime = createdAt
        protected set
    var updatedAt: LocalDateTime = updatedAt
        protected set

    override fun equals(other: Any?): Boolean =
        when (other) {
            is EntityBase<*> -> other.id == this.id
            else -> false
        }

    override fun hashCode(): Int = id.hashCode()
}
