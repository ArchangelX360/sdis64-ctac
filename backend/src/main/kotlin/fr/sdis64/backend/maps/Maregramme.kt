package fr.sdis64.backend.maps

import fr.sdis64.backend.maps.utilities.StorageConfiguration
import fr.sdis64.backend.utilities.AbstractScheduledFetcherService
import fr.sdis64.backend.utilities.FetcherScheduler
import fr.sdis64.backend.utilities.InvalidatingFetcher
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.micrometer.core.instrument.MeterRegistry
import jetbrains.datalore.base.values.Color
import kotlinx.datetime.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.letsPlot.export.ggsave
import org.jetbrains.letsPlot.geom.*
import org.jetbrains.letsPlot.ggsize
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleXDateTime
import org.jetbrains.letsPlot.scale.scaleYDiscrete
import org.jetbrains.letsPlot.themes.elementRect
import org.jetbrains.letsPlot.themes.elementText
import org.jetbrains.letsPlot.themes.theme
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.writeText
import kotlin.math.ceil
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ConfigurationProperties(prefix = "ctac.maps.maregramme")
data class MaregrammeConfiguration(
    private val url: String,
    private val portId: String,

    val width: Int,
    val height: Int,

    private val filename: String,
) {
    fun filepath(storageConfiguration: StorageConfiguration) = storageConfiguration.pathToMapFile(filename)

    val widgetUrl = "${url}/${portId}"
}

@Component
class MaregrammeRoutine(
    @Autowired private val httpClient: HttpClient,
    @Autowired private val storageConfiguration: StorageConfiguration,
    @Autowired private val maregrammeConfiguration: MaregrammeConfiguration,
    @Autowired private val registry: MeterRegistry,
) : AbstractScheduledFetcherService() {
    init {
        val scheduler = FetcherScheduler(
            Maregramme(
                maregrammeConfiguration,
                storageConfiguration,
                httpClient,
            ),
            1.minutes,
            registry,
            0.seconds,
        )
        scheduler.startIn(scheduledFetcherScope)
    }
}

private class Maregramme(
    private val configuration: MaregrammeConfiguration,
    private val storageConfiguration: StorageConfiguration,
    private val httpClient: HttpClient,
) : InvalidatingFetcher<Unit> {
    override val name = "maps_maregramme"

    private var currentDayDataCache: Data = Data.Unavailable

    private suspend fun fetchData(): Data {
        val d = currentDayDataCache
        if (d is Data.Available && !d.isStale()) {
            // data of the day is already cached
            return d
        }
        try {
            val response = httpClient.get(configuration.widgetUrl)
            val rawWidgetCode = response.body<String>()
            val html = extractHtml(rawWidgetCode)
            LOG.debug(html)
            currentDayDataCache = Data.Available(
                date = extractDate(html),
                seaLevels = readSeaLevels(html),
                seaCoefficients = extractFirstSeaCoefficient(html),
            )
        } catch (e: IllegalStateException) {
            LOG.error("failed to fetch maregramme data", e)
        }
        return currentDayDataCache
    }

    override suspend fun fetch() {
        when (val d = fetchData()) {
            is Data.Unavailable -> error("maregramme data unavailable")
            is Data.Available -> {
                val plottingDate = if (d.isStale()) {
                    val lastSecondOfDay = d.date.atTime(23, 59, 59).toInstant(TimeZone.currentSystemDefault())
                    LOG.warn("Maregramme data is stale, will plot for $lastSecondOfDay instead of for 'now'")
                    lastSecondOfDay
                } else {
                    Clock.System.now()
                }

                plotMaregramme(
                    d.seaLevels,
                    d.seaCoefficients,
                    plottingDate,
                    configuration.filepath(storageConfiguration),
                )
            }
        }
    }

    override suspend fun onError() {
        configuration.filepath(storageConfiguration)
            .writeText("""<svg xmlns="http://www.w3.org/2000/svg" width="${configuration.width}" height="${configuration.height}"></svg>""")
    }

    private fun plotMaregramme(
        seaLevels: List<SeaLevel>,
        seaCoefficients: List<SeaCoefficient>,
        instantToPlot: Instant,
        outputFile: Path,
    ) {
        // data are for French ports, it does not matter where the code runs
        val now = instantToPlot.toLocalDateTime(TimeZone.of("Europe/Paris"))

        val xAxis = "Heures du jour"
        val yAxis = "Niveau de la mer"

        val base = letsPlot {
            x = xAxis
            y = yAxis
        }

        val seaLevel = geomLine(
            data = mapOf<String, List<Any>>(
                xAxis to seaLevels.map { now.date.atTime(it.time).toPlottableTime() },
                yAxis to seaLevels.map { it.levelInMeter },
            ),
            color = Color.PACIFIC_BLUE,
            size = 3,
        )

        val currentY = seaLevels.approximateLevelAt(now)
        val currentX = now.toPlottableTime()
        val currentLevelCrosshair = geomPoint(
            x = currentX,
            y = currentY,
            color = Color.RED,
            size = 5,
        ) + geomVLine(
            xintercept = currentX,
            color = Color.RED,
            linetype = 4,
        ) + geomHLine(
            yintercept = currentY,
            color = Color.RED,
            linetype = 4,
        )

        val coefLabelOffset = 0.6
        val coefLabels = seaCoefficients.map { coef ->
            val x = now.date.atTime(coef.time).toPlottableTime()
            val label = when (coef) {
                is SeaCoefficient.HighSea -> "PM (${coef.value})\n${coef.time}"
                is SeaCoefficient.LowSea -> "BM\n${coef.time}"
            }
            geomVLine(
                xintercept = x,
                color = Color.WHITE,
                linetype = 3,
            ) + geomLabel(
                color = Color.WHITE,
                label = label,
                fill = ctacWallBackgroundColor,
                x = x,
                y = seaLevels.maxBy { it.levelInMeter }.levelInMeter + coefLabelOffset,
                size = 7,
            )
        }.reduce { acc, label -> acc.plus(label) }

        val maxSeaLevel = seaLevels.maxOf { it.levelInMeter }
        val scaleY = scaleYDiscrete(
            format = "{.0f}m",
            breaks = 0.rangeTo(ceil(maxSeaLevel).toInt()).toList(),
            limits = listOf(0, maxSeaLevel + coefLabelOffset + 0.2),
        )

        val scaleX = scaleXDateTime(
            format = "%H:%M",
            breaks = listOf(
                now.date.atTime(0, 0).toPlottableTime(),
                now.date.atTime(4, 0).toPlottableTime(),
                now.date.atTime(8, 0).toPlottableTime(),
                now.date.atTime(12, 0).toPlottableTime(),
                now.date.atTime(16, 0).toPlottableTime(),
                now.date.atTime(20, 0).toPlottableTime(),
                now.date.plus(1, DateTimeUnit.DAY).atTime(0, 0).toPlottableTime(),
            ),
        )

        val plot = base + ctacWallOptions + seaLevel + currentLevelCrosshair + coefLabels + scaleY + scaleX
        ggsave(
            plot = plot,
            filename = outputFile.name,
            path = outputFile.parent.absolutePathString(),
        )
    }

    private val ctacWallBackgroundColor = Color.parseHex("#424242")
    private val textTheme = elementText(
        color = Color.WHITE,
        family = listOf(
            "Helvetica Neue",
            "Helvetica",
            "Arial",
            "Lucida Grande",
            "sans-serif",
        ).joinToString(","),
        size = 18,
    )
    private val ctacWallOptions = ggsize(configuration.width, configuration.height) + theme(
        text = textTheme,
        legendText = textTheme,
        axisText = textTheme,
        axisTitle = textTheme,
        plotBackground = elementRect(
            fill = ctacWallBackgroundColor,
            color = Color.TRANSPARENT,
        ),
    )

    private fun LocalDateTime.toPlottableTime(): Long = toJavaLocalDateTime().toEpochSecond(ZoneOffset.UTC) * 1000
    private fun SeaLevel.toPlottableTime(d: LocalDate): Long = d.atTime(time).toPlottableTime()

    private fun List<SeaLevel>.approximateLevelAt(d: LocalDateTime): Float {
        val i = indexOfLast { it.time <= d.time }
        if (i == lastIndex) return last().levelInMeter
        val before = get(i)
        val beforeTime = before.toPlottableTime(d.date)
        val after = get(i + 1)
        val afterTime = after.toPlottableTime(d.date)
        return before.levelInMeter + (after.levelInMeter - before.levelInMeter) * (d.toPlottableTime() - beforeTime) / (afterTime - beforeTime)
    }

    private fun extractHtml(jsWidgetScriptText: String): String {
        val htmlExtractionRegex = "ifrm.document.write\\('(.*)'\\);".toRegex()
        return htmlExtractionRegex.findAllFirstGroup(jsWidgetScriptText)
            .joinToString("") { it.trim().replace("\\\"", "\"") }
    }

    private fun extractDate(html: String): LocalDate {
        val dateExtractionRegex = "<!--thead><tr><th colspan=\"4\">(.*?)</th></tr></thead-->".toRegex()
        val date = dateExtractionRegex.findFirstGroup(html) ?: error("failed to find date")
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return java.time.LocalDate.parse(date, format).toKotlinLocalDate()
    }

    private fun extractFirstSeaCoefficient(html: String): List<SeaCoefficient> {
        val tbody = "<tbody>(.*?)</tbody>".toRegex().findFirstGroup(html) ?: error("failed to find first `tbody`")
        val tds = "<td>(.*?)</td>".toRegex().findAllFirstGroup(tbody)
        return tds.windowed(4, partialWindows = false, step = 4) { row -> SeaCoefficient.fromTableRow(row) }.toList()
    }

    private fun readSeaLevels(html: String): List<SeaLevel> {
        val rawArray = ".*var data = (\\[.*]).*".toRegex().findFirstGroup(html) ?: error("failed to find raw data")
        return Json.decodeFromString<List<SeaLevel>>(rawArray)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(Maregramme::class.java)
    }
}

private fun Regex.findFirstGroup(s: String): String? = find(s)?.groupValues?.get(1)
private fun Regex.findAllFirstGroup(s: String): List<String> = findAll(s).map { it.groupValues[1] }.toList()

private sealed class Data {
    object Unavailable : Data()
    data class Available(
        val date: LocalDate,
        val seaLevels: List<SeaLevel>,
        val seaCoefficients: List<SeaCoefficient>,
    ) : Data() {
        fun isStale(): Boolean {
            val timezone = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            return date != now.toLocalDateTime(timezone).date
        }
    }
}

private sealed class SeaCoefficient(
    val time: LocalTime,
) {
    class HighSea(val value: Int, time: LocalTime) : SeaCoefficient(time)
    class LowSea(time: LocalTime) : SeaCoefficient(time)

    companion object {
        fun fromTableRow(row: List<String>): SeaCoefficient {
            if (row.size != 4) error("SeaCoefficient row must have 4 values")
            val (type, timeStr, _, valueStr) = row
            val time = LocalTime.parse("$timeStr:00")
            return when (type) {
                "PM" -> HighSea(time = time, value = valueStr.toInt())
                "BM" -> LowSea(time = time)
                else -> error("SeaCoefficient row start with either PM or BM")
            }
        }
    }
}

@Serializable(with = SeaLevelSerializer::class)
private data class SeaLevel(
    val levelInMeter: Float,
    val time: LocalTime,
)

@OptIn(ExperimentalSerializationApi::class)
private class SeaLevelSerializer : KSerializer<SeaLevel> {
    private val delegateSerializer = JsonArray.serializer()
    override val descriptor = SerialDescriptor("SeaLevel", delegateSerializer.descriptor)

    override fun serialize(encoder: Encoder, value: SeaLevel) = error("SeaLevel should not be serialized")

    override fun deserialize(decoder: Decoder): SeaLevel {
        val (timeStr, levelStr) = decoder.decodeSerializableValue(delegateSerializer).map {
            it.jsonPrimitive.content
        }
        return SeaLevel(
            time = LocalTime.parse(timeStr),
            levelInMeter = levelStr.toFloat(),
        )
    }
}
