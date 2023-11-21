package fr.sdis64.backend.vehicles

import fr.sdis64.backend.utilities.SetCrudRepository
import fr.sdis64.backend.vehicles.entities.VehicleMap

interface VehicleMapRepository : SetCrudRepository<VehicleMap, Long>
