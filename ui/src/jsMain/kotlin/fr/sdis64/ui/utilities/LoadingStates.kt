package fr.sdis64.ui.utilities

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import fr.sdis64.client.CtacResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

sealed class LoadableResult<out T> {
    data class Success<out T>(val value: T) : LoadableResult<T>()
    data class Failure(val throwable: Throwable) : LoadableResult<Nothing>()
    object Loading : LoadableResult<Nothing>()
}

@Composable
fun <T> loaderOr(
    result: LoadableResult<CtacResult<T>>,
    block: @Composable (T) -> Unit,
) = when (result) {
    is LoadableResult.Success -> {
        when (val v = result.value) {
            is CtacResult.Success -> block(v.value)
            is CtacResult.Error -> {
                console.error(v.throwable)
                Spinner() // TODO: Failure and Loading might need to be expressed in different ways, it would be nice to have the tile greyed indicating stale data instead for example
            }
        }
    }

    is LoadableResult.Failure -> {
        console.error(result)
        Spinner() // TODO: Failure and Loading might need to be expressed in different ways, it would be nice to have the tile greyed indicating stale data instead for example
    }

    is LoadableResult.Loading -> Spinner()
}

@Composable
fun <T> errorOr(
    result: LoadableResult<CtacResult<T>>,
    block: @Composable (T) -> Unit,
) = when (result) {
    is LoadableResult.Success -> {
        when (val v = result.value) {
            is CtacResult.Success -> block(v.value)
            is CtacResult.Error -> {
                console.error(v.throwable)
            }
        }
    }

    is LoadableResult.Failure -> console.error(result)
    is LoadableResult.Loading -> {}
}

@Composable
fun <T> rememberLoadingState(block: () -> Flow<T>): State<LoadableResult<T>> =
    remember { block().asLoadableResult() }.collectAsState(LoadableResult.Loading)

fun <T> Flow<T>.asLoadableResult(): Flow<LoadableResult<T>> =
    map<T, LoadableResult<T>> { LoadableResult.Success(it) }.catch { e -> emit(LoadableResult.Failure(e)) }
