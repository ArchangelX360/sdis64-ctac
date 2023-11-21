package fr.sdis64.backend.vehicles

import fr.sdis64.api.DisplayOption
import fr.sdis64.api.vehicles.*
import fr.sdis64.backend.cis.CisRepository
import fr.sdis64.backend.test.configurations.TestDbConfiguration
import fr.sdis64.backend.test.systelClientWithMockedHttp
import fr.sdis64.backend.utilities.toDTO
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import fr.sdis64.backend.cis.entities.Cis as CisEntity
import fr.sdis64.backend.vehicles.entities.VehicleStatus as VehicleStatusEntity
import fr.sdis64.backend.vehicles.entities.VehicleType as VehicleTypeEntity

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        CisRepository::class,
        VehicleStatusRepository::class,
        VehicleTypeRepository::class,
        TestDbConfiguration::class,
    ],
    initializers = [TestDbConfiguration.Initializer::class],
)
@ActiveProfiles("test")
class VehicleServiceTest {

    @Autowired
    private lateinit var cisRepository: CisRepository

    @Autowired
    private lateinit var vehicleStatusRepository: VehicleStatusRepository

    @Autowired
    private lateinit var vehicleTypeRepository: VehicleTypeRepository

    @AfterEach
    fun tearDown() {
        cisRepository.deleteAll()
        vehicleStatusRepository.deleteAll()
        vehicleTypeRepository.deleteAll()
    }

    // FIXME: add failure cases, unparsable vehicle, missing type/statut/cis, etc.

    @Test
    fun shouldGetVehicles() = runBlocking {
        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("materiel.departement") {
                respondOk(
                    """{"result":[["VLU", "33", "USTARITZ", "DISPONIBLE", "65280", "0", "VLU", "1", "ARMABLE"],["VLU", "88", "SOUMOULOU", "DISPONIBLE", "65280", "0", "VLU", "2", "ARMABLE"],["FPT", "28", "USTARITZ", "DISPONIBLE SECONDAIRE", "65280", "0", "FPTL_PS", "1", "NON ARMABLE"],["FPT", "28", "USTARITZ", "DISPONIBLE", "65280", "0", "FPT", "1", "ARMABLE"]],"errors":[]}"""
                )
            }
        }

        val ustaritz = cisRepository.save(
            CisEntity(
                name = "USTARITZ",
                code = "UTZ",
                displayToCta = true,
                displayToCodis = true,
                displayPosition = 2,
                systelId = 48L,
            )
        )

        val soumoulou = cisRepository.save(
            CisEntity(
                name = "SOUMOULOU",
                code = "SML",
                displayToCta = true,
                displayToCodis = true,
                displayPosition = 1,
                systelId = 5L,
            )
        )

        val disponible = vehicleStatusRepository.save(
            VehicleStatusEntity(
                name = "DISPONIBLE",
                textColor = "#000000",
                backgroundColor = "#FFFFFF",
                position = 2,
                mode = VehicleStatus.Mode.BLACKLIST,
                blacklist = emptySet(),
                category = VehicleStatus.Category.AVAILABLE,
            )
        )

        val disponibleSecondaire = vehicleStatusRepository.save(
            VehicleStatusEntity(
                name = "DISPONIBLE SECONDAIRE",
                textColor = "#000000",
                backgroundColor = "#FFFFFF",
                position = 1,
                mode = VehicleStatus.Mode.BLACKLIST,
                blacklist = emptySet(),
                category = VehicleStatus.Category.AVAILABLE,
            )
        )

        val vlu = vehicleTypeRepository.save(
            VehicleTypeEntity(
                name = "VLU",
                displayToCta = true,
                displayToCodis = true,
                displayPosition = 1,
            )
        )

        val fpt = vehicleTypeRepository.save(
            VehicleTypeEntity(
                name = "FPT",
                displayToCta = true,
                displayToCodis = true,
                displayPosition = 1,
            )
        )

        val fptlPs = vehicleTypeRepository.save(
            VehicleTypeEntity(
                name = "FPTL_PS",
                displayToCta = true,
                displayToCodis = true,
                displayPosition = 4,
            )
        )

        val vehicleService = VehicleService(
            VehicleConfiguration(1000, ""),
            systelClient,
            cisRepository,
            vehicleTypeRepository,
            vehicleStatusRepository,
            SimpleMeterRegistry(),
        )
        val vehicles = vehicleService.getVehicles()

        assertEquals(3, vehicles.size)
        assertTrue(
            vehicles.contains(
                Vehicle(
                    cis = ustaritz.toDTO(),
                    name = vlu.name,
                    order = 33,
                    primaryFunction = VehicleFunction(
                        type = vlu.toDTO(),
                        state = VehicleState.ARMABLE,
                        status = disponible.toDTO()
                    ),
                    secondaryFunctions = emptySet(),
                )
            )
        )
        assertTrue(
            vehicles.contains(
                Vehicle(
                    cis = soumoulou.toDTO(),
                    name = vlu.name,
                    order = 88,
                    primaryFunction = VehicleFunction(
                        type = vlu.toDTO(),
                        state = VehicleState.ARMABLE,
                        status = disponible.toDTO()
                    ),
                    secondaryFunctions = emptySet(),
                )
            )
        )
        assertTrue(
            vehicles.contains(
                Vehicle(
                    cis = ustaritz.toDTO(),
                    name = fpt.name,
                    order = 28,
                    primaryFunction = VehicleFunction(
                        type = fpt.toDTO(),
                        state = VehicleState.ARMABLE,
                        status = disponible.toDTO()
                    ),
                    secondaryFunctions = setOf(
                        VehicleFunction(
                            type = fptlPs.toDTO(),
                            state = VehicleState.NON_ARMABLE,
                            status = disponibleSecondaire.toDTO(),
                        ),
                    ),
                )
            )
        )
    }

    @Test
    fun shouldGetDisplayableVehicles() = runBlocking {
        val noDisplay = DisplayOption(position = 1, toCta = false, toCodis = false)
        val display = DisplayOption(position = 2, toCta = true, toCodis = false)

        val cisNoDisplay = Cis(
            id = null,
            name = "noDisplay",
            code = "NOD",
            displayOption = noDisplay,
            systelId = null,
        )
        val cisDisplay = Cis(
            id = null,
            name = "display",
            code = "DIS",
            displayOption = display,
            systelId = null,
        )
        val typeNoDisplay = VehicleType(
            id = null,
            name = "noDisplay",
            displayOption = noDisplay,
        )
        val typeDisplay = VehicleType(
            id = null,
            name = "display",
            displayOption = display,
        )
        val statusNoDisplay = VehicleStatus(
            id = null,
            name = "noDisplay",
            category = VehicleStatus.Category.AVAILABLE,
            mode = VehicleStatus.Mode.BLACKLIST,
            blacklist = setOf(typeDisplay, typeNoDisplay), // preventing all
        )
        val statusNoDisplay2 = VehicleStatus(
            id = null,
            name = "noDisplay2",
            category = VehicleStatus.Category.AVAILABLE,
            mode = VehicleStatus.Mode.WHITELIST,
            whitelist = emptySet(), // authorizing none
        )
        val statusDisplay = VehicleStatus(
            id = null,
            name = "display",
            category = VehicleStatus.Category.AVAILABLE,
            mode = VehicleStatus.Mode.WHITELIST,
            whitelist = setOf(typeDisplay, typeNoDisplay), // preventing all
        )

        val notDisplayedBecauseCis = Vehicle(
            cis = cisNoDisplay,
            name = typeDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeDisplay,
                status = statusDisplay,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 33,
        )

        val notDisplayedBecauseStatus = Vehicle(
            cis = cisDisplay,
            name = typeDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeDisplay,
                status = statusNoDisplay,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 34
        )

        val notDisplayedBecauseStatus2 = Vehicle(
            cis = cisDisplay,
            name = typeDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeDisplay,
                status = statusNoDisplay2,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 35,
        )

        val notDisplayedBecauseType = Vehicle(
            cis = cisDisplay,
            name = typeNoDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeNoDisplay,
                status = statusDisplay,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = setOf(
                VehicleFunction(
                    type = typeDisplay,
                    status = statusDisplay,
                    state = VehicleState.ARMABLE,
                ),
            ),
            order = 36,
        )

        val displayed = Vehicle(
            cis = cisDisplay,
            name = typeDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeDisplay,
                status = statusDisplay,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = emptySet(),
            order = 37,
        )

        val displayed2 = Vehicle(
            cis = cisDisplay,
            name = typeDisplay.name,
            primaryFunction = VehicleFunction(
                type = typeDisplay,
                status = statusDisplay,
                state = VehicleState.ARMABLE,
            ),
            secondaryFunctions = setOf(
                VehicleFunction(
                    // verifying that secondary function is ignored
                    type = typeNoDisplay,
                    status = statusDisplay,
                    state = VehicleState.ARMABLE,
                ),
            ),
            order = 38,
        )

        val vehicles = HashSet(
            listOf(
                notDisplayedBecauseCis,
                notDisplayedBecauseStatus,
                notDisplayedBecauseStatus2,
                notDisplayedBecauseType,
                displayed,
                displayed2,
            )
        )

        val vehicleServiceSpy = spyk(
            VehicleService(
                VehicleConfiguration(1000, ""),
                mockk(),
                mockk(),
                mockk(),
                mockk(),
                SimpleMeterRegistry(),
            )
        )

        coEvery { vehicleServiceSpy.getVehicles() } returns vehicles

        val actual = vehicleServiceSpy.getDisplayableVehicles() // this is the real method call as it is a spy
        assertEquals(2, actual.size)
        assertTrue(actual.contains(displayed))
        assertTrue(actual.contains(displayed2))
    }
}
