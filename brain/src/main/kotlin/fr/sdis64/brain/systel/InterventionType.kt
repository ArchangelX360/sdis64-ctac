package fr.sdis64.brain.systel

enum class InterventionType(
    val systelKey: String,
) {
    SAP(systelKey = "ACCIDENT NE NECESSITANT QUE DES SECOURS A VICTIMES"),
    AVP(systelKey = "ACCIDENT DE LA CIRCULATION"),
    INC(systelKey = "INCENDIE"),
    DIV(systelKey = "OPERATIONS DIVERSES"),
    RT(systelKey = "RISQUES TECHNOLOGIQUES");

    companion object {
        private val types: Map<String, InterventionType> = values().associateBy { it.systelKey }

        fun create(systelKey: String?): InterventionType =
            types[systelKey] ?: error("intervention type '$systelKey' not found")
    }
}
