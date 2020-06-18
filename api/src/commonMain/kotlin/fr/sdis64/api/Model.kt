package fr.sdis64.api

import kotlinx.serialization.Serializable

interface Identified {
    val id: Long?
}

@Serializable
data class Session(val username: String)

@Serializable
data class DisplayOption(
    val toCta: Boolean = false,
    val toCodis: Boolean = false,
    val position: Int = 999,
) : Comparable<DisplayOption> {

    val displayable: Boolean
        get() = this.toCodis || this.toCta

    override fun compareTo(other: DisplayOption): Int = this.position.compareTo(other.position)
}
