package com.coco.infra.webclient

import arrow.core.raise.Effect
import arrow.core.raise.effect
import com.coco.domain.core.awaitList
import com.coco.domain.utils.*
import com.coco.domain.utils.Client.ApiHelperError
import io.netty.channel.ChannelOption
import io.netty.resolver.DefaultAddressResolverGroup
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpMethod
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import reactor.util.retry.Retry
import java.time.Duration
import kotlin.reflect.KClass
import com.coco.domain.utils.HttpMethod as CustomHttpMethod

class ApiUtilsFactory {
    private val logger = logger()
    private val uriBuilderFactory: DefaultUriBuilderFactory = DefaultUriBuilderFactory()
    private val httpClient: HttpClient =
        HttpClient
            .create(connectionProvider())
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
            .resolver(DefaultAddressResolverGroup.INSTANCE)

    fun create(): ApiUtils = ApiUtils(apiClientHelper(webClient()))

    private fun webClient(): WebClient {
        uriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY
        return WebClient
            .builder()
            .uriBuilderFactory(uriBuilderFactory)
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)
            }.clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }

    private fun apiClientHelper(webClient: WebClient): Client =
        object : Client {
            override suspend fun <T : Any> call(
                clazz: KClass<T>,
                apiRequestSpec: ApiRequestSpec,
            ): Effect<ApiHelperError, T> =
                effect {
                    retrieve(webClient, apiRequestSpec)
                        .bind()
                        .bodyToMono(clazz.java)
                        // 4XX, 5XX이면 retry
                        .retryWhen(
                            Retry
                                .backoff(5, Duration.ofMillis(100))
                                .onRetryExhaustedThrow { _, retrySignal ->
                                    retrySignal.failure()
                                },
                        ).doOnError(WebClientResponseException::class.java) {
                            errorLog(it.javaClass.name, apiRequestSpec, it.statusCode.value(), it.responseBodyAsString)
                            if (it.statusCode.is4xxClientError) {
                                raise(ApiHelperError.ExternalServer4XXFail)
                            } else if (it.statusCode.is5xxServerError) {
                                raise(ApiHelperError.ExternalServer5XXFail)
                            } else {
                                raise(ApiHelperError.UnknownError)
                            }
                        }
                        // 외부 서버가 죽어있으면 여기로 떨어짐
                        .doOnError(WebClientRequestException::class.java) {
                            errorLog(it.javaClass.name, apiRequestSpec)
                            raise(ApiHelperError.WebClientRequestException)
                        }.awaitSingle()
                }

            // TODO : 나중에 외부 API에서 List를 받아와야 할 때 테스트 해봐야 할수도?
            override suspend fun <T : Any> callList(
                clazz: KClass<T>,
                apiRequestSpec: ApiRequestSpec,
            ): Effect<ApiHelperError, List<T>> =
                effect {
                    retrieve(webClient, apiRequestSpec)
                        .bind()
                        .bodyToFlux(clazz.java)
                        .awaitList()
                }
        }

    private fun connectionProvider(): ConnectionProvider =
        ConnectionProvider
            .builder("http-pool")
            .maxConnections(100)
            .pendingAcquireTimeout(Duration.ofMillis(0))
            .pendingAcquireMaxCount(-1)
            .maxIdleTime(Duration.ofMillis(1000L))
            .build()

    private fun retrieve(
        webClient: WebClient,
        apiRequestSpec: ApiRequestSpec,
    ): Effect<ApiHelperError, ResponseSpec> =
        effect {
            webClient
                .method(toWebClientHttpMethod(apiRequestSpec.httpMethod))
                .uri(apiRequestSpec.uri)
                .accept(toWebClientMediaType(apiRequestSpec.acceptValue))
                .acceptCharset(apiRequestSpec.charset)
                .contentType(toWebClientMediaType(apiRequestSpec.contentTypeValue))
                .apply {
                    apiRequestSpec.body?.let {
                        when (apiRequestSpec.contentTypeValue) {
                            MediaType.MultipartFormData -> {
                                if (it is Map<*, *>) {
                                    body(it.toMultiPartFormDataBody().bind())
                                } else {
                                    logger.error("MultiPartFormData body type must be Map")
                                    raise(ApiHelperError.RetrieveError)
                                }
                            }

                            MediaType.FormUrlEncode -> {
                                if (it is Map<*, *>) {
                                    body(it.toFormUrlBody().bind())
                                } else {
                                    logger.error("FormUrlEncode body type must be Map")
                                    raise(ApiHelperError.RetrieveError)
                                }
                            }

                            else -> bodyValue(it)
                        }
                    }
                    apiRequestSpec.headers?.let {
                        headers { headers ->
                            for (header in it) {
                                headers.set(header.key, header.value)
                            }
                        }
                    }
                }.retrieve()
        }

    private fun Map<*, *>.toFormUrlBody(): Effect<ApiHelperError, BodyInserters.FormInserter<String>> {
        val that = this
        return effect {
            val body: MultiValueMap<String, String> = LinkedMultiValueMap()
            that.filter { it.value != null }.forEach { (key, value) ->
                if (key is String && value is String) {
                    body.add(key, value)
                } else {
                    logger.error("UrlEncode-body key, value must be String. key : {}, body: {}", key, value)
                    raise(ApiHelperError.RetrieveError)
                }
            }

            BodyInserters.fromFormData(body)
        }
    }

    private fun Map<*, *>.toMultiPartFormDataBody(): Effect<ApiHelperError, BodyInserters.MultipartInserter> {
        val that = this
        return effect {
            val builder = MultipartBodyBuilder()
            that.filter { it.value != null }.forEach { (key, value) ->
                if (key is String) {
                    builder.part(key, value!!)
                } else {
                    logger.error("MultiPartFormData key type error : {}", key)
                    raise(ApiHelperError.RetrieveError)
                }
            }
            BodyInserters.fromMultipartData(builder.build())
        }
    }

    private fun toWebClientHttpMethod(httpMethod: CustomHttpMethod) =
        when (httpMethod) {
            CustomHttpMethod.GET -> HttpMethod.GET
            CustomHttpMethod.POST -> HttpMethod.POST
            CustomHttpMethod.PUT -> HttpMethod.PUT
            CustomHttpMethod.DELETE -> HttpMethod.DELETE
        }

    private fun toWebClientMediaType(mediaType: MediaType): org.springframework.http.MediaType =
        when (mediaType) {
            MediaType.ApplicationJson -> org.springframework.http.MediaType.APPLICATION_JSON
            MediaType.TextPlain -> org.springframework.http.MediaType.TEXT_PLAIN
            MediaType.MultipartFormData -> org.springframework.http.MediaType.MULTIPART_FORM_DATA
            MediaType.FormUrlEncode -> org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
        }

    fun errorLog(
        exceptionClassName: String,
        apiRequestSpec: ApiRequestSpec,
        responseHttpStatus: Int? = null,
        responseBody: String? = null,
    ) {
        logger.error(
            "# WebClientError | ExceptionName : {} | REMOTE_URI = {} | REQUEST_METHOD = {} | REQUEST_HEADERS = {} " +
                "| REQUEST_BODY = {} | RESPONSE_STATUS = {} | RESPONSE_BODY = {}",
            exceptionClassName,
            apiRequestSpec.uri,
            apiRequestSpec.httpMethod,
            apiRequestSpec.headers,
            apiRequestSpec.body,
            responseHttpStatus,
            responseBody,
        )
    }
}
