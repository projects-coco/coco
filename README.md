COCO
====

COCO is designed to facilitate the use of Spring WebFlux, Batch, WebClient, Redis, Flyway, TestContainer and jOOQ.

Additional modules for other frameworks(e.g., Spring MVC, JPA, Prometheus) are planned for future release.

Examples (Kotlin Code)
========

project start
----------------------

you can use COCO by @CocoWebApplication

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
you can handle Effect (which is data structure of [Arrow(FP Library)](https://arrow-kt.io/)) by using 'handle'

```kotlin
suspend fun <A> handle(
    successCode: HttpStatus = HttpStatus.OK,
    handler: Effect<ApiError, A>,
): ResponseEntity<A> {
    return handler.fold({ throw ApiError.ApiException(it) }, { result = ResponseEntity.status(successCode).body(it) })
}
```

handler example

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

you can use redis by @Import(RedisConfig::class)

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

you have to do is implements SimpleRedisRepositoryBase

you can customize 'READ' method (e.g retrieveByKey)

```kotlin
class RedisRepositoryImpl(private val redissonReactiveClient: RedissonReactiveClient) :
    SimpleRedisRepositoryBase<Entity>() {
    override fun generateKey(entity: Entity): String {}

    override fun delete(entity: Entity): Effect<Nothing, LocalDateTime> {}

    override fun save(entity: Entity): Effect<Nothing, Entity> {}

    fun retrieveByKey(key: Key): Effect<NotFound, Entity> {}
}
```

[Spring Batch](https://spring.io/projects/spring-batch)
----------------------

you can use SpringBatch by @CocoBatchApplication

```kotlin
@CocoBatchApplication
class Application
```

With COCO, you have to do is only writing batch logic.  
COCO provides simple function for batch tasks such as 'intervalNMinutesSchedule(N)'(running batches every N minute) so
that you can use batch easily.

```kotlin
@Configuration
class BatchExample {
    @Component
    class Job(
        private val cocoCoroutineScopeProvider: CocoCoroutineScopeProvider,
        private val batchService: BatchService,
    ) : org.quartz.Job {
        override fun execute(jobExecutionContext: JobExecutionContext?) {
            cocoCoroutineScopeProvider.provide().launch {
                batchService.batch().bindOrNothing()
            }
        }
    }

    @Bean
    fun batchJobDetail(): JobDetail =
        JobBuilder
            .newJob(Job::class.java)
            .withIdentity(Job::class.qualifiedName)
            .storeDurably()
            .build()

    @Bean
    fun batchJobTrigger(): Trigger =
        TriggerBuilder
            .newTrigger()
            .forJob(Job::class.qualifiedName)
            .startNow()
            .withSchedule(intervalNMinutesSchedule(1))
            .build()
}
```
