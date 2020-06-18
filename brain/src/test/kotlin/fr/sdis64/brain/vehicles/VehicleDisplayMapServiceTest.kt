package fr.sdis64.brain.vehicles

import fr.sdis64.api.DisplayOption
import fr.sdis64.api.vehicles.*
import fr.sdis64.brain.cis.CisRepository
import fr.sdis64.brain.operators.OperatorPhoneNumberRepository
import fr.sdis64.brain.operators.OperatorStatusRepository
import fr.sdis64.brain.test.configurations.TestDbConfiguration
import fr.sdis64.brain.test.systelClientWithMockedHttp
import fr.sdis64.brain.utilities.toDTO
import io.ktor.client.engine.mock.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
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
import kotlin.test.fail
import fr.sdis64.brain.cis.entities.Cis as CisEntity
import fr.sdis64.brain.vehicles.entities.VehicleMap as VehicleMapEntity
import fr.sdis64.brain.vehicles.entities.VehicleStatus as VehicleStatusEntity
import fr.sdis64.brain.vehicles.entities.VehicleType as VehicleTypeEntity

@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    classes = [
        OperatorStatusRepository::class,
        OperatorPhoneNumberRepository::class,
        TestDbConfiguration::class,
    ],
    initializers = [TestDbConfiguration.Initializer::class],
)
@ActiveProfiles("test")
class VehicleDisplayMapServiceTest {

    @Autowired
    private lateinit var vehicleMapRepository: VehicleMapRepository

    @Autowired
    private lateinit var vehicleTypeRepository: VehicleTypeRepository

    @Autowired
    private lateinit var vehicleStatusRepository: VehicleStatusRepository

    @Autowired
    private lateinit var cisRepository: CisRepository

    private fun getCis(): Cis = Cis(
        id = 1L,
        name = "TEST_CIS",
        code = "TST",
        displayOption = DisplayOption(toCta = false, toCodis = false, position = 1),
        systelId = null,
    )

    private fun getStatus(name: String, category: VehicleStatus.Category): VehicleStatus =
        VehicleStatus(
            id = null,
            name = name,
            category = category,
            mode = VehicleStatus.Mode.BLACKLIST,
        )

    private fun saveTypeEntity(name: String): VehicleTypeEntity = vehicleTypeRepository.save(
        VehicleTypeEntity(
            name = name,
            displayToCta = false,
            displayToCodis = false,
            displayPosition = 1,
        )
    )

    private fun saveStatusEntity(
        name: String,
        category: VehicleStatus.Category,
        mode: VehicleStatus.Mode
    ): VehicleStatusEntity = vehicleStatusRepository.save(
        VehicleStatusEntity(
            name = name,
            category = category,
            mode = mode,
        )
    )

    @AfterEach
    fun tearDown() {
        vehicleMapRepository.deleteAll()
        vehicleTypeRepository.deleteAll()
        vehicleStatusRepository.deleteAll()
        cisRepository.deleteAll()
    }

    @Test
    fun shouldGetMapAvailability() = runBlocking {
        val testType = saveTypeEntity("test")
        val testType2 = saveTypeEntity("test2")
        val testSecondaryType = saveTypeEntity("test_secondary")
        val testSecondaryNotForThisMap = saveTypeEntity("test_secondary_not_for_this_map")

        val vehicleMap = VehicleMapEntity(
            name = "TEST",
            types = setOf(testType),
            degradedTypes = setOf(testSecondaryType),
        )
        vehicleMapRepository.save(vehicleMap)

        val vehicleService: VehicleService = mockk()
        coEvery { vehicleService.getVehicles() } returns setOf(
            // AVAILABLE
            Vehicle(
                name = "test",
                order = 1,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("", VehicleStatus.Category.AVAILABLE),
                    type = testType.toDTO(),
                    state = VehicleState.ARMABLE,
                ),
                secondaryFunctions = emptySet(),
            ),
            // ARMABLE IN SECONDARY FUNCTION
            Vehicle(
                name = "test",
                order = 11,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("", VehicleStatus.Category.NON_ARMABLE),
                    type = testType.toDTO(),
                    state = VehicleState.UNAVAILABLE,
                ),
                secondaryFunctions = setOf(
                    VehicleFunction(
                        status = getStatus("", VehicleStatus.Category.AVAILABLE),
                        type = testSecondaryType.toDTO(),
                        state = VehicleState.ARMABLE,
                    ),
                ),
            ),

            // NON_ARMABLE DESPITE SECONDARY FUNCTION
            Vehicle(
                name = "test",
                order = 12,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("", VehicleStatus.Category.NON_ARMABLE),
                    type = testType.toDTO(),
                    state = VehicleState.UNAVAILABLE,
                ),
                secondaryFunctions = setOf(
                    VehicleFunction(
                        status = getStatus("", VehicleStatus.Category.AVAILABLE),
                        type = testSecondaryType.toDTO(),
                        state = VehicleState.NON_ARMABLE,
                    ),
                ),
            ),

            // UNAVAILABLE BECAUSE NON SUPPORTED SECONDARY FUNCTION
            Vehicle(
                name = "test",
                order = 13,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("", VehicleStatus.Category.UNAVAILABLE),
                    type = testType.toDTO(),
                    state = VehicleState.UNAVAILABLE,
                ),
                secondaryFunctions = setOf(
                    VehicleFunction(
                        status = getStatus("", VehicleStatus.Category.AVAILABLE),
                        type = testSecondaryNotForThisMap.toDTO(),
                        state = VehicleState.ARMABLE,
                    ),
                ),
            ),


            // IGNORED NOT RIGHT TYPE
            Vehicle(
                name = "test",
                order = 2,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("", VehicleStatus.Category.AVAILABLE),
                    type = testType2.toDTO(),
                    state = VehicleState.ARMABLE,
                ),
                secondaryFunctions = emptySet(),
            ),

            // IGNORED BLACKLISTED STATUS
            Vehicle(
                name = "test",
                order = 111,
                cis = getCis(),
                primaryFunction = VehicleFunction(
                    status = getStatus("TEST_BLACKLIST", VehicleStatus.Category.AVAILABLE),
                    type = testType.toDTO(),
                    state = VehicleState.ARMABLE,
                ),
                secondaryFunctions = emptySet(),
            ),
        )

        val vehicleDisplayMapService = VehicleDisplayMapService(
            vehicleMapRepository,
            vehicleService,
            VehicleMapsConfiguration(statusNameBlacklist = listOf("TEST_BLACKLIST")),
        )

        val actual = vehicleDisplayMapService.getVehicleDisplayMaps()

        assertEquals(1, actual.size)
        assertTrue { actual.first().cisToAvailability.containsKey(getCis().name) }
        assertEquals(vehicleMap.name, actual.first().mapName)
        assertEquals(1, actual.first().cisToAvailability[getCis().name]!!.available)
        assertEquals(1, actual.first().cisToAvailability[getCis().name]!!.armableDegraded)
        assertEquals(2, actual.first().cisToAvailability[getCis().name]!!.nonArmable)
        assertEquals(1, actual.first().cisToAvailability[getCis().name]!!.unavailable)
        assertEquals(4, actual.first().cisToAvailability[getCis().name]!!.total)
    }

    // Arudy was duplicated vehicles in the previous implementation, this prevent regression
    @Test
    fun shouldNotConsiderSecondaryFunctionAsDuplicatedVehicle() = runBlocking {
        val arudy = CisEntity(
            name = "ARUDY",
            code = "ADY",
            systelId = 13L,
            displayToCta = true,
            displayToCodis = true,
            displayPosition = 1,
        )
        cisRepository.save(arudy)

        val ccfmType = saveTypeEntity("CCFM")
        saveTypeEntity("CCFM_DIV")
        val ccfmFdfType = saveTypeEntity("CCFM_FDF")
        val fptlsrType = saveTypeEntity("FPTLSR")
        saveTypeEntity("VSRM")
        saveTypeEntity("FPTL_VEAPS")
        saveTypeEntity("FPTL_VEA")
        val fptlType = saveTypeEntity("FPTL")
        val fptlPsType = saveTypeEntity("FPTL_PS")
        saveTypeEntity("MPR_90")
        saveTypeEntity("VATF")
        saveTypeEntity("VGR")
        saveTypeEntity("VLHR")
        saveTypeEntity("VLHR_FDF")
        saveTypeEntity("VSN_SEV")
        saveTypeEntity("VLU_EVAC")
        saveTypeEntity("VLU_MED")
        saveTypeEntity("VTU")
        saveTypeEntity("VLU_INC")
        saveTypeEntity("VLU_SAP")
        saveTypeEntity("VLU")
        saveTypeEntity("VLU_CA_INC")
        saveTypeEntity("VLU_CA_SAP")
        saveTypeEntity("VSN")
        saveTypeEntity("VLU_ASCEN")
        saveTypeEntity("VTU_HYM")
        saveTypeEntity("VLU_PS")
        val vsavType = saveTypeEntity("VSAV")
        saveTypeEntity("VSAV_MI")
        val vsavPsType = saveTypeEntity("VSAV_PS")
        val vsavPsCaType = saveTypeEntity("VSAV_PS_CA")
        val vsacPsSpType = saveTypeEntity("VSAV_PS_SP")
        val vsavHrType = saveTypeEntity("VSAVHR")
        val vlsmType = saveTypeEntity("VLSM")
        val vliType = saveTypeEntity("VLI")
        val ccrlType = saveTypeEntity("CCRL")
        val ccrmType = saveTypeEntity("CCRM")
        val fptType = saveTypeEntity("FPT")
        val fptsrType = saveTypeEntity("FPTSR")
        val fptPsType = saveTypeEntity("FPT_PS")
        val ccrmPsType = saveTypeEntity("CCRM_PS")

        saveStatusEntity("ARMABLE", VehicleStatus.Category.AVAILABLE, VehicleStatus.Mode.BLACKLIST)
        saveStatusEntity("NON ARMABLE", VehicleStatus.Category.NON_ARMABLE, VehicleStatus.Mode.WHITELIST)
        saveStatusEntity("INDISPONIBLE", VehicleStatus.Category.UNAVAILABLE, VehicleStatus.Mode.WHITELIST)

        val sapMap = VehicleMapEntity(
            name = "SAP",
            types = setOf(
                vsavType,
                vsavHrType,
            ),
            degradedTypes = setOf(
                vsavPsType,
                vsavPsCaType,
                vsacPsSpType,
            ),
        )
        vehicleMapRepository.save(sapMap)

        val incMap = VehicleMapEntity(
            name = "INC",
            types = setOf(
                ccrlType,
                ccrmType,
                fptType,
                fptlType,
                fptsrType,
                fptlsrType,
            ),
            degradedTypes = setOf(
                fptPsType,
                fptlPsType,
                ccrmPsType,
            ),
        )
        vehicleMapRepository.save(incMap)

        val fdfMap = VehicleMapEntity(
            name = "FDF",
            types = setOf(ccfmFdfType),
            degradedTypes = setOf(ccfmType),
        )
        vehicleMapRepository.save(fdfMap)

        val vlsmMap = VehicleMapEntity(
            name = "VLSM",
            types = setOf(vlsmType),
            degradedTypes = setOf(vliType),
        )
        vehicleMapRepository.save(vlsmMap)

        val systelClient = systelClientWithMockedHttp {
            onDataSourceCall("materiel.departement") {
                respondOk(
                    """{"result":[["CCFM", "9", "ARUDY", "ARMABLE", "0", "0", "CCFM", "1", "ARMABLE"],
                ["CCFM", "9", "ARUDY", "ARMABLE", "0", "0", "CCFM_DIV", "1", "ARMABLE"],
                ["CCFM", "9", "ARUDY", "NON ARMABLE", "0", "0", "CCFM_FDF", "1", "NON ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "ARMABLE", "0", "0", "FPTLSR", "1", "ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "ARMABLE", "0", "0", "VSRM", "1", "ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "NON ARMABLE", "0", "0", "FPTL_VEAPS", "1", "NON ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "NON ARMABLE", "0", "0", "FPTL_VEA", "1", "NON ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "NON ARMABLE", "0", "0", "FPTL", "1", "NON ARMABLE"],
                ["FPTLSR", "72", "ARUDY", "NON ARMABLE", "0", "0", "FPTL_PS", "1", "NON ARMABLE"],
                ["MPR_90", "14", "ARUDY", "INDISPONIBLE", "0", "0", "MPR_90", "1", "INDISPONIBLE"],
                ["VATF", "7", "ARUDY", "ARMABLE", "0", "0", "VATF", "1", "ARMABLE"],
                ["VGR", "1", "ARUDY", "ARMABLE", "0", "0", "VGR", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLHR", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLHR_FDF", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "NON ARMABLE", "0", "0", "VSN_SEV", "1", "NON ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_EVAC", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_MED", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VTU", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_INC", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_SAP", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_CA_INC", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "ARMABLE", "0", "0", "VLU_CA_SAP", "1", "ARMABLE"],
                ["VLHR", "40", "ARUDY", "NON ARMABLE", "0", "0", "VSN", "1", "NON ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_ASCEN", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VTU_HYM", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_INC", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_CA_SAP", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_SAP", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VTU", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_PS", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "ARMABLE", "0", "0", "VLU_CA_INC", "1", "ARMABLE"],
                ["VLU", "72", "ARUDY", "NON ARMABLE", "0", "0", "VSN", "1", "NON ARMABLE"],
                ["VSAV", "69", "ARUDY", "NON ARMABLE", "0", "0", "VSAV", "1", "NON ARMABLE"],
                ["VSAV", "69", "ARUDY", "ARMABLE", "0", "0", "VSAV_MI", "1", "ARMABLE"],
                ["VSAV", "69", "ARUDY", "ARMABLE", "0", "0", "VSAV_PS", "1", "ARMABLE"],
                ["VSAV", "69", "ARUDY", "ARMABLE", "0", "0", "VSAV_PS_CA", "1", "ARMABLE"],
                ["VSAV", "69", "ARUDY", "ARMABLE", "0", "0", "VSAV_PS_SP", "1", "ARMABLE"]],"errors":[]}"""
                )
            }
        }

        val vehicleDisplayMapService = VehicleDisplayMapService(
            vehicleMapRepository,
            VehicleService(
                VehicleConfiguration(1000, ""),
                systelClient,
                cisRepository,
                vehicleTypeRepository,
                vehicleStatusRepository,
                SimpleMeterRegistry(),
            ),
            VehicleMapsConfiguration(statusNameBlacklist = emptyList()),
        )

        val actual = vehicleDisplayMapService.getVehicleDisplayMaps()
        val sap = actual.find { it.mapName == "SAP" } ?: fail("should have had SAP map")
        val inc = actual.find { it.mapName == "INC" } ?: fail("should have had INC map")
        val fdf = actual.find { it.mapName == "FDF" } ?: fail("should have had FDF map")

        assertEquals(3, actual.size)

        assertTrue { sap.cisToAvailability.containsKey("ARUDY") }
        assertEquals(0, sap.cisToAvailability["ARUDY"]!!.available)
        assertEquals(1, sap.cisToAvailability["ARUDY"]!!.armableDegraded)
        assertEquals(1, sap.cisToAvailability["ARUDY"]!!.nonArmable)
        assertEquals(0, sap.cisToAvailability["ARUDY"]!!.unavailable)
        assertEquals(1, sap.cisToAvailability["ARUDY"]!!.total)

        assertTrue { inc.cisToAvailability.containsKey("ARUDY") }
        assertEquals(1, inc.cisToAvailability["ARUDY"]!!.available)
        assertEquals(0, inc.cisToAvailability["ARUDY"]!!.armableDegraded)
        assertEquals(0, inc.cisToAvailability["ARUDY"]!!.nonArmable)
        assertEquals(0, inc.cisToAvailability["ARUDY"]!!.unavailable)
        assertEquals(1, inc.cisToAvailability["ARUDY"]!!.total)

        assertTrue { fdf.cisToAvailability.containsKey("ARUDY") }
        assertEquals(0, fdf.cisToAvailability["ARUDY"]!!.available)
        assertEquals(1, fdf.cisToAvailability["ARUDY"]!!.armableDegraded)
        assertEquals(1, fdf.cisToAvailability["ARUDY"]!!.nonArmable)
        assertEquals(0, fdf.cisToAvailability["ARUDY"]!!.unavailable)
        assertEquals(1, fdf.cisToAvailability["ARUDY"]!!.total)
    }
}
