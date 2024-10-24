package com.coco.domain.core

import arrow.core.Option
import arrow.core.identity
import arrow.core.raise.Effect
import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.fold

@RaiseDSL
inline fun <Error, B> Raise<Error>.ensureSome(
    option: Option<B>,
    raise: () -> Error,
): B {
    return option.fold({ raise(raise()) }, ::identity)
}

// Error, A, B
// recover: Error -> B = E1 -> A
// transform: A->B = E2 -> A
context(Raise<E2>)
suspend fun <E1, E2, A> Effect<E1, A>.bindOrRaise(transform: (E1) -> E2) = fold({ raise(transform(it)) }, ::identity)

fun <T> Nothing?.toOption(): Option<T> = Option.fromNullable(null)

fun <A, R> Option<A>.bindOrNull(ifSome: (A) -> R) = this.fold({ null }, ifSome)

suspend fun <E1, A> Effect<E1, A>.bindOrNothing() = fold({ null }, ::identity)
