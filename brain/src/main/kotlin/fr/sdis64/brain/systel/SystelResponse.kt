package fr.sdis64.brain.systel

data class SystelResponse(
    val result: List<List<String>>,
) {
    /**
     * Convert a map-like SystelResponse to the map it represents
     *
     * Example of a map-like SystelResponse:
     * ```
     * {
     *   "result":[
     *     ["AUTRES ACTIVITES",1],
     *     ["ACCIDENT NE NECESSITANT QUE DES SECOURS A VICTIMES",17],
     *     ["ACCIDENT DE LA CIRCULATION",3],
     *     ["RISQUES TECHNOLOGIQUES",1]
     *   ],
     *   "errors":[]
     * }
     * ```
     *
     * @return a map corresponding of the result array or throws an exception
     */
    fun toMap(): Map<String, Int> = result.associate { it.toPair() }

    private fun List<String>.toPair(): Pair<String, Int> {
        require(size == 2) { "SystelResponse result element should contain two elements that represents a map entry, got $size elements" }
        return this[0] to this[1].toInt()
    }

    /**
     * Convert a single value SystelResponse to its integer value
     *
     * Example of a single value SystelResponse:
     * ```
     * {
     *   "result":[
     *     [3148]
     *   ],
     *   "errors":[]
     * }
     * ```
     *
     * @return the value or throws an exception
     */
    fun toInteger(): Int {
        require(result.size == 1) { "SystelResponse result array must contain one single element, got ${result.size}" }
        require(result[0].size == 1) { "SystelResponse first element of the result array must contain one single element, got ${result[0].size}" }
        return result[0][0].toInt()
    }
}
