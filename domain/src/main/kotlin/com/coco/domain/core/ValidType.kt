package com.coco.domain.core

import arrow.core.Either

typealias Reason = String

// TODO : refactor either to Option<Reason>
abstract class ValidType<V, A>(val unsafeMake: (V) -> (A)) {
    abstract fun validate(value: V): Either<Reason, A>
}
