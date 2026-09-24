package no.pipehill.reiseapp.service.support

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <T> concurrently(first: () -> T, second: () -> T): List<T> {
    val ready = CountDownLatch(2)
    val start = CountDownLatch(1)
    val executor = Executors.newFixedThreadPool(2)
    try {
        val futures = listOf(first, second).map { action ->
            executor.submit<T> {
                ready.countDown()
                check(start.await(10, TimeUnit.SECONDS)) { "Concurrent test did not start in time" }
                action()
            }
        }
        check(ready.await(10, TimeUnit.SECONDS)) { "Both test workers must be ready" }
        start.countDown()
        return futures.map { it.get(15, TimeUnit.SECONDS) }
    } finally {
        start.countDown()
        executor.shutdownNow()
        executor.awaitTermination(10, TimeUnit.SECONDS)
    }
}
