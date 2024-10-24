package com.coco.domain.core

import arrow.core.raise.Effect

interface Atomicity {
    suspend operator fun <T> invoke(f: suspend () -> T): T

    // TODO effect가 실패했을 때 rollback을 할지 말지 결정이 필요함.
    fun <A, R> effect(f: Effect<A, R>): Effect<A, R> = arrow.core.raise.effect { invoke { f() } }
}

interface AtomicityProvider {
    fun provide(): Atomicity
}
