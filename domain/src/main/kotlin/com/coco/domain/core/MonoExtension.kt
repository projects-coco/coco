package com.coco.domain.core

import arrow.core.Either
import kotlinx.coroutines.reactor.awaitSingle
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

suspend fun <T, V> Mono<T>.then(f: () -> V): V =
    this.then(
        Mono.fromCallable {
            f()
        },
    ).awaitSingle()

suspend fun <T, V> Mono<T>.awaitMap(f: (T) -> V): V {
    val value = this.awaitSingle()
    return f(value)
}

fun <A, B, C> Mono<Either<A, B>>.fold(
    onLeft: (A) -> Mono<C>,
    onRight: (B) -> Mono<C>,
): Mono<C> =
    this.flatMap {
        it.fold(onLeft, onRight)
    }

suspend fun <T> Flux<T>.awaitList(): List<T> = this.collectList().awaitSingle()
