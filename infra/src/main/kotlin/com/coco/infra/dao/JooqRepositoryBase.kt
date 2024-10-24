package com.coco.infra.dao

import com.coco.domain.core.PageRequest
import com.coco.domain.core.awaitList
import com.coco.domain.utils.camelToSnake
import kotlinx.coroutines.reactive.awaitFirst
import org.jooq.*
import org.jooq.impl.DSL
import reactor.core.publisher.Flux

suspend fun <R : Record?> Select<R>.awaitList(): List<R> = Flux.from(this).awaitList()

fun <R : TableRecord<R>> TableRecord<R>.isNotEmpty(): Boolean {
    try {
        field(0)?.get(this)?.let {
            return true
        } ?: return false
    } catch (_: NullPointerException) {
        return false
    }
}

abstract class JooqRepositoryBase {
    protected suspend fun countAll(
        query: SelectOnConditionStep<*>,
        condition: Condition? = null,
    ) = currentDslContext()
        .select(DSL.count())
        .from(query.where(condition))
        .awaitFirst()
        ?.value1()?.toLong() ?: 0L

    protected suspend fun countAll(
        table: Table<*>? = null,
        condition: Condition? = null,
        joinTable: Table<*>? = null,
        joinType: JoinType = JoinType.LEFT_OUTER_JOIN,
        joinCondition: Condition? = null,
    ) = currentDslContext()
        .select(DSL.count())
        .from(table)
        .apply {
            joinTable?.let {
                join(joinTable, joinType).on(joinCondition)
            }
            condition?.run {
                where(condition)
            }
        }
        .awaitFirst()
        ?.value1()?.toLong() ?: 0L

    protected fun calculatePages(
        totalCount: Long,
        pageRequest: PageRequest,
    ): Long {
        val pageSize = pageRequest.limit.toLong()
        val totalPages = totalCount / pageSize + if (totalCount % pageSize == 0L) 0 else 1
        return totalPages
    }

    protected fun generateSortFields(
        sort: PageRequest.Sort,
        fields: Collection<TableField<*, *>>,
    ): SortField<*>? =
        sort.let {
            val targetField =
                fields.find { field ->
                    field.name.contains(it.field.camelToSnake().lowercase())
                }
            targetField?.let { field ->
                when (it.direction) {
                    PageRequest.Sort.Direction.ASC -> field.asc()
                    PageRequest.Sort.Direction.DESC -> field.desc()
                }
            }
        }

    protected fun generateSortFields(
        sort: PageRequest.Sort,
        table: Table<*>,
    ): SortField<*>? {
        val fields = table.javaClass.declaredFields
        val filteredFields =
            fields.filter { it.type == TableField::class.java }
                .map {
                    it.trySetAccessible()
                    it.get(table) as TableField<*, *>
                }
        return generateSortFields(sort, filteredFields)
    }
}
