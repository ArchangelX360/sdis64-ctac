package fr.sdis64.api.operators

import fr.sdis64.api.Identified
import kotlinx.serialization.Serializable

@Serializable
data class Operator(
    val post: String,
    val name: String,
    val function: String,
    val status: OperatorStatus,
    val phoneNumber: OperatorPhoneNumber,
)

@Serializable
data class OperatorPhoneNumber(
    override val id: Long?,
    val systelNumber: String,
    val realNumber: String,
) : Identified

@Serializable
data class OperatorStatus(
    override val id: Long?,
    val name: String,
    val backgroundColor: String? = "#000000",
    val textColor: String? = "#FFFFFF",
    val displayed: Boolean = false,
) : Identified
