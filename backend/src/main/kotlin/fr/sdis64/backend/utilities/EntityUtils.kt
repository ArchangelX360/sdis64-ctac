package fr.sdis64.backend.utilities

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import java.util.*

fun <T> Optional<T>.orNotFound(): T = this.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND) }

inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R): Set<R> = mapTo(HashSet()) { transform(it) }
