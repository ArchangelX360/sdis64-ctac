package fr.sdis64.brain.utilities

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.slf4j.LoggerFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class) // for runTest and advanceTimeBy
class FetcherSchedulerTest {

    companion object {
        @BeforeAll
        @JvmStatic
        fun setup() {
            val logger = LoggerFactory.getLogger(FetcherScheduler::class.java) as Logger
            logger.level = Level.DEBUG
        }
    }

    @Test
    fun fetchesWithCorrectInitialDelayAndPeriod() = runTest {
        val mockFetcher = MockFetcher<String>()

        val initialDelayMillis = 2000L
        val periodMillis = 8000L

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
        ) { scheduler ->

            advanceTimeBy(initialDelayMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            mockFetcher.expectFetchAndSimulateResponse("first response")
            runCurrent()
            assertEquals("first response", scheduler.getValue(), "should return the fetched value")

            repeat(10) {
                advanceTimeBy(periodMillis)
                mockFetcher.assertNoPendingFetch() // shouldn't fetch again before the period elapses
                runCurrent()
                mockFetcher.expectFetchAndSimulateResponse("response $it")
                runCurrent()
                assertEquals("response $it", scheduler.getValue(), "should return the fetched value")
            }
        }
    }

    @Test
    fun timeoutAfterPeriod() = runTest(dispatchTimeoutMs = 5000) {
        val mockFetcher = MockFetcher<String>()

        val initialDelayMillis = 2000L
        val periodMillis = 8000L

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
        ) { scheduler ->

            advanceTimeBy(initialDelayMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            mockFetcher.expectFetch() // no response here, we simulate that fetch() hangs

            advanceTimeBy(periodMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't retry before the period
            runCurrent()
            // should retry immediately after the period (cancelling the previous fetch)
            mockFetcher.expectFetchAndSimulateResponse("response")
            runCurrent()
            assertEquals("response", scheduler.getValue(), "should return the fetched value")
        }
    }

    @Test
    fun getValue_shouldSuspendUntilTheFirstValueIsFetched() = runTest {
        val mockFetcher = MockFetcher<String>()

        val initialDelayMillis = 2000L
        val periodMillis = 8000L

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
        ) { scheduler ->

            val deferredGetValue = async(start = CoroutineStart.UNDISPATCHED) {
                scheduler.getValue()
            }

            assertTrue(deferredGetValue.isActive, "getValue should suspend at the beginning")
            advanceTimeBy(initialDelayMillis)
            assertTrue(deferredGetValue.isActive, "getValue should still be suspended until initialDelay is reached")
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            assertTrue(deferredGetValue.isActive, "getValue should still be suspended until the first fetch responds")
            mockFetcher.expectFetchAndSimulateResponse("first response")
            runCurrent()
            assertTrue(deferredGetValue.isCompleted, "getValue should resume once fetch has returned a result")
            assertEquals("first response", deferredGetValue.await(), "getValue should return the fetched value")
        }
    }

    @Test
    fun retries_regularFetcher() = runTest {
        val mockFetcher = MockFetcher<String>()

        val initialDelayMillis = 1000L
        val periodMillis = 5000L
        val maxRetries = 3

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
            maxRetries = maxRetries,
        ) { scheduler ->

            advanceTimeBy(initialDelayMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            mockFetcher.expectFetchAndSimulateResponse("response 1")
            runCurrent()
            assertEquals("response 1", scheduler.getValue(), "should return the fetched value")
            advanceTimeBy(periodMillis)
            runCurrent()
            // should retry immediately on error (without delay)
            repeat(maxRetries - 1) {
                mockFetcher.expectFetchAndSimulateError(RuntimeException("BOOM"))
                runCurrent()
                assertEquals("response 1", scheduler.getValue(), "should keep returning the latest valid value")
            }
            mockFetcher.expectFetchAndSimulateResponse("response 2")
            runCurrent()
            assertEquals("response 2", scheduler.getValue(), "should return the new value after a retry succeeded")

            advanceTimeBy(periodMillis)
            runCurrent()

            repeat(maxRetries) {
                mockFetcher.expectFetchAndSimulateError(RuntimeException("BOOM"))
                runCurrent()
                assertEquals("response 2", scheduler.getValue())
            }
            mockFetcher.assertNoPendingFetch() // should give up after max retries

            advanceTimeBy(periodMillis)
            runCurrent()
            mockFetcher.expectFetchAndSimulateResponse("response 3") // should try again after the period
            runCurrent()
            assertEquals("response 3", scheduler.getValue())
        }
    }

    @Test
    fun retries_invalidatingFetcher_setValueFromOnError() = runTest {
        val mockFetcher = MockInvalidatingFetcher<String>()

        val initialDelayMillis = 1000L
        val periodMillis = 5000L
        val maxRetries = 3

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
            maxRetries = maxRetries,
        ) { scheduler ->

            advanceTimeBy(initialDelayMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            mockFetcher.expectFetchAndSimulateResponse("response 1")
            runCurrent()
            assertEquals("response 1", scheduler.getValue(), "should return the fetched value")
            advanceTimeBy(periodMillis)
            runCurrent()
            // should retry immediately on error (without delay)
            repeat(maxRetries - 1) {
                mockFetcher.expectFetchAndSimulateError(RuntimeException("BOOM"))
                mockFetcher.expectOnErrorAndSimulateResponse("error value $it")
                runCurrent()
                assertEquals("error value $it", scheduler.getValue(), "should return error value from onError")
            }
            mockFetcher.expectFetchAndSimulateResponse("response 2")
            runCurrent()
            assertEquals("response 2", scheduler.getValue(), "should return the new value after a retry succeeded")

            advanceTimeBy(periodMillis)
            runCurrent()

            repeat(maxRetries) {
                mockFetcher.expectFetchAndSimulateError(RuntimeException("BOOM"))
                mockFetcher.expectOnErrorAndSimulateResponse("error value ${it + maxRetries - 1}")
                runCurrent()
                assertEquals("error value ${it + maxRetries - 1}", scheduler.getValue())
            }
            mockFetcher.assertNoPendingFetch() // should give up after max retries all failed

            advanceTimeBy(periodMillis)
            runCurrent()
            mockFetcher.expectFetchAndSimulateResponse("response 3") // should try again after the next period
            runCurrent()
            assertEquals("response 3", scheduler.getValue())
        }
    }

    @Test
    fun retries_invalidatingFetcher_recoverFailureInOnError() = runTest {
        val mockFetcher = MockInvalidatingFetcher<String>()

        val initialDelayMillis = 1000L
        val periodMillis = 5000L
        val maxRetries = 3

        withTestFetcherScheduler(
            fetcher = mockFetcher,
            periodMillis = periodMillis,
            initialDelayMillis = initialDelayMillis,
            maxRetries = maxRetries,
        ) { scheduler ->
            advanceTimeBy(initialDelayMillis)
            mockFetcher.assertNoPendingFetch() // shouldn't start before the initialDelay
            runCurrent()
            mockFetcher.expectFetchAndSimulateError(RuntimeException("error during fetching"))
            runCurrent()
            mockFetcher.expectOnErrorAndSimulateError(RuntimeException("but oh no! Error during onError recovery as well!"))
            runCurrent()
            mockFetcher.expectFetchAndSimulateError(RuntimeException("retry but fetch still failing"))
            runCurrent()
            mockFetcher.expectOnErrorAndSimulateResponse("on error result")
            runCurrent()
            assertEquals("on error result", scheduler.getValue(), "should return the first successful onError value")
        }
    }

    private suspend fun TestScope.withTestFetcherScheduler(
        fetcher: Fetcher<String>,
        periodMillis: Long = 5000,
        initialDelayMillis: Long = 1000,
        maxRetries: Int = 5,
        block: suspend TestScope.(fetcherScheduler: FetcherScheduler<String>) -> Unit,
    ) {
        val scheduler = FetcherScheduler(
            fetcher = fetcher,
            period = periodMillis.milliseconds,
            registry = SimpleMeterRegistry(),
            initialDelay = initialDelayMillis.milliseconds,
            maxRetries = maxRetries,
        )
        val fetcherJob = scheduler.startIn(this)

        try {
            block(scheduler)
        } finally {
            // This is to ensure the scheduler is cancelled even in case of test assertion error.
            // Structured concurrency is not sufficient to deal with this because runTest runs a test dispatcher
            // until all started coroutines are idle, and this never happens. Indeed, even though the MockFetcher hangs,
            // the infinite loop goes on because of the built-in timeout on fetch, so we retry indefinitely.
            fetcherJob.cancel()
        }
    }
}

private open class MockFetcher<T : Any>(
    override val name: String = "mock_fetcher",
) : Fetcher<T> {
    private val fetchRequests = Channel<Unit>(1)
    private val fetchResponses = Channel<Result<T>>()

    override suspend fun fetch(): T {
        // simulates suspension until the test method calls expectFetch()
        fetchRequests.send(Unit)
        return fetchResponses.receive().getOrThrow()
    }

    fun expectFetch() {
        val result = fetchRequests.tryReceive()
        if (result.isClosed) {
            fail("Fetch request channel was closed unexpectedly")
        }
        if (result.isFailure) {
            fail("Expected a fetch call, but the fetch request channel is empty")
        }
    }

    suspend fun expectFetchAndSimulateResponse(response: T) {
        expectFetch()
        fetchResponses.send(Result.success(response))
    }

    suspend fun expectFetchAndSimulateError(error: Throwable) {
        expectFetch()
        fetchResponses.send(Result.failure(error))
    }

    fun assertNoPendingFetch() {
        val result = fetchRequests.tryReceive()
        if (result.isClosed) {
            fail("Fetch request channel was closed unexpectedly")
        }
        if (result.isSuccess) {
            fail("Expected no fetch call, but a fetch request was enqueued")
        }
    }
}

private class MockInvalidatingFetcher<T : Any>(
    override val name: String = "mock_fetcher",
) : MockFetcher<T>(name), InvalidatingFetcher<T> {
    private val errorRequests = Channel<Unit>(1)
    private val errorResponses = Channel<Result<T>>()

    override suspend fun onError(): T {
        errorRequests.send(Unit)
        return errorResponses.receive().getOrThrow()
    }

    suspend fun expectOnError() {
        val result = errorRequests.receiveCatching()
        if (result.isClosed) {
            fail("onError request channel was closed unexpectedly")
        }
        if (result.isFailure) {
            fail("Expected an onError call, but the onError request channel is empty")
        }
    }

    suspend fun expectOnErrorAndSimulateResponse(response: T) {
        expectOnError()
        errorResponses.send(Result.success(response))
    }

    suspend fun expectOnErrorAndSimulateError(error: Throwable) {
        expectOnError()
        errorResponses.send(Result.failure(error))
    }
}
