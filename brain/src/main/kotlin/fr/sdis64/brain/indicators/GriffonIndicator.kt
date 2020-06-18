package fr.sdis64.brain.indicators

enum class GriffonIndicator(
    val level: String,
    val backgroundColor: String,
    val textColor: String,
) {
    ND(level = "ND", backgroundColor = "#ffffff", textColor = "#000000"),
    FAIBLE(level = "Faible", backgroundColor = "#2196F3", textColor = "#000000"),
    LEGER(level = "Léger", backgroundColor = "#4CAF50", textColor = "#000000"),
    MODERE(level = "Modéré", backgroundColor = "#FFEB3B", textColor = "#000000"),
    SEVERE(level = "Sévère", backgroundColor = "#FF9800", textColor = "#000000"),
    TRES_SEVERE(level = "Très sévère", backgroundColor = "#F44336", textColor = "#000000"),
    EXCEPTIONNEL(level = "Exceptionnel", backgroundColor = "#000000", textColor = "#ffffff");

    companion object {
        private val indicators: Map<String, GriffonIndicator> = values().associateBy { it.level }

        fun create(level: String?): GriffonIndicator = indicators[level] ?: ND
    }
}
