package fr.sdis64.brain.vehicles

import fr.sdis64.brain.utilities.SetCrudRepository
import fr.sdis64.brain.vehicles.entities.VehicleMap

interface VehicleMapRepository : SetCrudRepository<VehicleMap, Long>
