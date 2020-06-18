package fr.sdis64.client

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

sealed class CtacResult<out T> {
    class Success<T>(val value: T) : CtacResult<T>()
    class Error(val throwable: Throwable) : CtacResult<Nothing>()
}

inline fun <T> poll(
    period: Duration,
    crossinline block: suspend () -> T,
): Flow<CtacResult<T>> = flow {
    while (true) {
        try {
            emit(CtacResult.Success(block()))
        } catch (e: Throwable) {
            // catching Throwable because 502 Gateway failures are not reported as Exception and will break the loop
            emit(CtacResult.Error(e))
        }
        delay(period)
    }
}

inline fun <T> triggerablePoll(
    trigger: ReceiveChannel<Unit>,
    crossinline block: suspend () -> T,
): Flow<CtacResult<T>> = flow {
    while (true) {
        try {
            emit(CtacResult.Success(block()))
        } catch (e: Throwable) {
            // catching Throwable because 502 Gateway failures are not reported as Exception and will break the loop
            emit(CtacResult.Error(e))
        }
        trigger.receive()
    }
}

fun CtacClient.operators(
    pollingPeriod: Duration = 10.seconds,
) = poll(pollingPeriod) { findOperators() }

fun CtacClient.callsStats(
    pollingPeriod: Duration = 5.seconds,
) = poll(pollingPeriod) { getCallsStats() }

fun CtacClient.interventionStats(
    pollingPeriod: Duration = 5.seconds,
) = poll(pollingPeriod) { getInterventionStats() }

fun CtacClient.responseTime(
    pollingPeriod: Duration = 5.seconds,
) = poll(pollingPeriod) { getResponseTime() }

fun CtacClient.unseenMailSubjects(
    pollingPeriod: Duration = 20.seconds,
) = poll(pollingPeriod) { getUnseenMailSubjects() }

fun CtacClient.griffonIndicator(
    pollingPeriod: Duration = 30.seconds,
) = poll(pollingPeriod) { getGriffonIndicator() }

fun CtacClient.weatherIndicators(
    pollingPeriod: Duration = 20.seconds,
) = poll(pollingPeriod) { getWeatherIndicators() }

fun CtacClient.activeManualIndicators(
    pollingPeriod: Duration = 1.minutes,
) = poll(pollingPeriod) { findManualIndicatorLevels(active = true) }

fun CtacClient.activeOrganisms(
    categoryId: Long?,
    pollingPeriod: Duration = 1.minutes,
) = poll(pollingPeriod) {
    findAllOrganisms(categoryId = categoryId, activeAt = Clock.System.now())
}

fun CtacClient.displayableVehicles(
    pollingPeriod: Duration = 10.seconds,
) = poll(pollingPeriod) { getDisplayableVehicles() }

fun CtacClient.helicopters(
    pollingPeriod: Duration = 10.seconds,
) = poll(pollingPeriod) { getHelicopters() }

fun CtacClient.vehiclesMaps(
    pollingPeriod: Duration = 10.seconds,
) = poll(pollingPeriod) { getVehicleMaps() }

fun CtacClient.vehiclesMap(
    name: String,
    pollingPeriod: Duration = 10.seconds,
) = poll(pollingPeriod) { getVehicleMap(name) }

fun CtacClient.latestCriticalChange(
    pollingPeriod: Duration = 30.seconds,
) = poll(pollingPeriod) { getLatestCriticalChange() }
