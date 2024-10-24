package com.coco.domain

import arrow.core.raise.Effect
import arrow.core.raise.effect
import com.coco.domain.model.EntityBase
import com.coco.domain.model.EntityId
import com.coco.domain.model.ReadOnlyRepository.NotFound
import com.coco.domain.model.Repository
import io.mockk.Call
import io.mockk.MockKStubScope
import io.mockk.coEvery
import io.mockk.slot
import java.time.LocalDateTime

infix fun <E, A> MockKStubScope<Effect<E, A>, *>.succeeds(value: A) = returns(effect { value })

infix fun <E, A> MockKStubScope<Effect<E, A>, *>.fails(error: E) = returns(effect { raise(error) })

infix fun <E, A, M : MockKStubScope<Effect<E, A>, *>> M.succeedAnswers(answer: M.(Call) -> A) =
    answers {
        effect { answer(it) }
    }

infix fun <E, A, M : MockKStubScope<Effect<E, A>, *>> M.failAnswers(answer: M.(Call) -> E) =
    answers {
        effect { raise(answer(it)) }
    }

inline fun <reified ID : EntityId<*>, E : EntityBase<ID>, R : Repository<ID, E>> R.mockRetrieveSucceeds(entity: E) =
    also { coEvery { retrieve(any()) } succeeds entity }

inline fun <reified ID : EntityId<*>, E : EntityBase<ID>, R : Repository<ID, E>> R.mockRetrieveNotFound() =
    also { coEvery { retrieve(any()) } fails NotFound }

inline fun <reified ID : EntityId<*>, E : EntityBase<ID>, R : Repository<ID, E>> R.mockExistsById(result: Boolean = true) =
    also { coEvery { existsById(any()) } succeeds result }

inline fun <reified ID : EntityId<*>, reified E : EntityBase<ID>, R : Repository<ID, E>> R.mockSave() =
    also {
        val slot = slot<E>()
        coEvery { save(capture(slot)) } succeedAnswers { slot.captured }
    }

inline fun <reified ID : EntityId<*>, reified E : EntityBase<ID>, R : Repository<ID, E>> R.mockDelete(
    result: LocalDateTime = LocalDateTime.now(),
) = also { coEvery { delete(any()) } succeeds result }
