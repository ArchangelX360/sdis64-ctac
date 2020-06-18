package fr.sdis64.brain.utilities

import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import java.util.concurrent.Executors

open class CoroutineScopedComponent {

    protected val componentScope: CoroutineScope =
        CoroutineScope(CoroutineName("${this::class.simpleName}-coroutine") + SupervisorJob())

    @PreDestroy
    fun cancelScope() {
        componentScope.cancel()
    }
}

open class AbstractScheduledFetcherService : CoroutineScopedComponent() {

    companion object {
        // this dispatcher is in the companion so it is shared by all fetchers
        private val fetcherDispatcher = Executors.newFixedThreadPool(5).asCoroutineDispatcher()
    }

    protected val scheduledFetcherScope: CoroutineScope = componentScope + fetcherDispatcher

    @PreDestroy
    fun cancelFetcherScope() {
        scheduledFetcherScope.cancel()
    }
}
