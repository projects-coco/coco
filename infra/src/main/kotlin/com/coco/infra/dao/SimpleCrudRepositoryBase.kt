package com.coco.infra.dao

import arrow.core.raise.Effect
import arrow.core.raise.effect
import com.coco.domain.core.Page
import com.coco.domain.core.PageRequest
import com.coco.domain.core.awaitList
import com.coco.domain.core.bindOrNothing
import com.coco.domain.model.EntityBase
import com.coco.domain.model.EntityId
import com.coco.domain.model.ReadOnlyRepository.NotFound
import com.coco.domain.model.Repository
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.*
import org.jooq.impl.DSL
import java.time.LocalDateTime

fun Table<*>.createdAt() = field("created_at", LocalDateTime::class.java)!!

fun Table<*>.updatedAt() = field("updated_at", LocalDateTime::class.java)!!

fun Table<*>.deletedAt() = DSL.field("deleted_at", LocalDateTime::class.java)

fun Table<*>.isNotDeleted() = deletedAt().isNull

fun <R : Record> SelectConnectByStep<R>.paging(pageRequest: PageRequest) = limit(pageRequest.limit).offset(pageRequest.offset)

suspend fun <R : Record> DSLContext.upsertWithReturningRecord(
    table: Table<R>,
    record: R,
): R =
    insertInto(table)
        .set(record)
        .onDuplicateKeyUpdate()
        .set(record)
        .set(table.updatedAt(), DSL.currentLocalDateTime())
        .returningResult(table)
        .awaitFirst()
        .value1()

suspend fun DSLContext.awaitExists(
    table: Table<*>,
    condition: Condition,
): Boolean =
    selectOne()
        .from(table)
        .where(condition.and(table.isNotDeleted()))
        .awaitFirstOrNull()
        ?.value1() != null

abstract class SimpleCrudRepositoryBase<Id : EntityId<*>, Entity : EntityBase<Id>, R : Record>(
    protected val table: Table<R>,
    protected val toJooq: Entity.() -> R,
    protected val toDomain: R.() -> Entity,
    private val keyColumn: String = "id",
) : JooqRepositoryBase(),
    Repository<Id, Entity> {
    protected fun Table<*>.id() = field(keyColumn, ByteArray::class.java)!!

    private fun Table<*>.filterById(id: EntityId<*>): Condition = id().eq(id.toBytes()).and(isNotDeleted())

    override fun retrieveFirst(): Effect<NotFound, Entity> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .awaitFirstOrNull()
                ?.toDomain()
                ?: raise(NotFound)
        }

    override fun retrieve(id: Id): Effect<NotFound, Entity> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .where(table.filterById(id))
                .awaitFirstOrNull()
                ?.toDomain()
                ?: raise(NotFound)
        }

    override fun retrieveForUpdate(id: Id): Effect<NotFound, Entity> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .where(table.filterById(id))
                .forUpdate()
                .wait(5)
                .awaitFirstOrNull()
                ?.toDomain()
                ?: raise(NotFound)
        }

    override fun retrieveAll(ids: List<Id>): Effect<Nothing, List<Entity>> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .where(table.id().`in`(ids.map { it.toBytes() }))
                .awaitList()
                .map { it.toDomain() }
                .sortedBy { it.id.toString() }
        }

    override fun retrieveAll(): Effect<Nothing, List<Entity>> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .awaitList()
                .map { toDomain(it) }
                .sortedBy { it.id.toString() }
        }

    override fun retrieveAll(pageRequest: PageRequest): Effect<Nothing, Page<Entity>> =
        effect {
            val content =
                currentDslContext()
                    .selectFrom(table)
                    .apply {
                        orderBy(orderByBuilder(pageRequest.sort, table))
                        paging(pageRequest)
                    }.awaitList()
                    .map { toDomain(it) }
            val totalElements = countAll(table)
            val totalPages = calculatePages(totalElements, pageRequest)
            Page(
                content = content,
                totalElements = totalElements,
                totalPages = totalPages,
                pageRequest = pageRequest,
            )
        }

    override fun existsById(id: Id): Effect<Nothing, Boolean> =
        effect {
            currentDslContext()
                .awaitExists(table, table.filterById(id))
        }

    override fun save(
        entity: Entity,
        skipLock: Boolean,
    ): Effect<Nothing, Entity> =
        effect {
            if (!skipLock) {
                val previous: Entity? = retrieve(entity.id).bindOrNothing()
                if (previous != null && entity.updatedAt != previous.updatedAt) {
                    throw RuntimeException(
                        "Concurrency error: " +
                            "The data you were attempting to edit has been modified by another operation. " +
                            "Please refresh and retry.",
                    )
                }
            }
            currentDslContext()
                .upsertWithReturningRecord(table, entity.toJooq())
                .toDomain()
        }

    override fun delete(entity: Entity): Effect<Nothing, LocalDateTime> =
        effect {
            currentDslContext()
                .update(table)
                .set(table.deletedAt(), DSL.currentLocalDateTime())
                .where(table.filterById(entity.id))
                .returningResult(table.deletedAt())
                .awaitFirst()
                .value1()
        }

    override fun unsafeHardDelete(entity: Entity): Effect<Nothing, Unit> =
        effect {
            currentDslContext()
                .delete(table)
                .where(table.filterById(entity.id))
                .awaitSingle()
        }

    protected fun orderByBuilder(
        sort: PageRequest.Sort,
        table: Table<*>,
    ): SortField<*> {
        val fields = table.javaClass.declaredFields
        val filteredFields =
            fields
                .filter { it.type == TableField::class.java }
                .map {
                    it.trySetAccessible()
                    it.get(table) as TableField<*, *>
                }
        return generateSortFields(sort, filteredFields) ?: table.id().desc()
    }
}
