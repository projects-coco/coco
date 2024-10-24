package com.coco.domain.core

class DumbAtomicity : Atomicity {
    override suspend fun <T> invoke(f: suspend () -> T): T {
        return f()
    }
}

class DumbAtomicityProvider : AtomicityProvider {
    override fun provide(): Atomicity = DumbAtomicity()
}
