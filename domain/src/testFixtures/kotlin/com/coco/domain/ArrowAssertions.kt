package com.coco.domain

import arrow.core.identity
import arrow.core.raise.EagerEffect
import arrow.core.raise.Effect
import arrow.core.raise.fold
import arrow.core.raise.getOrElse
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe

fun <R, A> EagerEffect<R, A>.shouldSucceed(): A = getOrElse { fail("Expected success to execute Effect, but failed with $it.") }

fun <R, A> EagerEffect<R, A>.shouldFail(): R = fold(::identity) { fail("Expected fail to execute Effect, but success with $it") }

suspend inline fun <R, A> Effect<R, A>.shouldSucceed(
    message: (R) -> String = { "Expected fail to execute Effect, but success with $it" },
): A = getOrElse { fail(message(it)) }

suspend inline fun <R, A> Effect<R, A>.shouldFail(): R = fold(::identity) { fail("Expected fail to execute Effect, but success with $it") }

suspend inline infix fun <R, A> Effect<R, A>.shouldSucceedWith(result: A): A = shouldSucceed().also { it shouldBe result }

suspend inline infix fun <R, A> Effect<R, A>.shouldFailWith(error: R): R = shouldFail().also { it shouldBe error }
