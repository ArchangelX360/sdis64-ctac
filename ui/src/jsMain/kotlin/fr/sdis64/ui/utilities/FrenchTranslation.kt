package fr.sdis64.ui.utilities

fun String.toFrench(): String {
    return when (this.lowercase()) {
        "monday" -> "Lundi"
        "tuesday" -> "Mardi"
        "wednesday" -> "Mercredi"
        "thursday" -> "Jeudi"
        "friday" -> "Vendredi"
        "saturday" -> "Samedi"
        "sunday" -> "Dimanche"

        "january" -> "Janvier"
        "february" -> "Février"
        "march" -> "Mars"
        "april" -> "Avril"
        "may" -> "Mai"
        "june" -> "Juin"
        "july" -> "Juillet"
        "august" -> "Août"
        "september" -> "Septembre"
        "october" -> "Octobre"
        "november" -> "Novembre"
        "december" -> "Décembre"

        else -> this
    }
}
