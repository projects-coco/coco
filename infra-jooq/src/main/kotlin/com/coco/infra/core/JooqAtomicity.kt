package com.coco.infra.core

import com.coco.domain.core.Atomicity
import com.coco.domain.core.AtomicityProvider
import com.coco.infra.dao.JooqCoroutineContext
import com.coco.infra.dao.currentDslContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.jooq.kotlin.coroutines.transactionCoroutine

class JooqAtomicity : Atomicity {
    override suspend operator fun <T> invoke(f: suspend () -> T): T {
        return currentDslContext()
            // mono context is not allow coroutine context with Job
            .transactionCoroutine(currentCoroutineContext().minusKey(Job)) {
                withContext(JooqCoroutineContext(it.dsl())) {
                    f()
                }
            }
    }
}

class JooqAtomicityProvider : AtomicityProvider {
    override fun provide(): Atomicity = JooqAtomicity()
}
