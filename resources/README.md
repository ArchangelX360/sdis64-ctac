# Resources

## Vehicle map

`resources/src/main/resources/vehicle-map.svg` is made from `CIS_v072018.afdesign` file.

To create a new version
- edit `resources/base/vehicle-map/CIS_v072018.afdesign`
- export your modifications to `resources/src/main/resources/vehicle-map.svg`
- run `./gradlew :resources:run`
- inline the output in `ui/src/jsMain/kotlin/fr/sdis64/ui/vehicles/VehicleMaps.kt`
