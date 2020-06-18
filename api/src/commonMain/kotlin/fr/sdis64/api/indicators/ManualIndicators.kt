package fr.sdis64.api.indicators

import fr.sdis64.api.Identified
import kotlinx.serialization.Serializable

@Serializable
data class ManualIndicatorLevel(
    override val id: Long?,
    val name: String,
    val category: ManualIndicatorCategory,
    val descriptions: Set<String> = emptySet(),
    val active: Boolean = false,
) : Identified

@Serializable
data class ManualIndicatorCategory(
    override val id: Long?,
    val name: String,
    val type: ManualIndicatorType,
) : Identified

@Serializable
enum class ManualIndicatorType(val displayName: String) {
    VIGIPIRATE("Vigipirate"),
    ORDRE_PUBLIC("Ordre public"),
}
