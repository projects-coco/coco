COCO
====

COCO is designed to facilitate the use of Spring WebFlux, WebClient, Redis, Flyway, TestContainer and jOOQ.

Examples (Kotlin Code)
========

project start
----------------------

You can use COCO by @CocoWebApplication

```kotlin
@CocoWebApplication
class Application
```

@CocoWebApplication imports configuration class for web application (such as JsonConfiguration, WebConfig,
FlywayConfiguration ...) and also enables the use of SpringBoot.

```kotlin
@SpringBootApplication
@EnableCocoApplication
@Import(
    JsonConfiguration::class,
    WebConfig::class,
    FlywayConfiguration::class,
    WebClientConfiguration::class,
    ApiUtilsFactory::class,
    EventNotificationBusInjector::class,
    DslContextInjector::class,
    ReactiveRequestContextInjector::class,
)
annotation class CocoWebApplication()
```

Webflux handler
----------------------
You can handle Effect (which is data structure of [Arrow(FP Library)](https://arrow-kt.io/)) by using 'handle'

```kotlin
suspend fun <A> handle(
    successCode: HttpStatus = HttpStatus.OK,
    handler: Effect<ApiError, A>,
): ResponseEntity<A> {
    return handler.fold({ throw ApiError.ApiException(it) }, { result = ResponseEntity.status(successCode).body(it) })
}
```

Handler example

```kotlin
@PostMapping("/login")
suspend fun login(
    @RequestBody request: LoginRequest,
) = handle {
    service
        .login(
            LoginCommand(
                username = request.username,
                password = request.password,
            ),
        ).bindOrRaise {
            when (it) {
                LoginError.NotFoundUser -> raiseNotFound("사용자가 존재하지 않습니다")
                LoginError.PasswordMismatch -> raiseBadRequest("비밀번호가 일치하지 않습니다")
            }
        }
```

[Flyway](https://www.red-gate.com/products/flyway)
----------------------
You can use Flyway simply by **only adding** YAML configuration.

```yaml
spring:
  flyway:
    location: { your sql file location }
```

[Test Container](https://testcontainers.com/)
----------------------
You can use TestContainer simply by **only adding** YAML configuration.

```yaml
test:
  db:
    username: { test-user }
    password: { test-password }
    schema: { test-schema }
    engine: mariadb
    endpoint: localhost
    # classpath 하위에 만들 것
    flyway-sql-location: db/migration
    flyway-schema-history: { flyway_schema_history }
```

[Redis](https://redis.io/)
----------------------

You can use redis by @Import(RedisConfig::class)

```kotlin
@CocoWebApplication
@Import(RedisConfig::class)
class Application
```

```yaml
app:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
```

You have to do is implements SimpleRedisRepositoryBase

You can customize 'READ' method (e.g retrieveByKey)

```kotlin
class RedisRepositoryImpl(private val redissonReactiveClient: RedissonReactiveClient) :
    SimpleRedisRepositoryBase<Entity>() {
    override fun generateKey(entity: Entity): String {}

    override fun delete(entity: Entity): Effect<Nothing, LocalDateTime> {}

    override fun save(entity: Entity): Effect<Nothing, Entity> {}

    fun retrieveByKey(key: Key): Effect<NotFound, Entity> {}
}
```

[Spring WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
----------------------

You can use Spring WebClient by ApiUtils

```kotlin
ApiUtils()
    .post(API_ENDPOINT)
    .headers(mapOf("Authorization" to authToken))
    .body(mapOf("grant_type" to "account_credentials"))
    .contentType(MediaType.FormUrlEncode)
    .call(Response::class)
```

[jOOQ](https://www.jooq.org/)
----------------------

You can use jOOQ by implementing SimpleCrudRepositoryBase.

Implementing SimpleCrudRepositoryBase allows you to use CRUD methods.

```kotlin
abstract class SimpleCrudRepositoryBase<Id : EntityId<*>, Entity : EntityBase<Id>, R : Record>(
    protected val table: Table<R>,
    protected val toJooq: Entity.() -> R,
    protected val toDomain: R.() -> Entity,
    private val keyColumn: String = "id",
) : JooqRepositoryBase(), Repository<Id, Entity>

---

UserRepository : SimpleCrudRepositoryBase<User.Id, User, UserRecord>

---

UserRepository().save(user)
UserRepository().retrieve(user.id)
UserRepository().delete(user)
```

You can use 'dynamic query' by implementing SearchRepositoryBase

```kotlin
abstract class SearchRepositoryBase<Id : EntityId<*>, Entity : EntityBase<Id>, R : Record, S : SearchDtoBase>(
    table: Table<R>,
    toJooq: Entity.() -> R,
    toDomain: R.() -> Entity,
    val selectConditionBuilder: suspend S.() -> Condition,
) : SearchRepository<Entity, S>, SimpleCrudRepositoryBase<Id, Entity, R>(table, toJooq, toDomain)

---

UserRepository : SearchRepositoryBase<User.Id, User, UserRecord, UserSearchDto>

---

UserRepository().search(
    UserSearchDto(
        username = "username",
        name = "name",
        email = "email",
    )
)
```

Event Notification Bus
----------------------

You can use EventNotificationBus, which enables the transfer of event between **different modules**.

```kotlin
class A {
    fun emitEvent() {
        EventNotificationBus().emitNotification(MyCustomEvent())
    }
}

---

class B {
    fun handleEvent() {
        EventNotificationBus()
            .notifications()
            .handleEvent<MyCustomEvent> { event -> customLogic(event) }
            .subscribe()
    }
}

```

Pagination
----------------------

You can use pagination by only using 'PageRequest' in your Controller

```kotlin
    @GetMapping()
suspend fun page(
    @RequestBody request: Request,
    page: PageRequest,
) {
    ...
}

```