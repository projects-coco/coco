package com.coco.domain.utils

import java.security.SecureRandom
import kotlin.random.asKotlinRandom

object StringUtils {
    private const val BASE_32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
    private val random = SecureRandom().asKotlinRandom()

    fun newBase32Random(len: Int = 10): String = (1..len).map { BASE_32.random(random) }.joinToString("")
}

fun String.camelToSnake(): String {
    val pattern = "(?<=.)[A-Z]".toRegex()
    return this.replace(pattern, "_$0").lowercase()
}

fun String.snakeToCamel(): String {
    val pattern = "_[a-z]".toRegex()
    return replace(pattern) { it.value.last().uppercase() }
}
