package fr.sdis64.backend.systel

data class SystelOperator(
    val post: String,
    val name: String,
    val function: String,
    val status: String,
    val phoneNumber: String,
)

fun SystelResponse.toSystelOperators(): List<SystelOperator> = this.result.map {
    SystelOperator(
        post = it[2],
        name = it[1],
        function = it[3],
        status = it[4],
        phoneNumber = it[0],
    )
}
