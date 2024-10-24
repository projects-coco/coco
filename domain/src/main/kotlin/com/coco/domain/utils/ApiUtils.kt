package com.coco.domain.utils

import arrow.core.raise.Effect
import java.nio.charset.Charset
import kotlin.reflect.KClass

class ApiUtils(
    private val client: Client,
) {
    fun get(uri: String) = ApiRequestSpec(client, uri, HttpMethod.GET)

    fun post(uri: String) = ApiRequestSpec(client, uri, HttpMethod.POST)

    fun put(uri: String) = ApiRequestSpec(client, uri, HttpMethod.PUT)

    fun delete(uri: String) = ApiRequestSpec(client, uri, HttpMethod.DELETE)
}

class ApiRequestSpec(
    private val client: Client,
    var uri: String,
    var httpMethod: HttpMethod,
) {
    var headers: Map<String, String>? = null
        private set
    var body: Any? = null
        private set
    var acceptValue: MediaType = MediaType.ApplicationJson
        private set
    var contentTypeValue: MediaType = MediaType.ApplicationJson
        private set
    var charset: Charset = Charsets.UTF_8
        private set

    fun headers(headers: Map<String, String>) = apply { this.headers = headers }

    fun body(body: Any) = apply { this.body = body }

    fun accept(acceptValue: MediaType) = apply { this.acceptValue = acceptValue }

    fun contentType(contentType: MediaType) = apply { this.contentTypeValue = contentType }

    fun encoding(charset: Charset) = apply { this.charset = charset }

    suspend fun <T : Any> call(clazz: KClass<T>): Effect<Client.ApiHelperError, T> = client.call(clazz, this)

    suspend fun <T : Any> callList(clazz: KClass<T>): Effect<Client.ApiHelperError, List<T>> = client.callList(clazz, this)
}

interface Client {
    sealed class ApiHelperError {
        data object ExternalServer4XXFail : ApiHelperError()

        data object ExternalServer5XXFail : ApiHelperError()

        data object UnknownError : ApiHelperError()

        data object WebClientRequestException : ApiHelperError()

        data object RetrieveError : ApiHelperError()
    }

    suspend fun <T : Any> call(
        clazz: KClass<T>,
        apiRequestSpec: ApiRequestSpec,
    ): Effect<ApiHelperError, T>

    suspend fun <T : Any> callList(
        clazz: KClass<T>,
        apiRequestSpec: ApiRequestSpec,
    ): Effect<ApiHelperError, List<T>>
}

sealed class HttpMethod {
    data object GET : HttpMethod()

    data object POST : HttpMethod()

    data object PUT : HttpMethod()

    data object DELETE : HttpMethod()
}

sealed class MediaType {
    data object ApplicationJson : MediaType()

    data object TextPlain : MediaType()

    data object MultipartFormData : MediaType()

    data object FormUrlEncode : MediaType()
}
