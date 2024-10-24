package com.coco.domain.model

import arrow.core.raise.Effect
import com.coco.domain.core.Page
import com.coco.domain.core.PageRequest
import java.time.LocalDateTime

interface ReadOnlyRepository<T> {
    object NotFound

    fun retrieveFirst(): Effect<NotFound, T>

    fun retrieveAll(): Effect<Nothing, List<T>>

    fun retrieveAll(pageRequest: PageRequest): Effect<Nothing, Page<T>>
}

interface Repository<Id : EntityId<*>, Entity : EntityBase<Id>> : ReadOnlyRepository<Entity> {
    fun retrieve(id: Id): Effect<ReadOnlyRepository.NotFound, Entity>

    fun retrieveForUpdate(id: Id): Effect<ReadOnlyRepository.NotFound, Entity>

    fun existsById(id: Id): Effect<Nothing, Boolean>

    fun save(
        entity: Entity,
        skipLock: Boolean = false,
    ): Effect<Nothing, Entity>

    fun delete(entity: Entity): Effect<Nothing, LocalDateTime>

    fun unsafeHardDelete(entity: Entity): Effect<Nothing, Unit>

    fun retrieveAll(ids: List<Id>): Effect<Nothing, List<Entity>>
}

interface SearchRepository<Entity, S : SearchDtoBase> : ReadOnlyRepository<Entity> {
    fun search(searchDto: S): Effect<Nothing, List<Entity>>

    fun search(
        searchDto: S,
        pageRequest: PageRequest,
    ): Effect<Nothing, Page<Entity>>
}
