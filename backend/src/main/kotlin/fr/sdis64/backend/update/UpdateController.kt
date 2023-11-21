package fr.sdis64.backend.update

import kotlinx.datetime.Instant
import kotlinx.datetime.toKotlinInstant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ConfigurationProperties(prefix = "ctac.update")
data class UpdateConfiguration(
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val latestCriticalChange: java.time.Instant
)

@RestController
@RequestMapping("/update")
class UpdateController(
    @Autowired private val updateConfiguration: UpdateConfiguration,
) {
    @GetMapping(value = ["/latest-critical-change"])
    fun latestCriticalChange(): Instant = updateConfiguration.latestCriticalChange.toKotlinInstant()
}
