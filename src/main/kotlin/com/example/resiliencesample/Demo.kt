package com.example.resiliencesample

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.decorators.Decorators
import io.github.resilience4j.retry.Retry
import io.vavr.control.Try
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpServerErrorException
import java.util.function.Supplier


fun main() {

    val backendService = BackendService()

    // Create a CircuitBreaker with default configuration
    val circuitBreaker: CircuitBreaker = CircuitBreaker.ofDefaults("backendName")

    circuitBreaker.eventPublisher
        .onError { event -> println("CIRCUITBREAKER = ${event.circuitBreakerName}") }

//    val config = RetryConfig.custom<Any>()
//        .maxAttempts(3)
//        .waitDuration(Duration.ofMillis(1000))
////        .retryOnResult { response: Any -> response.getStatus() === 500 }
////        .retryOnException { e: Throwable? -> e is WebServiceException }
//        .retryExceptions(IOException::class.java, TimeoutException::class.java, HttpServerErrorException::class.java)
////        .ignoreExceptions(BunsinessException::class.java, OtherBunsinessException::class.java)
//        .build()

    // Create a Retry with default configuration
    // 3 retry attempts and a fixed time interval between retries of 500ms
    val retry: Retry = Retry.ofDefaults("backendName")
//    val retry: Retry = Retry.of("backendName", config)


    retry.eventPublisher
        .onRetry { event -> println("RETRY = ${event.name}") }

    val supplier = Supplier<Any> {
        backendService.doSomething()
    }

    // Decorate your call to backendService.doSomething()
    // with CircuitBreaker and Retry
    // **note: you will need the resilience4j-all dependency for this
    val decoratedSupplier = Decorators.ofSupplier(supplier)
        .withCircuitBreaker(circuitBreaker)
        .withRetry(retry)
        .decorate()

    // Execute the decorated supplier and recover from any exception
    Try.ofSupplier(decoratedSupplier)
        .recover { throwable ->
            println("Hello from Recovery")
            println(throwable.message)
        }
//
//    val result = circuitBreaker.executeSupplier<Any> { backendService.doSomething() }
//
//    println(result)
}

data class Response(
    val status: Int = 200,
    val body: String = "Hello"
)

class BackendService {
//    fun doSomething() = Response()

    fun doSomething(): Nothing =
        throw HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "This is a remote exception") as Throwable;
}