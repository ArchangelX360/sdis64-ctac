package fr.sdis64.backend.utilities

import fr.sdis64.api.DisplayOption
import fr.sdis64.api.indicators.GriffonIndicator
import fr.sdis64.api.indicators.ManualIndicatorCategory
import fr.sdis64.api.indicators.ManualIndicatorLevel
import fr.sdis64.api.operators.OperatorPhoneNumber
import fr.sdis64.api.operators.OperatorStatus
import fr.sdis64.api.organisms.Organism
import fr.sdis64.api.organisms.OrganismCategory
import fr.sdis64.api.organisms.OrganismTimeWindow
import fr.sdis64.api.vehicles.Cis
import fr.sdis64.api.vehicles.VehicleMap
import fr.sdis64.api.vehicles.VehicleStatus
import fr.sdis64.api.vehicles.VehicleType
import fr.sdis64.backend.utilities.entities.withId
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import fr.sdis64.backend.cis.entities.Cis as CisEntity
import fr.sdis64.backend.indicators.GriffonIndicator as ServerGriffonIndicator
import fr.sdis64.backend.indicators.entities.ManualIndicatorCategory as ManualIndicatorCategoryEntity
import fr.sdis64.backend.indicators.entities.ManualIndicatorLevel as ManualIndicatorLevelEntity
import fr.sdis64.backend.operators.entities.OperatorPhoneNumber as OperatorPhoneNumberEntity
import fr.sdis64.backend.operators.entities.OperatorStatus as OperatorStatusEntity
import fr.sdis64.backend.organisms.entities.Organism as OrganismEntity
import fr.sdis64.backend.organisms.entities.OrganismCategory as OrganismCategoryEntity
import fr.sdis64.backend.organisms.entities.OrganismDuration as OrganismDurationEntity
import fr.sdis64.backend.vehicles.entities.VehicleMap as VehicleMapEntity
import fr.sdis64.backend.vehicles.entities.VehicleStatus as VehicleStatusEntity
import fr.sdis64.backend.vehicles.entities.VehicleType as VehicleTypeEntity

fun ServerGriffonIndicator.toDTO() = GriffonIndicator(
    level = level,
    backgroundColor = backgroundColor,
    textColor = textColor,
)

fun VehicleMapEntity.toDTO() = VehicleMap(
    id = id,
    name = name,
    types = types.mapToSet { it.toDTO() },
    degradedTypes = degradedTypes.mapToSet { it.toDTO() },
)

fun VehicleMap.toEntity() = VehicleMapEntity(
    name = name,
    types = types.mapToSet { it.toEntity() },
    degradedTypes = degradedTypes.mapToSet { it.toEntity() },
).withId(id)

fun VehicleTypeEntity.toDTO() = VehicleType(
    id = id,
    name = name,
    displayOption = DisplayOption(
        toCta = displayToCta,
        toCodis = displayToCodis,
        position = displayPosition,
    ),
)

fun VehicleType.toEntity() = VehicleTypeEntity(
    name = name,
    displayToCta = displayOption.toCta,
    displayToCodis = displayOption.toCodis,
    displayPosition = displayOption.position,
).withId(id)

fun VehicleStatusEntity.toDTO() = VehicleStatus(
    id = id,
    name = name,
    category = category,
    mode = mode,
    position = position,
    backgroundColor = backgroundColor,
    textColor = textColor,
    blacklist = blacklist.mapToSet { it.toDTO() },
    whitelist = whitelist.mapToSet { it.toDTO() },
)

fun VehicleStatus.toEntity() = VehicleStatusEntity(
    name = name,
    category = category,
    mode = mode,
    position = position,
    backgroundColor = backgroundColor,
    textColor = textColor,
    blacklist = blacklist.mapToSet { it.toEntity() },
    whitelist = whitelist.mapToSet { it.toEntity() },
).withId(id)

fun CisEntity.toDTO() = Cis(
    id = id,
    name = name,
    code = code,
    displayOption = DisplayOption(
        toCta = displayToCta,
        toCodis = displayToCodis,
        position = displayPosition,
    ),
    systelId = systelId,
)

fun Cis.toEntity() = CisEntity(
    name = name,
    code = code,
    systelId = systelId,
    displayToCta = displayOption.toCta,
    displayToCodis = displayOption.toCodis,
    displayPosition = displayOption.position,
)

fun ManualIndicatorLevelEntity.toDTO() = ManualIndicatorLevel(
    id = id,
    name = name,
    category = category.toDTO(),
    descriptions = descriptions,
    active = active,
)

fun ManualIndicatorLevel.toEntity() = ManualIndicatorLevelEntity(
    name = name,
    category = category.toEntity(),
    descriptions = descriptions,
    active = active,
).withId(id)

fun ManualIndicatorCategoryEntity.toDTO() = ManualIndicatorCategory(
    id = id,
    name = name,
    type = type,
)

fun ManualIndicatorCategory.toEntity() = ManualIndicatorCategoryEntity(
    name = name,
    type = type,
).withId(id)

fun OperatorStatusEntity.toDTO() = OperatorStatus(
    id = id,
    name = name,
    backgroundColor = backgroundColor,
    textColor = textColor,
    displayed = displayed,
)

fun OperatorStatus.toEntity() = OperatorStatusEntity(
    name = name,
    backgroundColor = backgroundColor,
    textColor = textColor,
    displayed = displayed,
)

fun OperatorPhoneNumberEntity.toDTO() = OperatorPhoneNumber(
    id = id,
    systelNumber = systelNumber,
    realNumber = realNumber,
)

fun OperatorPhoneNumber.toEntity() = OperatorPhoneNumberEntity(
    systelNumber = systelNumber,
    realNumber = realNumber,
)

fun OrganismEntity.toDTO() = Organism(
    id = id,
    name = name,
    category = category.toDTO(),
    activeTimeWindows = activeTimeWindows.mapToSet { it.toDTO() },
)

fun Organism.toEntity() = OrganismEntity(
    name = name,
    category = category.toEntity(),
    activeTimeWindows = activeTimeWindows.mapToSet { it.toEntity() },
).withId(id)

fun OrganismCategoryEntity.toDTO() = OrganismCategory(
    id = id,
    name = name,
)

fun OrganismCategory.toEntity() = OrganismCategoryEntity(
    name = name,
).withId(id)

fun OrganismDurationEntity.toDTO() = OrganismTimeWindow(
    id = id,
    start = start.toKotlinInstant(),
    end = end.toKotlinInstant(),
)

fun OrganismTimeWindow.toEntity() = OrganismDurationEntity(
    start = start.toJavaInstant(),
    end = end.toJavaInstant(),
).withId(id)
