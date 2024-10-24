package com.coco.infra.redis

import arrow.core.raise.Effect
import com.coco.domain.core.Page
import com.coco.domain.core.PageRequest
import com.coco.domain.model.EntityBase
import com.coco.domain.model.EntityId
import com.coco.domain.model.ReadOnlyRepository
import com.coco.domain.model.Repository
import org.redisson.api.RedissonReactiveClient
import org.redisson.client.codec.Codec
import org.redisson.client.codec.StringCodec
import java.time.Duration

abstract class SimpleRedisRepositoryBase<K : EntityId<*>, V : EntityBase<K>>(
    private val redissonReactiveClient: RedissonReactiveClient,
    private val codec: Codec = StringCodec(),
    private val ttl: Duration = Duration.ofMinutes(30),
) : Repository<K, V> {
    override fun retrieveFirst(): Effect<ReadOnlyRepository.NotFound, V> = throw NotImplementedError()

    override fun retrieve(id: K): Effect<ReadOnlyRepository.NotFound, V> = throw NotImplementedError()

    override fun retrieveForUpdate(id: K): Effect<ReadOnlyRepository.NotFound, V> = throw NotImplementedError()

    override fun existsById(id: K): Effect<Nothing, Boolean> = throw NotImplementedError()

    override fun unsafeHardDelete(entity: V): Effect<Nothing, Unit> = throw NotImplementedError()

    override fun retrieveAll(ids: List<K>): Effect<Nothing, List<V>> = throw NotImplementedError()

    override fun retrieveAll(): Effect<Nothing, List<V>> = throw NotImplementedError()

    override fun retrieveAll(pageRequest: PageRequest): Effect<Nothing, Page<V>> = throw NotImplementedError()

    protected abstract fun generateKey(entity: V): String
}
