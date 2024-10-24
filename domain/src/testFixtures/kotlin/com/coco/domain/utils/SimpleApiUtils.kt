package com.coco.domain.utils

import arrow.core.raise.Effect
import kotlin.reflect.KClass

data object SimpleApiClient : Client {
    override suspend fun <T : Any> call(
        clazz: KClass<T>,
        apiRequestSpec: ApiRequestSpec,
    ): Effect<Client.ApiHelperError, T> {
        TODO("Not yet implemented")
    }

    override suspend fun <T : Any> callList(
        clazz: KClass<T>,
        apiRequestSpec: ApiRequestSpec,
    ): Effect<Client.ApiHelperError, List<T>> {
        TODO("Not yet implemented")
    }
}

fun simpleApiUtils(): ApiUtils = ApiUtils(SimpleApiClient)
