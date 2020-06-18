package fr.sdis64.api.vehicles

import fr.sdis64.api.*
import kotlin.test.Test
import kotlin.test.assertEquals

class VehicleTest {

    @Test
    fun shouldSort() {
        val display1 = DisplayOption(toCta = false, toCodis = false, position = 1)
        val display2 = DisplayOption(toCta = false, toCodis = false, position = 2)
        val cis1 = Cis(id = null, name = "c1", code = "C1", displayOption = display1, systelId = 1)
        val cis2 = Cis(id = null, name = "c2", code = "C2", displayOption = display2, systelId = 1)
        val type1 = VehicleType(id = null, name = "t1", displayOption = display1)
        val type2 = VehicleType(id = null, name = "t2", displayOption = display2)
        val status1 = VehicleStatus(
            id = null,
            name = "s1",
            category = VehicleStatus.Category.AVAILABLE,
            mode = VehicleStatus.Mode.WHITELIST,
            position = 1
        )
        val status2 = VehicleStatus(
            id = null,
            name = "s2",
            category = VehicleStatus.Category.AVAILABLE,
            mode = VehicleStatus.Mode.WHITELIST,
            position = 2
        )

        val v111 = Vehicle(
            cis = cis1,
            name = type1.name,
            primaryFunction = VehicleFunction(
                status = status1,
                type = type1,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 111,
        )
        val v112 = Vehicle(
            cis = cis1,
            name = type2.name,
            primaryFunction = VehicleFunction(
                status = status1,
                type = type2,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 112,
        )
        val v121 = Vehicle(
            cis = cis1,
            name = type1.name,
            primaryFunction = VehicleFunction(
                status = status2,
                type = type1,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 121,
        )
        val v122 = Vehicle(
            cis = cis1,
            name = type2.name,
            primaryFunction = VehicleFunction(
                status = status2,
                type = type2,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 122,
        )
        val v211 = Vehicle(
            cis = cis2,
            name = type1.name,
            primaryFunction = VehicleFunction(
                status = status1,
                type = type1,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 211,
        )
        val v212 = Vehicle(
            cis = cis2,
            name = type2.name,
            primaryFunction = VehicleFunction(
                status = status1,
                type = type2,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 212,
        )
        val v221 = Vehicle(
            cis = cis2,
            name = type1.name,
            primaryFunction = VehicleFunction(
                status = status2,
                type = type1,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 221,
        )
        val v222 = Vehicle(
            cis = cis2,
            name = type2.name,
            primaryFunction = VehicleFunction(
                status = status2,
                type = type2,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 222,
        )

        val vehicles = mutableListOf(
            v222,
            v221,
            v212,
            v211,
            v122,
            v121,
            v112,
            v111,
        )

        val expected = mutableListOf(
            v111,
            v112,
            v121,
            v122,
            v211,
            v212,
            v221,
            v222,
        )

        vehicles.sort()

        assertEquals(expected, vehicles)
    }

}
