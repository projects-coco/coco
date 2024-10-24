package com.coco.infra.dao

import arrow.core.raise.Effect
import arrow.core.raise.effect
import com.coco.domain.core.Page
import com.coco.domain.core.PageRequest
import com.coco.domain.model.EntityBase
import com.coco.domain.model.EntityId
import com.coco.domain.model.SearchDtoBase
import com.coco.domain.model.SearchRepository
import org.jooq.Condition
import org.jooq.Record
import org.jooq.Table

abstract class SearchRepositoryBase<Id : EntityId<*>, Entity : EntityBase<Id>, R : Record, S : SearchDtoBase>(
    table: Table<R>,
    toJooq: Entity.() -> R,
    toDomain: R.() -> Entity,
    val selectConditionBuilder: suspend S.() -> Condition,
) : SearchRepository<Entity, S>,
    SimpleCrudRepositoryBase<Id, Entity, R>(table, toJooq, toDomain) {
    override fun search(searchDto: S): Effect<Nothing, List<Entity>> =
        effect {
            currentDslContext()
                .selectFrom(table)
                .apply {
                    searchDto.run {
                        where(searchDto.selectConditionBuilder())
                    }
                }
                .awaitList()
                .map { toDomain(it) }
        }

    override fun search(
        searchDto: S,
        pageRequest: PageRequest,
    ): Effect<Nothing, Page<Entity>> =
        effect {
            val content =
                currentDslContext()
                    .selectFrom(table)
                    .apply {
                        searchDto.run {
                            where(searchDto.selectConditionBuilder())
                        }
                        orderBy(orderByBuilder(pageRequest.sort, table))
                        paging(pageRequest)
                    }
                    .awaitList()
                    .map { toDomain(it) }
            val totalElements = countAll(table, searchDto.selectConditionBuilder())
            val totalPages = calculatePages(totalElements, pageRequest)
            Page(
                content = content,
                totalPages = totalPages,
                totalElements = totalElements,
                pageRequest = pageRequest,
            )
        }
}
